package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AetherCyan
import com.example.ui.theme.AetherSensorGreen
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherSurfaceVariant
import com.example.ui.theme.AetherTextPrimary
import com.example.ui.theme.AetherTextSecondary

@Composable
fun ConsentDialog(
    onAccept: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AetherSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AetherCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(AetherCyan.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .border(1.dp, AetherCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AetherCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "سياسة الخصوصية والذكاء السحابي",
                    color = AetherTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "مرحباً بك في أثير (AETHER AI). يعمل المساعد عبر تقنيات الذكاء الاصطناعي السحابي المتطورة لتحليل إطارات الكاميرا والتفاعل معك باللغة العربية الفصحى.",
                    color = AetherTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AetherSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    PrivacyBulletItem(
                        icon = Icons.Default.Cloud,
                        title = "معالجة سحابية تتطلب الإنترنت",
                        desc = "تُرسل لقطات الكاميرا والأسئلة إلى المزود السحابي المختار للمعالجة اللحظية."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PrivacyBulletItem(
                        icon = Icons.Default.Lock,
                        title = "أمان وسرية البيانات",
                        desc = "لا يتم حفظ أو تخزين صورك أو بيعها لأي جهة، وتُحفظ مفاتيحك الخاصة على هاتفك فقط."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PrivacyBulletItem(
                        icon = Icons.Default.Visibility,
                        title = "تحكم كامل وباقات مجانية",
                        desc = "يمكنك إيقاف البث في أي وقت والتبديل بين النماذج وضبط تردد التحليل."
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AetherCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "موافق والبدء في الاستخدام",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyBulletItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AetherCyan,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = AetherTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = desc,
                color = AetherTextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
