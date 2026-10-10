package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpeechUiState
import com.example.data.TelemetryState
import com.example.ui.theme.AetherAlertRed
import com.example.ui.theme.AetherCyan
import com.example.ui.theme.AetherGlass
import com.example.ui.theme.AetherLaserAmber
import com.example.ui.theme.AetherSensorGreen
import com.example.ui.theme.AetherTextPrimary
import com.example.ui.theme.AetherTextSecondary
import com.example.ui.theme.AetherViolet
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HolographicHud(
    modifier: Modifier = Modifier,
    telemetry: TelemetryState,
    speechState: SpeechUiState,
    audioRms: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reticle_rotation"
    )

    val scanY by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_laser"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Scanning laser overlay when analyzing or streaming
        if (telemetry.isStreaming || speechState == SpeechUiState.PROCESSING_VISION) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height * scanY
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AetherCyan.copy(alpha = 0.8f),
                            AetherViolet.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // Top Telemetry Console
        TopTelemetryBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            telemetry = telemetry,
            speechState = speechState
        )

        // Center Targeting Reticle
        CenterReticle(
            modifier = Modifier.align(Alignment.Center),
            rotation = rotation,
            scale = if (telemetry.isStreaming) pulseScale else 1f,
            isAnalyzing = speechState == SpeechUiState.PROCESSING_VISION,
            isStreaming = telemetry.isStreaming
        )

        // Real-Time Identified Object or Person Tag
        if (!telemetry.lastIdentifiedObject.isNullOrBlank()) {
            Surface(
                color = AetherGlass,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AetherCyan.copy(alpha = 0.7f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 190.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AetherSensorGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "رصد فوري: ${telemetry.lastIdentifiedObject}",
                        color = AetherCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Dynamic Waveform Visualizer
        AudioWaveVisualizer(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 24.dp),
            speechState = speechState,
            audioRms = audioRms
        )
    }
}

@Composable
private fun TopTelemetryBar(
    modifier: Modifier = Modifier,
    telemetry: TelemetryState,
    speechState: SpeechUiState
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AetherGlass, RoundedCornerShape(16.dp))
            .border(1.dp, AetherCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Provider Badge & Stream Indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            val statusColor = when {
                telemetry.errorMessage != null -> AetherAlertRed
                speechState == SpeechUiState.PROCESSING_VISION -> AetherLaserAmber
                telemetry.isStreaming -> AetherSensorGreen
                else -> AetherCyan
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = telemetry.activeProviderTitle,
                    color = AetherTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        telemetry.errorMessage != null -> "تنبيه بالنظام"
                        speechState == SpeechUiState.PROCESSING_VISION -> "معالجة سحابية…"
                        speechState == SpeechUiState.SPEAKING -> "أثير يتحدث"
                        speechState == SpeechUiState.LISTENING -> "يستمع لصوتك…"
                        telemetry.isStreaming -> "بث سحابي مستمر"
                        else -> "استكشاف لحظي"
                    },
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Metrics: Latency & Frame Counter
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (telemetry.latencyMs > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = AetherCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${telemetry.latencyMs}ms",
                        color = AetherCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = AetherTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "#${telemetry.framesAnalyzed}",
                    color = AetherTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun CenterReticle(
    modifier: Modifier = Modifier,
    rotation: Float,
    scale: Float,
    isAnalyzing: Boolean,
    isStreaming: Boolean
) {
    val reticleColor = when {
        isAnalyzing -> AetherLaserAmber
        isStreaming -> AetherCyan
        else -> AetherCyan.copy(alpha = 0.6f)
    }

    Box(
        modifier = modifier
            .size((160 * scale).dp)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 8.dp.toPx()

            // Outer dashed sensor circle
            drawCircle(
                color = reticleColor.copy(alpha = 0.4f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner rotating arc brackets
            val bracketAngle = 40f
            for (i in 0 until 4) {
                val startAngle = i * 90f + 25f
                drawArc(
                    color = reticleColor,
                    startAngle = startAngle,
                    sweepAngle = bracketAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Central crosshair pip
            val pipRadius = 4.dp.toPx()
            drawCircle(
                color = reticleColor,
                radius = pipRadius,
                center = center
            )
        }
    }
}

@Composable
private fun AudioWaveVisualizer(
    modifier: Modifier = Modifier,
    speechState: SpeechUiState,
    audioRms: Float
) {
    val barCount = 28
    val isActive = speechState == SpeechUiState.LISTENING || speechState == SpeechUiState.SPEAKING

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val transition = rememberInfiniteTransition(label = "wave_bars")
        for (i in 0 until barCount) {
            val wavePhase by transition.animateFloat(
                initialValue = 0f,
                targetValue = 6.28f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200 + (i * 20), easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "bar_$i"
            )

            val baseHeight = if (isActive) {
                val sine = (sin(wavePhase + (i * 0.4f)) + 1f) / 2f
                val rmsBonus = if (audioRms > 0f) audioRms * 28f else 10f
                (8f + sine * rmsBonus).coerceIn(4f, 36f)
            } else {
                3f
            }

            val barColor = when (speechState) {
                SpeechUiState.LISTENING -> AetherSensorGreen
                SpeechUiState.SPEAKING -> AetherCyan
                SpeechUiState.PROCESSING_VISION -> AetherLaserAmber
                else -> AetherTextSecondary.copy(alpha = 0.25f)
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(baseHeight.dp)
                    .background(barColor, RoundedCornerShape(2.dp))
            )
        }
    }
}
