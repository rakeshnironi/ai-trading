package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.EditProfileDialog
import com.example.util.WhatsAppTradingAlertHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
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
import com.example.security.session.UserSession
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: TradingViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToBrokerConnect: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsState()
    val portfolioSummary by viewModel.portfolioSummary.collectAsState()
    val sessionState by viewModel.brokerSessionState.collectAsState()
    val accountInfo by viewModel.brokerAccountInfo.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val userDetails by viewModel.userProfileDetails.collectAsState()
    val lastAlert by viewModel.lastDispatchedAlert.collectAsState()
    val isScannerRunning by viewModel.isMarketScannerRunning.collectAsState()
    val context = LocalContext.current

    val clipboardManager = LocalClipboardManager.current
    var showDownloadGuideDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var copiedUrlFeedback by remember { mutableStateOf(false) }
    val appShareUrl = "https://ais-pre-fqjcymvmbutrgbvk2sc62f-536147365589.asia-east1.run.app"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trader Profile", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                }
            }
        }

        // Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, CyanAccent),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (userDetails["name"] ?: activeSession?.name ?: profile?.name ?: "RN").take(2).uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = userDetails["name"] ?: activeSession?.name ?: profile?.name ?: "Rakesh Nironi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = userDetails["email"] ?: activeSession?.email ?: profile?.email ?: "rakeshnironi@gmail.com",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF25D366).copy(alpha = 0.15f)) {
                            Text(
                                text = "WhatsApp: ${userDetails["phone"] ?: activeSession?.phone ?: profile?.phone ?: "+91 9876543210"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF25D366),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile Details", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Personal & Broker Information Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Personal & Demat Account", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        TextButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("edit_details_text_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileInfoRow(label = "Full Name", value = userDetails["name"] ?: profile?.name ?: "Rakesh Nironi")
                    ProfileInfoRow(label = "Email Address", value = userDetails["email"] ?: profile?.email ?: "rakeshnironi@gmail.com")
                    ProfileInfoRow(label = "WhatsApp Number", value = userDetails["phone"] ?: profile?.phone ?: "+91 9876543210")
                    ProfileInfoRow(label = "Client Code", value = userDetails["clientCode"] ?: profile?.angelClientId?.ifEmpty { "ANGEL_RN99" } ?: "ANGEL_RN99")
                    ProfileInfoRow(label = "KYC Status", value = userDetails["kycStatus"] ?: "VERIFIED (SEBI / KRA)")
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Automated WhatsApp Trading Alerts Engine Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF25D366).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = "WhatsApp Alerts",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Automated WhatsApp Alerts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("ML Buy/Sell signals dispatched automatically", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (profile?.isWhatsappEnabled != false) Color(0xFF25D366).copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (profile?.isWhatsappEnabled != false) "ACTIVE" else "MUTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profile?.isWhatsappEnabled != false) Color(0xFF25D366) else Color.Red,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "The ML engine continuously monitors RSI, MACD, and ATR volatility. When an actionable Buy or Sell signal triggers, an instant executive trade briefing is automatically formatted and delivered to ${userDetails["phone"] ?: profile?.phone ?: "+91 9876543210"}.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.scanUniverseAndDispatchWhatsAppAlerts() },
                        enabled = !isScannerRunning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("scan_market_and_alert_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScannerRunning) "Scanning ML Models..." else "Scan Market & Dispatch WhatsApp Signals",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    lastAlert?.let { alert ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NavySurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Last Signal: ${alert.action.name} ${alert.symbol}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alert.action == com.example.data.model.SignalType.BUY) EmeraldGreen else CrimsonRed
                                    )
                                    Text(
                                        text = if (alert.success) "Sent ✓" else "Throttled",
                                        fontSize = 10.sp,
                                        color = if (alert.success) EmeraldGreen else AmberGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.message,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        val recMsg = alert.log?.messageText ?: "Trading Alert for ${alert.symbol}"
                                        WhatsAppTradingAlertHelper.openWhatsAppDirect(
                                            context = context,
                                            mobileNumber = userDetails["phone"] ?: profile?.phone ?: "+91 9876543210",
                                            messageText = recMsg
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open in WhatsApp App", fontSize = 11.sp, color = Color(0xFF25D366), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // JWT Session & Security Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                            Text("JWT Session Security", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldGreen.copy(alpha = 0.15f)
                        ) {
                            Text("HS256 ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    activeSession?.let { session ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NavySurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Device:", fontSize = 11.sp, color = TextMuted)
                                    Text(session.deviceModel, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("JWT Token (Masked):", fontSize = 11.sp, color = TextMuted)
                                    Text(
                                        text = if (session.token.length > 20) session.token.take(10) + "..." + session.token.takeLast(8) else "Valid JWT Token",
                                        fontSize = 11.sp,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Hashing & Rounds:", fontSize = 11.sp, color = TextMuted)
                                    Text("BCrypt ($2a$10 rounds)", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.logout {
                                    onNavigateToAuth()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("logout_button")
                        ) {
                            Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onNavigateToAuth,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavySurface, contentColor = CyanAccent),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("Switch Account / Reset", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // ==========================================
        // DOWNLOAD APP & EXPORT APK CARD
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Text("Download & Export App", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text("APK / ZIP / PWA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Text(
                        "Download the compiled APK for your Android phone, export the full Kotlin source code, or copy the direct web application link.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

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
                                .height(40.dp)
                                .testTag("profile_download_apk_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(16.dp))
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
                                .height(40.dp)
                                .testTag("profile_copy_link_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (copiedUrlFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(if (copiedUrlFeedback) "Copied!" else "Copy Link", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Official Broker Gateway Connection Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (sessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED) EmeraldGreen.copy(alpha = 0.6f) else NavyBorder
                )
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = if (sessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED) EmeraldGreen else CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Official Broker Gateway", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (sessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED) EmeraldGreen.copy(alpha = 0.15f) else NavySurface
                        ) {
                            Text(
                                text = if (sessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED) "🟢 CONNECTED" else "⚪ DISCONNECTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED) EmeraldGreen else TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (sessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED && accountInfo != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Connected: ${accountInfo!!.holderName} (${accountInfo!!.clientCode})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Broker: ${accountInfo!!.brokerName} • Exchanges: ${accountInfo!!.exchanges.joinToString(", ")}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    } else {
                        Text(
                            text = "Connect your Angel One SmartAPI account to sync live positions, execute real trades, and stream tick data.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Button(
                        onClick = onNavigateToBrokerConnect,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED) NavySurface else CyanAccent,
                            contentColor = if (sessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED) CyanAccent else NavyDeep
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("manage_broker_button")
                    ) {
                        Text(
                            text = if (sessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED) "Manage Broker Session" else "Connect Angel One Account",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Virtual Capital & Reset Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Virtual Trading Wallet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Available Cash Balance", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = portfolioSummary?.formattedCash ?: "₹1,00,000.00",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }

                        Button(
                            onClick = { viewModel.resetVirtualWallet() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavySurface)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                Text("Reset to ₹1L", fontSize = 12.sp, color = CyanAccent)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
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

    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = userDetails["name"] ?: activeSession?.name ?: profile?.name ?: "Rakesh Nironi",
            initialEmail = userDetails["email"] ?: activeSession?.email ?: profile?.email ?: "rakeshnironi@gmail.com",
            initialPhone = userDetails["phone"] ?: activeSession?.phone ?: profile?.phone ?: "+91 9876543210",
            initialClientCode = userDetails["clientCode"] ?: profile?.angelClientId?.ifEmpty { "ANGEL_RN99" } ?: "ANGEL_RN99",
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, email, phone, clientCode ->
                viewModel.updateUserProfile(name, email, phone, clientCode)
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
