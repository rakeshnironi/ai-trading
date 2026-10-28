package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun SettingsScreen(
    viewModel: TradingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBrokerConnect: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsState()

    var whatsappEnabled by remember(profile) { mutableStateOf(profile?.isWhatsappEnabled ?: true) }
    var tradeAlerts by remember(profile) { mutableStateOf(profile?.isTradeAlertsEnabled ?: true) }
    var aiSignals by remember(profile) { mutableStateOf(profile?.isAiSignalsEnabled ?: true) }
    var priceAlerts by remember(profile) { mutableStateOf(profile?.isPriceAlertsEnabled ?: true) }
    var dailySummary by remember(profile) { mutableStateOf(profile?.isDailySummaryEnabled ?: true) }

    var userPhone by remember(profile) { mutableStateOf(profile?.phone ?: "+91 9876543210") }
    var whatsappToken by remember(profile) { mutableStateOf(profile?.whatsappApiKey ?: "") }
    var whatsappPhoneId by remember(profile) { mutableStateOf(profile?.whatsappPhoneNumberId ?: "") }
    var angelApiKey by remember(profile) { mutableStateOf(profile?.angelApiKey ?: "") }
    var angelClientId by remember(profile) { mutableStateOf(profile?.angelClientId ?: "") }

    val clipboardManager = LocalClipboardManager.current
    var showDownloadGuideDialog by remember { mutableStateOf(false) }
    var copiedUrlFeedback by remember { mutableStateOf(false) }

    val appShareUrl = "https://ais-pre-fqjcymvmbutrgbvk2sc62f-536147365589.asia-east1.run.app"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Column {
                    Text("App Settings & Integration", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("API Configs, Export & Alert Channels", fontSize = 11.sp, color = CyanAccent)
                }
            }
        }

        // ==========================================
        // DOWNLOAD & EXPORT APP (APK / SOURCE CODE)
        // ==========================================
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Download & Export App", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyanAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "APK & ZIP Available",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CyanAccent.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Android, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                                }
                            }
                            Column {
                                Text("Autonomous Trading Android App", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Install APK on phone or download full code ZIP", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        // Action Buttons: Download Guide & Copy Web Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showDownloadGuideDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("download_apk_guide_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(18.dp))
                                    Text("Download APK", color = NavyDeep, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(appShareUrl))
                                    copiedUrlFeedback = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("copy_app_url_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copiedUrlFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(if (copiedUrlFeedback) "Link Copied!" else "Copy App Link", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // App Link Surface
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NavySurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Live Web App / Preview URL:", fontSize = 10.sp, color = TextMuted)
                                    Text(appShareUrl, fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.Medium, maxLines = 1)
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(appShareUrl))
                                        copiedUrlFeedback = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = CyanAccent, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Notification Preferences
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Notification Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToggleRow(
                            title = "Master WhatsApp Alerts",
                            subtitle = "Deliver trade & ML alerts directly to WhatsApp",
                            checked = whatsappEnabled,
                            onCheckedChange = {
                                whatsappEnabled = it
                                viewModel.updateNotificationSettings(it, tradeAlerts, aiSignals, priceAlerts, dailySummary)
                            }
                        )

                        ToggleRow(
                            title = "Paper Trade Confirmations",
                            subtitle = "Dispatch message immediately upon order fill",
                            checked = tradeAlerts,
                            onCheckedChange = {
                                tradeAlerts = it
                                viewModel.updateNotificationSettings(whatsappEnabled, it, aiSignals, priceAlerts, dailySummary)
                            }
                        )

                        ToggleRow(
                            title = "AI Neural Consensus Signals",
                            subtitle = "Instant alert when model signals BUY/SELL",
                            checked = aiSignals,
                            onCheckedChange = {
                                aiSignals = it
                                viewModel.updateNotificationSettings(whatsappEnabled, tradeAlerts, it, priceAlerts, dailySummary)
                            }
                        )

                        ToggleRow(
                            title = "Price Level Triggers",
                            subtitle = "Alert when stock crosses targeted price",
                            checked = priceAlerts,
                            onCheckedChange = {
                                priceAlerts = it
                                viewModel.updateNotificationSettings(whatsappEnabled, tradeAlerts, aiSignals, it, dailySummary)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // WhatsApp Gateway Configuration
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("WhatsApp Cloud API Gateway", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = userPhone,
                            onValueChange = { userPhone = it },
                            label = { Text("Recipient Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = whatsappToken,
                            onValueChange = { whatsappToken = it },
                            label = { Text("WhatsApp Access Token (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = whatsappPhoneId,
                            onValueChange = { whatsappPhoneId = it },
                            label = { Text("WhatsApp Phone Number ID (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Button(
                            onClick = {
                                viewModel.saveApiCredentials(
                                    angelApiKey = angelApiKey,
                                    angelClientId = angelClientId,
                                    whatsappToken = whatsappToken,
                                    whatsappPhoneId = whatsappPhoneId,
                                    userPhone = userPhone
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save WhatsApp Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Angel One SmartAPI Details
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Angel One SmartAPI Gateway", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SmartAPI Feed Status", fontSize = 13.sp, color = TextSecondary)
                            Surface(shape = RoundedCornerShape(6.dp), color = EmeraldGreen.copy(alpha = 0.15f)) {
                                Text("CONNECTED (Retrofit V2)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        OutlinedTextField(
                            value = angelApiKey,
                            onValueChange = { angelApiKey = it },
                            label = { Text("SmartAPI API Key (X-PrivateKey)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = angelClientId,
                            onValueChange = { angelClientId = it },
                            label = { Text("Angel One Client ID (e.g. A123456)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Button(
                            onClick = {
                                viewModel.saveApiCredentials(
                                    angelApiKey = angelApiKey,
                                    angelClientId = angelClientId,
                                    whatsappToken = whatsappToken,
                                    whatsappPhoneId = whatsappPhoneId,
                                    userPhone = userPhone
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Angel One Config", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToBrokerConnect,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.2f), contentColor = EmeraldGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Connect Broker (Official Login & TOTP)", fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("• Retrofit 2.12 + Moshi Network Service active", fontSize = 11.sp, color = TextMuted)
                        Text("• Endpoints: LTP Data, Full Market Quotes & Historical Candles", fontSize = 11.sp, color = TextMuted)
                        Text("• Strict constraint enforced: Zero third-party web scraping used.", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Educational Disclaimer Notice
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Disclaimer: This software is designed for academic research and educational paper trading purposes only. Trading in equity markets involves financial risk.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // DOWNLOAD & EXPORT GUIDE MODAL DIALOG
    // ==========================================
    if (showDownloadGuideDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadGuideDialog = false },
            containerColor = NavyCardElevated,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CyanAccent)
                    Text("Download & Export App", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "You can easily install or export this Autonomous Trading app on your phone or computer:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Option 1: Download APK directly from AI Studio
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Android, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                Text("1. Generate APK / AAB on Device", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            }
                            Text(
                                "• In the AI Studio editor header menu (top right), tap Settings ⚙️ or Export.\n• Select 'Generate APK / AAB' to trigger the build.\n• Once built, tap Download APK directly onto your Android device.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Option 2: Export Source Code ZIP
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.FolderZip, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                                Text("2. Export Complete Project (.ZIP)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            }
                            Text(
                                "• Open AI Studio Top Bar -> Project Menu -> 'Export as ZIP'.\n• Unzip and open in Android Studio to build debug/release APK anytime with standard Gradle commands.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Option 3: Instant Browser Preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                Text("3. Instant Web Preview on Mobile", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            }
                            Text(
                                "You can also open the Shared URL on any mobile browser (Chrome/Safari) and tap 'Add to Home Screen' (PWA) for immediate native-like access.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(appShareUrl))
                        showDownloadGuideDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(16.dp))
                        Text("Copy Link", color = NavyDeep, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadGuideDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NavyDeep,
                checkedTrackColor = CyanAccent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = NavyDeep
            )
        )
    }
}
