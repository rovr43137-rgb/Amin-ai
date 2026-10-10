package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechManager
import com.example.camera.compressToJpegBytes
import com.example.camera.computeSceneDifference
import com.example.data.AetherSettings
import com.example.data.AiProvider
import com.example.data.AnalysisResult
import com.example.data.ArabicDialect
import com.example.data.SettingsRepository
import com.example.data.SpeechUiState
import com.example.data.TelemetryState
import com.example.network.VisionProviderHub
import com.example.data.db.AetherDatabase
import com.example.data.db.VisionHistoryRepository
import com.example.data.db.VisionRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AetherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private val historyRepository = VisionHistoryRepository(AetherDatabase.getDatabase(application).visionRecordDao())
    private val hub = VisionProviderHub()
    val speechManager = SpeechManager(application)

    val settings: StateFlow<AetherSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AetherSettings()
    )

    val historyRecords: StateFlow<List<VisionRecord>> = historyRepository.allRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _telemetry = MutableStateFlow(TelemetryState())
    val telemetry: StateFlow<TelemetryState> = _telemetry.asStateFlow()

    private val _speechUiState = MutableStateFlow(SpeechUiState.IDLE)
    val speechUiState: StateFlow<SpeechUiState> = _speechUiState.asStateFlow()

    private val _latestResult = MutableStateFlow<AnalysisResult?>(null)
    val latestResult: StateFlow<AnalysisResult?> = _latestResult.asStateFlow()

    private val _userSpokenText = MutableStateFlow<String?>(null)
    val userSpokenText: StateFlow<String?> = _userSpokenText.asStateFlow()

    private var streamingJob: Job? = null
    private var currentBitmapProvider: (() -> Bitmap?)? = null
    private var lastAnalyzedBitmap: Bitmap? = null
    private var quotaCooldownUntil: Long = 0L

    init {
        // Synchronize speech manager state with UI state
        viewModelScope.launch {
            speechManager.isSpeaking.collect { isSpeaking ->
                if (isSpeaking) {
                    _speechUiState.value = SpeechUiState.SPEAKING
                } else if (_speechUiState.value == SpeechUiState.SPEAKING) {
                    _speechUiState.value = SpeechUiState.IDLE
                }
            }
        }

        viewModelScope.launch {
            speechManager.isListening.collect { isListening ->
                if (isListening) {
                    _speechUiState.value = SpeechUiState.LISTENING
                } else if (_speechUiState.value == SpeechUiState.LISTENING) {
                    _speechUiState.value = SpeechUiState.IDLE
                }
            }
        }

        viewModelScope.launch {
            speechManager.speechError.collect { error ->
                if (error != null) {
                    _telemetry.value = _telemetry.value.copy(errorMessage = error)
                }
            }
        }

        viewModelScope.launch {
            settings.collect { currentSettings ->
                _telemetry.value = _telemetry.value.copy(
                    activeProviderTitle = currentSettings.provider.titleArabic
                )
                speechManager.setupArabicTts(
                    dialect = currentSettings.dialect,
                    rate = currentSettings.speechRate,
                    pitch = currentSettings.speechPitch
                )
            }
        }
    }

    fun bindBitmapProvider(provider: () -> Bitmap?) {
        this.currentBitmapProvider = provider
    }

    fun toggleLiveStream() {
        val willStream = !_telemetry.value.isStreaming
        _telemetry.value = _telemetry.value.copy(isStreaming = willStream)

        if (willStream) {
            startStreamingLoop()
        } else {
            streamingJob?.cancel()
            streamingJob = null
        }
    }

    private fun startStreamingLoop() {
        streamingJob?.cancel()
        streamingJob = viewModelScope.launch {
            // First immediate scan as soon as stream starts
            delay(800L) // Brief pause for camera to settle
            if (isActive && _telemetry.value.isStreaming) {
                analyzeCurrentFrame(prompt = "", isStreamTick = true)
            }

            while (isActive && _telemetry.value.isStreaming) {
                // Wait while AI is currently speaking so speech does not overlap
                while (isActive && _telemetry.value.isStreaming && speechManager.isSpeaking.value) {
                    delay(350L)
                }

                // Adaptive Quota Backoff: If in cooldown after a 429 response, wait until rate limit window clears
                val now = System.currentTimeMillis()
                if (now < quotaCooldownUntil) {
                    val remaining = (quotaCooldownUntil - now).coerceAtLeast(1000L)
                    delay(remaining.coerceAtMost(3000L))
                    continue
                }

                val interval = settings.value.liveStreamIntervalSec.coerceIn(5, 30)
                delay(interval * 1000L)

                if (isActive && _telemetry.value.isStreaming && !speechManager.isSpeaking.value) {
                    analyzeCurrentFrame(prompt = "", isStreamTick = true)
                }
            }
        }
    }

    fun requestManualAnalysis(customPrompt: String = "") {
        viewModelScope.launch {
            analyzeCurrentFrame(prompt = customPrompt, isStreamTick = false)
        }
    }

    fun startVoiceQuery() {
        speechManager.stopSpeaking()
        _userSpokenText.value = null
        _speechUiState.value = SpeechUiState.LISTENING

        speechManager.startListening(settings.value.dialect) { recognizedText ->
            _userSpokenText.value = recognizedText
            viewModelScope.launch {
                analyzeCurrentFrame(prompt = recognizedText, isStreamTick = false)
            }
        }
    }

    fun stopVoiceListening() {
        speechManager.stopListening()
        if (_speechUiState.value == SpeechUiState.LISTENING) {
            _speechUiState.value = SpeechUiState.IDLE
        }
    }

    private suspend fun analyzeCurrentFrame(prompt: String, isStreamTick: Boolean) {
        val bitmap = currentBitmapProvider?.invoke()
        if (bitmap == null) {
            if (!isStreamTick) {
                _telemetry.value = _telemetry.value.copy(errorMessage = "تعذر التقاط إطار الكاميرا الحالي.")
            }
            return
        }

        // Permanent Quota Protection: If streaming tick and scene is static (< 8% diff), skip API call!
        if (isStreamTick && lastAnalyzedBitmap != null) {
            val sceneDiff = bitmap.computeSceneDifference(lastAnalyzedBitmap)
            if (sceneDiff < 0.08f) {
                // Scene is static, prevent wasting free-tier API requests
                return
            }
        }

        val wasIdle = _speechUiState.value == SpeechUiState.IDLE
        if (wasIdle) {
            _speechUiState.value = SpeechUiState.PROCESSING_VISION
        }

        _telemetry.value = _telemetry.value.copy(errorMessage = null)

        val jpegBytes = bitmap.compressToJpegBytes(quality = 75, maxDimension = 1024)
        val currentSettings = settings.value

        val result = hub.analyze(
            jpegBytes = jpegBytes,
            userPrompt = prompt,
            dialect = currentSettings.dialect,
            settings = currentSettings,
            isStreamTick = isStreamTick
        )

        result.fold(
            onSuccess = { res ->
                lastAnalyzedBitmap = bitmap
                val detectedObjectLabel = extractLabel(res.arabicResponse)
                val updatedRes = res.copy(identifiedObject = detectedObjectLabel)
                _latestResult.value = updatedRes
                _telemetry.value = _telemetry.value.copy(
                    latencyMs = res.latencyMs,
                    framesAnalyzed = _telemetry.value.framesAnalyzed + 1,
                    lastIdentifiedObject = detectedObjectLabel,
                    errorMessage = null
                )

                // Persist identification result in local Room database
                viewModelScope.launch {
                    historyRepository.saveRecord(
                        identifiedTitle = detectedObjectLabel,
                        fullArabicResponse = res.arabicResponse,
                        userPrompt = prompt,
                        providerName = res.providerName,
                        latencyMs = res.latencyMs
                    )
                }

                if (currentSettings.spokenResponseEnabled) {
                    speechManager.speak(res.arabicResponse)
                } else if (_speechUiState.value == SpeechUiState.PROCESSING_VISION) {
                    _speechUiState.value = SpeechUiState.IDLE
                }
            },
            onFailure = { err ->
                if (_speechUiState.value == SpeechUiState.PROCESSING_VISION) {
                    _speechUiState.value = SpeechUiState.IDLE
                }
                val rawMsg = err.localizedMessage ?: "حدث خطأ في الاتصال."
                val is429 = rawMsg.contains("429") || rawMsg.contains("الحد الأقصى") || rawMsg.contains("quota")
                if (is429) {
                    quotaCooldownUntil = System.currentTimeMillis() + 18000L
                }
                val friendlyMsg = if (is429) {
                    "تم بلوغ حد الباقة المجانية المؤقت. أثير يقوم بالتهدئة التلقائية لتفادي التوقف، أو يمكنك إضافة مفتاح Gemini الخاص بك لتجاوز أي قيود فوراً."
                } else {
                    rawMsg
                }
                _telemetry.value = _telemetry.value.copy(errorMessage = friendlyMsg)
            }
        )
    }

    private fun extractLabel(response: String): String {
        val clean = response.replace(Regex("^[•*\\s#]+"), "").trim()
        val firstSentence = clean.split(".", "،", "\n", "!", "؟").firstOrNull { it.isNotBlank() } ?: clean
        return if (firstSentence.length > 45) {
            firstSentence.take(45) + "…"
        } else {
            firstSentence
        }
    }

    fun replaySpeech() {
        val text = _latestResult.value?.arabicResponse
        if (!text.isNullOrBlank()) {
            speechManager.speak(text)
        }
    }

    fun stopSpeech() {
        speechManager.stopSpeaking()
    }

    fun dismissSubtitles() {
        _latestResult.value = null
        _userSpokenText.value = null
        stopSpeech()
    }

    fun toggleTorch() {
        val newTorch = !_telemetry.value.isTorchOn
        _telemetry.value = _telemetry.value.copy(isTorchOn = newTorch)
    }

    fun toggleCameraFacing() {
        val newFront = !_telemetry.value.isFrontCamera
        _telemetry.value = _telemetry.value.copy(
            isFrontCamera = newFront,
            isTorchOn = false // Torch usually unavailable on front
        )
    }

    fun updateProvider(provider: AiProvider) {
        viewModelScope.launch { repository.updateProvider(provider) }
    }

    fun updateDialect(dialect: ArabicDialect) {
        viewModelScope.launch { repository.updateDialect(dialect) }
    }

    fun updateIntervalSec(sec: Int) {
        viewModelScope.launch { repository.updateIntervalSec(sec) }
    }

    fun saveApiKeys(gemini: String, openRouter: String, baseUrl: String, customKey: String, customModel: String) {
        viewModelScope.launch { repository.updateApiKeys(gemini, openRouter, baseUrl, customKey, customModel) }
    }

    fun updateSpeechSettings(rate: Float, pitch: Float, spokenEnabled: Boolean) {
        viewModelScope.launch { repository.updateSpeechSettings(rate, pitch, spokenEnabled) }
    }

    fun acceptConsent() {
        viewModelScope.launch { repository.acceptConsent() }
    }

    fun updateAutoNarrate(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutoNarrate(enabled)
            _telemetry.value = _telemetry.value.copy(isAutoNarrating = enabled)
            if (enabled && !_telemetry.value.isStreaming) {
                toggleLiveStream()
            }
        }
    }

    fun deleteHistoryRecord(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteRecord(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    fun speakText(text: String) {
        speechManager.speak(text)
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
        speechManager.destroy()
    }
}
