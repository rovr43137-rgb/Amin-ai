package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.ArabicDialect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    private val _isSpeechRecognitionAvailable = MutableStateFlow(true)
    val isSpeechRecognitionAvailable: StateFlow<Boolean> = _isSpeechRecognitionAvailable.asStateFlow()

    init {
        mainHandler.post {
            try {
                _isSpeechRecognitionAvailable.value = SpeechRecognizer.isRecognitionAvailable(context)
                textToSpeech = TextToSpeech(context.applicationContext, this)
            } catch (e: Exception) {
                Log.e("SpeechManager", "Init error: ${e.message}")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            setupArabicTts(ArabicDialect.MSA)
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioRms.value = 0f
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioRms.value = 0f
                }
            })
        } else {
            Log.e("SpeechManager", "TextToSpeech init failed with code: $status")
        }
    }

    fun setupArabicTts(dialect: ArabicDialect, rate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isTtsReady) return
        val tts = textToSpeech ?: return

        val locale = try {
            Locale.forLanguageTag(dialect.localeTag)
        } catch (_: Exception) {
            Locale.forLanguageTag("ar")
        }

        val result = tts.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.forLanguageTag("ar"))
        }

        tts.setSpeechRate(rate.coerceIn(0.6f, 1.8f))
        tts.setPitch(pitch.coerceIn(0.6f, 1.8f))
    }

    fun speak(text: String, utteranceId: String = "aether_speech_${System.currentTimeMillis()}") {
        if (!isTtsReady || text.isBlank()) return
        stopListening()
        textToSpeech?.stop()
        _isSpeaking.value = true

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
        _audioRms.value = 0f
    }

    fun startListening(dialect: ArabicDialect, onResult: (String) -> Unit) {
        mainHandler.post {
            stopSpeaking()

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _speechError.value = "محرك التعرف الصوتي غير متوفر على هذا النظام. يمكنك استخدام الأسئلة السريعة."
                _isListening.value = false
                return@post
            }

            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            _speechError.value = null
                        }
                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }
                        override fun onRmsChanged(rmsdB: Float) {
                            _audioRms.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        }
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _isListening.value = false
                        }
                        override fun onError(error: Int) {
                            _isListening.value = false
                            _audioRms.value = 0f
                            val msg = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "لم يتم التقاط صوت واضح. يمكنك إعادة التحدث أو اختيار سؤال سريع."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "انتهت مهلة الاستماع الصوتي."
                                SpeechRecognizer.ERROR_AUDIO -> "خطأ في تسجيل الصوت من الميكروفون."
                                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "تأكد من الاتصال بالإنترنت للاستماع."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "صلاحية تسجيل الصوت غير ممنوحة."
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "المحرك الصوتي مشغول، جارٍ إعادة التهيئة."
                                else -> "تنبيه في التعرف الصوتي ($error). يمكنك استخدام الأسئلة السريعة."
                            }
                            _speechError.value = msg
                        }
                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            _audioRms.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull()?.trim()
                            if (!text.isNullOrBlank()) {
                                _recognizedText.value = text
                                onResult(text)
                            }
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            matches?.firstOrNull()?.let { _recognizedText.value = it }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, dialect.localeTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, dialect.localeTag)
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar", "ar-SA", "ar-EG"))
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                _speechError.value = "تعذر تشغيل الميكروفون: ${e.message}"
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            _isListening.value = false
            _audioRms.value = 0f
        }
    }

    fun clearError() {
        _speechError.value = null
    }

    fun destroy() {
        mainHandler.post {
            try {
                textToSpeech?.stop()
                textToSpeech?.shutdown()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
        }
    }
}
