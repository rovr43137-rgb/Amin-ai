package com.example.ui

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraPreviewView
import com.example.data.SpeechUiState
import com.example.ui.components.ConsentDialog
import com.example.ui.components.HistorySheet
import com.example.ui.components.HolographicHud
import com.example.ui.components.QuickQuerySheet
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SubtitlesOverlay
import com.example.ui.theme.AetherAlertRed
import com.example.ui.theme.AetherCyan
import com.example.ui.theme.AetherGlass
import com.example.ui.theme.AetherObsidian
import com.example.ui.theme.AetherSensorGreen
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherTextPrimary
import com.example.ui.theme.AetherViolet

@Composable
fun AetherScreen(
    viewModel: AetherViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val speechState by viewModel.speechUiState.collectAsState()
    val latestResult by viewModel.latestResult.collectAsState()
    val userSpokenText by viewModel.userSpokenText.collectAsState()
    val historyRecords by viewModel.historyRecords.collectAsState()
    val audioRms by viewModel.speechManager.audioRms.collectAsState()
    val isSpeaking by viewModel.speechManager.isSpeaking.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showQuickQuerySheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(previewViewRef) {
        if (previewViewRef != null) {
            viewModel.bindBitmapProvider {
                previewViewRef?.bitmap
            }
            if (!telemetry.isStreaming && settings.isAutoNarrateEnabled) {
                viewModel.toggleLiveStream()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AetherObsidian)
    ) {
        // Real CameraX Live Viewfinder
        CameraPreviewView(
            modifier = Modifier.fillMaxSize(),
            isTorchOn = telemetry.isTorchOn,
            isFrontCamera = telemetry.isFrontCamera,
            onPreviewViewReady = { previewView ->
                previewViewRef = previewView
            }
        )

        // Holographic Cybernetic HUD Overlay
        HolographicHud(
            modifier = Modifier.fillMaxSize(),
            telemetry = telemetry,
            speechState = speechState,
            audioRms = audioRms
        )

        // Error / Notice Banner
        AnimatedVisibility(
            visible = telemetry.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                color = AetherAlertRed.copy(alpha = 0.92f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                shadowElevation = 8.dp,
                modifier = Modifier.clickable {
                    val msg = telemetry.errorMessage ?: ""
                    if (msg.contains("الباقة") || msg.contains("مفتاح") || msg.contains("429")) {
                        showSettingsSheet = true
                    } else {
                        showQuickQuerySheet = true
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = telemetry.errorMessage ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        val msg = telemetry.errorMessage ?: ""
                        val hint = if (msg.contains("الباقة") || msg.contains("مفتاح") || msg.contains("429")) {
                            "اضغط هنا لإضافة مفتاح Gemini الخاص بك لتجاوز الحدود نهائياً"
                        } else {
                            "اضغط هنا لاختيار سؤال فوري أو الكتابة بدون ميكروفون"
                        }
                        Text(
                            text = hint,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Subtitles and Spoken Speech Overlay
        SubtitlesOverlay(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            result = latestResult,
            recognizedUserText = userSpokenText,
            isSpeaking = isSpeaking,
            onReplaySpeech = { viewModel.replaySpeech() },
            onStopSpeech = { viewModel.stopSpeech() },
            onDismiss = { viewModel.dismissSubtitles() }
        )

        // Futuristic Bottom Control Deck
        BottomControlDeck(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
            isStreaming = telemetry.isStreaming,
            isTorchOn = telemetry.isTorchOn,
            isFrontCamera = telemetry.isFrontCamera,
            speechState = speechState,
            historyCount = historyRecords.size,
            onMicClick = {
                if (speechState == SpeechUiState.LISTENING) {
                    viewModel.stopVoiceListening()
                } else {
                    viewModel.startVoiceQuery()
                }
            },
            onStreamToggle = { viewModel.toggleLiveStream() },
            onQuickQueryClick = { showQuickQuerySheet = true },
            onHistoryClick = { showHistorySheet = true },
            onTorchToggle = { viewModel.toggleTorch() },
            onCameraFlip = { viewModel.toggleCameraFacing() },
            onSettingsClick = { showSettingsSheet = true }
        )

        // History Sheet Modal (Room Database viewer)
        if (showHistorySheet) {
            HistorySheet(
                records = historyRecords,
                onDismiss = { showHistorySheet = false },
                onSpeakRecord = { viewModel.speakText(it) },
                onDeleteRecord = { viewModel.deleteHistoryRecord(it) },
                onClearAll = { viewModel.clearHistory() }
            )
        }

        // Quick Query Sheet (fallback for voice or direct question selection)
        if (showQuickQuerySheet) {
            QuickQuerySheet(
                onDismiss = { showQuickQuerySheet = false },
                onSubmitPrompt = { prompt ->
                    viewModel.requestManualAnalysis(prompt)
                }
            )
        }

        // Settings Dialog Modal
        if (showSettingsSheet) {
            SettingsDialog(
                settings = settings,
                onDismiss = { showSettingsSheet = false },
                onUpdateProvider = { viewModel.updateProvider(it) },
                onUpdateDialect = { viewModel.updateDialect(it) },
                onUpdateInterval = { viewModel.updateIntervalSec(it) },
                onSaveApiKeys = { gemini, openRouter, baseUrl, key, model ->
                    viewModel.saveApiKeys(gemini, openRouter, baseUrl, key, model)
                },
                onUpdateSpeechSettings = { rate, pitch, enabled ->
                    viewModel.updateSpeechSettings(rate, pitch, enabled)
                },
                onUpdateAutoNarrate = { viewModel.updateAutoNarrate(it) }
            )
        }

        // First-Launch Privacy Consent Dialog
        if (!settings.hasAcceptedConsent) {
            ConsentDialog(
                onAccept = { viewModel.acceptConsent() }
            )
        }
    }
}

@Composable
private fun BottomControlDeck(
    modifier: Modifier = Modifier,
    isStreaming: Boolean,
    isTorchOn: Boolean,
    isFrontCamera: Boolean,
    speechState: SpeechUiState,
    historyCount: Int,
    onMicClick: () -> Unit,
    onStreamToggle: () -> Unit,
    onQuickQueryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onTorchToggle: () -> Unit,
    onCameraFlip: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = AetherGlass,
        border = androidx.compose.foundation.BorderStroke(1.dp, AetherCyan.copy(alpha = 0.35f)),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Torch Button
            IconButton(
                onClick = onTorchToggle,
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (isTorchOn) AetherCyan.copy(alpha = 0.2f) else Color.Transparent,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "الفلاش",
                    tint = if (isTorchOn) AetherCyan else Color.White
                )
            }

            // Flip Front/Back Camera
            IconButton(
                onClick = onCameraFlip,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "تبديل الكاميرا",
                    tint = Color.White
                )
            }

            // Central Glowing Push-to-Talk Mic
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(
                        elevation = if (speechState == SpeechUiState.LISTENING) 20.dp else 8.dp,
                        shape = CircleShape,
                        spotColor = if (speechState == SpeechUiState.LISTENING) AetherSensorGreen else AetherCyan
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (speechState == SpeechUiState.LISTENING) {
                                listOf(AetherSensorGreen, Color(0xFF047857))
                            } else {
                                listOf(AetherCyan, Color(0xFF0092B3))
                            }
                        )
                    )
                    .border(
                        2.dp,
                        if (speechState == SpeechUiState.LISTENING) Color.White else AetherCyan,
                        CircleShape
                    )
                    .clickable { onMicClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "تحدث إلى أثير بالعربية",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Quick Questions & Text Prompt (Fallback for voice)
            IconButton(
                onClick = onQuickQueryClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "سؤال سريع أو كتابي",
                    tint = AetherCyan
                )
            }

            // History Records Button (Room Database)
            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "سجل الرصد والاستكشاف",
                        tint = if (historyCount > 0) AetherCyan else Color.White
                    )
                    if (historyCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(AetherSensorGreen, CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            // Live Stream Toggle Button (Continuous stream)
            IconButton(
                onClick = onStreamToggle,
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (isStreaming) AetherSensorGreen.copy(alpha = 0.2f) else Color.Transparent,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isStreaming) "إيقاف البث" else "بدء البث المباشر",
                    tint = if (isStreaming) AetherSensorGreen else Color.White
                )
            }

            // Settings Sheet Button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "الإعدادات والمزودات",
                    tint = Color.White
                )
            }
        }
    }
}
