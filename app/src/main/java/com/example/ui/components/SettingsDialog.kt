package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AetherSettings
import com.example.data.AiProvider
import com.example.data.ArabicDialect
import com.example.ui.theme.AetherCyan
import com.example.ui.theme.AetherLaserAmber
import com.example.ui.theme.AetherSensorGreen
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherSurfaceVariant
import com.example.ui.theme.AetherTextMuted
import com.example.ui.theme.AetherTextPrimary
import com.example.ui.theme.AetherTextSecondary
import com.example.ui.theme.AetherViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    settings: AetherSettings,
    onDismiss: () -> Unit,
    onUpdateProvider: (AiProvider) -> Unit,
    onUpdateDialect: (ArabicDialect) -> Unit,
    onUpdateInterval: (Int) -> Unit,
    onSaveApiKeys: (geminiKey: String, openRouterKey: String, customBaseUrl: String, customKey: String, customModel: String) -> Unit,
    onUpdateSpeechSettings: (rate: Float, pitch: Float, spokenEnabled: Boolean) -> Unit,
    onUpdateAutoNarrate: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var geminiKey by remember { mutableStateOf(settings.geminiCustomApiKey) }
    var openRouterKey by remember { mutableStateOf(settings.openRouterApiKey) }
    var customBaseUrl by remember { mutableStateOf(settings.customBaseUrl) }
    var customKey by remember { mutableStateOf(settings.customApiKey) }
    var customModel by remember { mutableStateOf(settings.customModel) }
    var hideKeys by remember { mutableStateOf(true) }

    var intervalSec by remember { mutableStateOf(settings.liveStreamIntervalSec) }
    var speechRate by remember { mutableStateOf(settings.speechRate) }
    var speechPitch by remember { mutableStateOf(settings.speechPitch) }
    var spokenEnabled by remember { mutableStateOf(settings.spokenResponseEnabled) }
    var autoNarrateEnabled by remember { mutableStateOf(settings.isAutoNarrateEnabled) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AetherSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = AetherCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مركز قيادة وإعدادات أثير",
                        color = AetherTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = AetherTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Free Tier & Quotas Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AetherCyan.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AetherCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الذكاء السحابي والباقات المجانية",
                            color = AetherTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أثير يعتمد بالكامل على السحابة ولا يتطلب تنزيل نماذج ضخمة على هاتفك. يُعطي التطبيق الأولوية للباقات المجانية (مثل حصة Google Gemini 2.5 Flash المجانية أو نماذج OpenRouter المجانية). يتطلب التطبيق اتصالاً بالإنترنت.",
                            color = AetherTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Provider Selection Section
            Text(
                text = "مزود الذكاء الاصطناعي (AI Provider)",
                color = AetherCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            AiProvider.values().forEach { provider ->
                val isSelected = settings.provider == provider
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onUpdateProvider(provider) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AetherSurfaceVariant else AetherSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) AetherCyan else AetherSurfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = provider.titleArabic,
                                    color = if (isSelected) AetherCyan else AetherTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (provider.isFreeByDefault) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(AetherSensorGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "باقة مجانية",
                                            color = AetherSensorGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = provider.descriptionArabic,
                                color = AetherTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AetherCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dialect Selection
            Text(
                text = "صوت ولهجة التحدث بالعربية",
                color = AetherCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            ArabicDialect.values().forEach { dialect ->
                val isSelected = settings.dialect == dialect
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateDialect(dialect) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dialect.titleArabic,
                            color = if (isSelected) AetherCyan else AetherTextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (dialect == ArabicDialect.MSA) "الفصحى المحسنة والمطلقة (موصى به)" else "تحدث طبيعي باللهجة",
                            color = AetherTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AetherCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Live Stream Frequency
            Text(
                text = "فترة تكرار البث المباشر (بالثواني): $intervalSec ثوانٍ",
                color = AetherCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "التحكم في تكرار تحليل الكاميرا يحافظ على الباقة المجانية واستهلاك الإنترنت.",
                color = AetherTextMuted,
                fontSize = 11.sp
            )
            Slider(
                value = intervalSec.toFloat(),
                onValueChange = {
                    intervalSec = it.toInt()
                    onUpdateInterval(intervalSec)
                },
                valueRange = 3f..15f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = AetherCyan,
                    activeTrackColor = AetherCyan,
                    inactiveTrackColor = AetherSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-Narrate Everything Seen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الرصد والتحدث التلقائي عند الرؤية",
                        color = AetherCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يتعرف أثير تلقائياً على كل ما يظهر في الكاميرا (أشخاص، أدوات، نصوص، أماكن) ويخبرك فوراً بصوته.",
                        color = AetherTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Switch(
                    checked = autoNarrateEnabled,
                    onCheckedChange = {
                        autoNarrateEnabled = it
                        onUpdateAutoNarrate(autoNarrateEnabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AetherSensorGreen,
                        checkedTrackColor = AetherSensorGreen.copy(alpha = 0.35f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spoken Response & Voice sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الرد الصوتي المسموع (TTS)",
                        color = AetherTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "نطق الإجابات تلقائياً بصوت عربي فصيح",
                        color = AetherTextMuted,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = spokenEnabled,
                    onCheckedChange = {
                        spokenEnabled = it
                        onUpdateSpeechSettings(speechRate, speechPitch, spokenEnabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AetherCyan,
                        checkedTrackColor = AetherCyan.copy(alpha = 0.3f)
                    )
                )
            }

            if (spokenEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "سرعة الإلقاء الصوتي: ${String.format("%.1fx", speechRate)}",
                    color = AetherTextSecondary,
                    fontSize = 12.sp
                )
                Slider(
                    value = speechRate,
                    onValueChange = {
                        speechRate = it
                        onUpdateSpeechSettings(speechRate, speechPitch, spokenEnabled)
                    },
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = AetherCyan,
                        activeTrackColor = AetherCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // API Keys Configuration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مفاتيح API المخصصة (اختياري)",
                    color = AetherCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { hideKeys = !hideKeys }) {
                    Icon(
                        imageVector = if (hideKeys) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "إظهار/إخفاء المفاتيح",
                        tint = AetherTextSecondary
                    )
                }
            }

            Text(
                text = "💡 نصيحة لتجاوز حدود الباقة المجانية: يمكنك الحصول على مفتاحك المجاني الخاص 100% من موقع Google AI Studio ووضعه هنا للاستمتاع بحصة طلبات خاصة بك ودائمة ومستمرة.",
                color = AetherCyan.copy(alpha = 0.9f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = geminiKey,
                onValueChange = { geminiKey = it },
                label = { Text("مفتاح Gemini API الخاص بك (اختياري)") },
                placeholder = { Text("AIzaSy...") },
                visualTransformation = if (hideKeys) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AetherCyan,
                    unfocusedBorderColor = AetherSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = openRouterKey,
                onValueChange = { openRouterKey = it },
                label = { Text("مفتاح OpenRouter API (للنماذج المجانية والمفتوحة)") },
                placeholder = { Text("sk-or-v1-...") },
                visualTransformation = if (hideKeys) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AetherCyan,
                    unfocusedBorderColor = AetherSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (settings.provider == AiProvider.CUSTOM) {
                OutlinedTextField(
                    value = customBaseUrl,
                    onValueChange = { customBaseUrl = it },
                    label = { Text("عنوان الخادم المخصص Base URL") },
                    placeholder = { Text("https://api.openai.com/v1/") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AetherCyan,
                        unfocusedBorderColor = AetherSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customKey,
                    onValueChange = { customKey = it },
                    label = { Text("مفتاح الخادم المخصص API Key") },
                    visualTransformation = if (hideKeys) PasswordVisualTransformation() else VisualTransformation.None,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AetherCyan,
                        unfocusedBorderColor = AetherSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customModel,
                    onValueChange = { customModel = it },
                    label = { Text("اسم نموذج الرؤية (Model Name)") },
                    placeholder = { Text("gpt-4o-mini أو ollama-llava") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AetherCyan,
                        unfocusedBorderColor = AetherSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onSaveApiKeys(geminiKey, openRouterKey, customBaseUrl, customKey, customModel)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AetherCyan)
            ) {
                Text("حفظ المفاتيح والإعدادات", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Open Source MIT Notice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = AetherTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AETHER AI • مشروع مفتوح المصدر (MIT License)",
                    color = AetherTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
