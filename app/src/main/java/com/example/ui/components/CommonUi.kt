package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OtpDetails
import com.example.data.model.SmsCategory
import com.example.ui.theme.AccentEmeraldContainer
import com.example.ui.theme.AccentEmeraldText
import com.example.ui.theme.AccentWarningContainer
import com.example.ui.theme.AccentWarningText
import com.example.ui.theme.ActiveCardBorderBrush
import com.example.ui.theme.AppSurfaceVariantLight
import com.example.ui.theme.AppWhite
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.PrimaryOrangeBorder
import com.example.ui.theme.PrimaryOrangeContainer
import com.example.ui.theme.PrimaryOrangeGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CategoryBadge(category: SmsCategory, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (category) {
        SmsCategory.OTP -> Triple(PrimaryOrangeContainer, PrimaryOrange, "OTP CODE")
        SmsCategory.BANKING -> Triple(AccentEmeraldContainer, AccentEmeraldText, "BANKING")
        SmsCategory.SECURITY -> Triple(AccentWarningContainer, AccentWarningText, "SECURITY")
        SmsCategory.GENERAL -> Triple(AppSurfaceVariantLight, TextSecondary, "MESSAGE")
        SmsCategory.ALL -> Triple(AppSurfaceVariantLight, TextSecondary, "ALL")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun OtpQuickActionCard(
    otp: OtpDetails,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var copiedRecently by remember { mutableStateOf(false) }

    val cardShape = RoundedCornerShape(20.dp)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDFB)),
        shape = cardShape,
        border = BorderStroke(1.5.dp, ActiveCardBorderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .testTag("otp_quick_action_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val initials = remember(otp.serviceName) {
                        otp.serviceName.filter { it.isLetterOrDigit() }.take(2).uppercase().ifBlank { "OT" }
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryOrangeContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = PrimaryOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = otp.serviceName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = PrimaryOrangeContainer,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, PrimaryOrangeBorder)
                            ) {
                                Text(
                                    text = "PASSCODE DETECTED",
                                    color = PrimaryOrange,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "One-tap instant copy for quick verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, PrimaryOrangeBorder),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "SECURITY CODE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = otp.code,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp,
                            color = PrimaryOrange
                        )
                    }

                    Button(
                        onClick = {
                            onCopy(otp.code)
                            copiedRecently = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (copiedRecently) Color(0xFF10B981) else PrimaryOrange
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("copy_otp_btn")
                    ) {
                        Icon(
                            imageVector = if (copiedRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copiedRecently) "Copied!" else "Copy Code",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionBanner(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasPermission) {
        val bannerShape = RoundedCornerShape(18.dp)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF8)),
            border = BorderStroke(1.dp, PrimaryOrangeBorder),
            shape = bannerShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier
                .fillMaxWidth()
                .clip(bannerShape)
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryOrangeContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "SMS Permission",
                        tint = PrimaryOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SMS Real-Time Sync",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Grant SMS permissions to receive live passcodes and sync sent carrier messages.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("grant_permission_btn")
                ) {
                    Text("Enable", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
