package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnalysisResult
import com.example.ui.theme.AetherCyan
import com.example.ui.theme.AetherGlass
import com.example.ui.theme.AetherSensorGreen
import com.example.ui.theme.AetherTextPrimary
import com.example.ui.theme.AetherTextSecondary
import com.example.ui.theme.AetherViolet

@Composable
fun SubtitlesOverlay(
    modifier: Modifier = Modifier,
    result: AnalysisResult?,
    recognizedUserText: String?,
    isSpeaking: Boolean,
    onReplaySpeech: () -> Unit,
    onStopSpeech: () -> Unit,
    onDismiss: () -> Unit
) {
    val isVisible = result != null || !recognizedUserText.isNullOrBlank()

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = AetherGlass,
            border = androidx.compose.foundation.BorderStroke(1.dp, AetherCyan.copy(alpha = 0.4f)),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header row: Status tag and action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = if (isSpeaking) AetherCyan else AetherTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSpeaking) "أثير يتحدث (عربية فصحى)" else "التحليل البصري السحابي",
                            color = if (isSpeaking) AetherCyan else AetherTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row {
                        if (result != null) {
                            IconButton(
                                onClick = {
                                    if (isSpeaking) onStopSpeech() else onReplaySpeech()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isSpeaking) "إيقاف الصوت" else "إعادة الاستماع",
                                    tint = if (isSpeaking) AetherViolet else AetherCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق الشاشة",
                                tint = AetherTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // If user asked something via voice:
                if (!recognizedUserText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سؤالك: \"$recognizedUserText\"",
                        color = AetherSensorGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // AI Response text:
                if (result != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result.arabicResponse,
                        color = AetherTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "المزود: ${result.providerName} • زمن الاستجابة: ${result.latencyMs}ms",
                        color = AetherTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
