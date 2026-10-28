package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.SleekLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import com.example.data.model.TradingMode
import com.example.ui.components.ModeSelectionBottomSheet
import com.example.ui.components.EditProfileDialog
import com.example.ui.theme.LiveMarketGreen
import com.example.ui.theme.PaperTradingBlue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: TradingViewModel,
    onNavigateToBrokerConnect: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToAuth: () -> Unit = {}
) {
    val userDetails by viewModel.userProfileDetails.collectAsState()
    val summary by viewModel.portfolioSummary.collectAsState()
    val brokerSessionState by viewModel.brokerSessionState.collectAsState()
    val brokerAccountInfo by viewModel.brokerAccountInfo.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val tradingMode by viewModel.tradingMode.collectAsState()

    // Security states
    val mpinEnabled by viewModel.mpinEnabled.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val twoFactorEnabled by viewModel.twoFactorEnabled.collectAsState()

    // Notification states
    val orderNotif by viewModel.orderNotifEnabled.collectAsState()
    val priceAlertNotif by viewModel.priceAlertNotifEnabled.collectAsState()
    val volumeAlertNotif by viewModel.volumeAlertNotifEnabled.collectAsState()
    val ipoAlertNotif by viewModel.ipoAlertNotifEnabled.collectAsState()
    val newsAlertNotif by viewModel.newsAlertNotifEnabled.collectAsState()
    val promoNotif by viewModel.promotionalNotifEnabled.collectAsState()

    // Smart features states
    val smartSearch by viewModel.smartSearchEnabled.collectAsState()
    val watchlistTags by viewModel.watchlistTagsEnabled.collectAsState()
    val advancedCharts by viewModel.advancedChartsEnabled.collectAsState()
    val volumeScanner by viewModel.volumeScannerEnabled.collectAsState()

    var showChangeMpinDialog by remember { mutableStateOf(false) }
    var showDeviceManagementDialog by remember { mutableStateOf(false) }
    var showAddFundsDialog by remember { mutableStateOf(false) }
    var showModeSelectionSheet by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("account_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Header: Profile Card
        item {
            Surface(
                color = NavySurface,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(SleekLavender, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userDetails["name"]?.take(2)?.uppercase() ?: "RN",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyDeep
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userDetails["name"] ?: "Rakesh Nironi",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = EmeraldGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = userDetails["kycStatus"] ?: "VERIFIED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Client Code: ${userDetails["clientCode"]}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekLavender
                            )
                            Text(
                                text = "${userDetails["phone"]} • ${userDetails["email"]}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }

                        // Edit Profile Icon Button
                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier
                                .testTag("account_edit_profile_btn")
                                .size(40.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SleekLavender.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = SleekLavender,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // User Login Information & Session Details Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("user_login_info_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, SleekLavender.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            Text(
                                text = "LOGIN CREDENTIALS & INFO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "AUTHENTICATED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Full Name:", fontSize = 12.sp, color = TextSecondary)
                            Text(userDetails["name"] ?: "Rakesh Nironi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Registered Email:", fontSize = 12.sp, color = TextSecondary)
                            Text(userDetails["email"] ?: "rakeshnironi@gmail.com", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SleekLavender)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Registered Mobile:", fontSize = 12.sp, color = TextSecondary)
                            Text(userDetails["phone"] ?: "+91 9876543210", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Angel Client Code:", fontSize = 12.sp, color = TextSecondary)
                            Text(userDetails["clientCode"] ?: "R982734", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("KYC Verification:", fontSize = 12.sp, color = TextSecondary)
                            Text("Verified (PAN + Aadhaar OK)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EmeraldGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Session Security:", fontSize = 12.sp, color = TextSecondary)
                            Text("256-Bit JWT SSL Encrypted", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CyanAccent)
                        }
                    }
                }
            }
        }

        // Account Mode Switcher Card (Groww + Angel One Style)
        item {
            val isLive = tradingMode == TradingMode.LIVE
            val accentColor = if (isLive) LiveMarketGreen else PaperTradingBlue

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { showModeSelectionSheet = true }
                    .testTag("account_mode_switch_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(2.dp, accentColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLive) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.School,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isLive) "Live Market Mode" else "Paper Trading Mode",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = accentColor,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (isLive) "LIVE" else "PAPER",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isLive) "Real trading mode • Connected to Market" else "Practice mode • Simulation Mode",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentColor
                                )
                            }
                        }

                        Button(
                            onClick = { showModeSelectionSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLive) "Real SmartAPI prices, actual order book & Demat holdings" else "Simulated prices, virtual ₹1,00,000 balance & zero risk",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Funds & Margin Card
        item {
            val isLive = tradingMode == TradingMode.LIVE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isLive) "LIVE TRADING MARGIN (REAL)" else "PRACTICE MARGIN (VIRTUAL ₹1L)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLive) LiveMarketGreen else PaperTradingBlue
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", summary?.virtualCashBalance ?: (if (isLive) 148250.0 else 100000.0))}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLive) LiveMarketGreen else PaperTradingBlue
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (isLive) {
                                        showAddFundsDialog = true
                                    } else {
                                        viewModel.resetVirtualWallet()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekLavender),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(if (isLive) Icons.Default.Add else Icons.Default.Refresh, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isLive) "Add Margin" else "Reset ₹1L", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isLive) "Linked Bank: ${userDetails["bankName"]} (${userDetails["bankAccount"]})" else "Virtual Balance: ₹1,00,000 Practice Account",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = if (isLive) "DP: ${userDetails["depository"]}" else "Zero Risk Simulation",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Section: Profile & Demat Details
        item {
            val isBrokerConnected = brokerSessionState == com.example.data.remote.dto.BrokerSessionState.CONNECTED
            val isBrokerSyncing by viewModel.isBrokerSyncing.collectAsState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROFILE & DEMAT ACCOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekLavender,
                    letterSpacing = 0.8.sp
                )
                if (isBrokerConnected) {
                    TextButton(
                        onClick = { viewModel.syncBrokerData() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBrokerSyncing) "Syncing..." else "Sync Demat",
                            fontSize = 11.sp,
                            color = CyanAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            val panDisplay = brokerAccountInfo?.pan?.ifBlank { null }
                ?: userDetails["pan"]
                ?: "ABCDE1234F"
            val bankNameDisplay = brokerAccountInfo?.bankName?.ifBlank { null }
                ?: userDetails["bankName"]
                ?: "HDFC Bank Limited"
            val bankAccDisplay = brokerAccountInfo?.bankAccountNo?.ifBlank { null }
                ?: userDetails["bankAccount"]
                ?: "•••• •••• 4821"
            val dematIdDisplay = brokerAccountInfo?.dematBoId?.ifBlank { null }
                ?: userDetails["dematBoId"]
                ?: "1208160012345678"
            val depositoryDisplay = brokerAccountInfo?.dpId?.takeIf { it.isNotBlank() }?.let { "CDSL (DP ID: $it)" }
                ?: userDetails["depository"]
                ?: "CDSL"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountDetailRow(icon = Icons.Default.Badge, label = "PAN Card", value = panDisplay)
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountDetailRow(icon = Icons.Default.AccountBalance, label = "Bank Account", value = "$bankNameDisplay • $bankAccDisplay")
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountDetailRow(icon = Icons.Default.Security, label = "Demat BO ID", value = dematIdDisplay)
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountDetailRow(icon = Icons.Default.AccountBalanceWallet, label = "Depository", value = depositoryDisplay)
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountDetailRow(
                        icon = Icons.Default.CheckCircle,
                        label = "KYC Status",
                        value = if (isBrokerConnected) "Verified & Live Active" else (userDetails["kycStatus"] ?: "Complete & Verified")
                    )
                }
            }
        }

        // Section: Broker Connect Shortcut
        item {
            Spacer(modifier = Modifier.height(10.dp))
            AccountSectionHeader(title = "BROKER & SMARTAPI CONNECTION")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateToBrokerConnect() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SleekLavender.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Angel One SmartAPI Connect", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Manage API Key, JWT Tokens & Live RMS", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Section: Security Settings
        item {
            Spacer(modifier = Modifier.height(10.dp))
            AccountSectionHeader(title = "SECURITY SETTINGS")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountSwitchRow(
                        icon = Icons.Default.Pin,
                        title = "MPIN Login",
                        subtitle = "Require 4-digit PIN to open application",
                        checked = mpinEnabled,
                        onCheckedChange = { viewModel.toggleMpin(it) }
                    )
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountSwitchRow(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric / Fingerprint",
                        subtitle = "Instant fingerprint & face unlock",
                        checked = biometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) }
                    )
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountSwitchRow(
                        icon = Icons.Default.Lock,
                        title = "Two-Factor Authentication (2FA)",
                        subtitle = "TOTP authenticator & SMS verification",
                        checked = twoFactorEnabled,
                        onCheckedChange = { viewModel.toggleTwoFactor(it) }
                    )
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeviceManagementDialog = true }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Device Management", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("2 Active logged-in sessions", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        // Section: Notification Settings
        item {
            Spacer(modifier = Modifier.height(10.dp))
            AccountSectionHeader(title = "NOTIFICATION SETTINGS")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountSwitchRow(icon = Icons.AutoMirrored.Filled.ListAlt, title = "Order Notifications", subtitle = "Instant alerts on order execution & triggers", checked = orderNotif, onCheckedChange = { viewModel.orderNotifEnabled.value = it })
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountSwitchRow(icon = Icons.AutoMirrored.Filled.ShowChart, title = "Price Alerts", subtitle = "Stock target price hit notifications", checked = priceAlertNotif, onCheckedChange = { viewModel.priceAlertNotifEnabled.value = it })
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountSwitchRow(icon = Icons.AutoMirrored.Filled.VolumeUp, title = "Volume Alerts", subtitle = "Unusual trading volume spike detection", checked = volumeAlertNotif, onCheckedChange = { viewModel.volumeAlertNotifEnabled.value = it })
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountSwitchRow(icon = Icons.Default.CheckCircle, title = "IPO Alerts", subtitle = "Upcoming mainboard & SME IPO subscriptions", checked = ipoAlertNotif, onCheckedChange = { viewModel.ipoAlertNotifEnabled.value = it })
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountSwitchRow(icon = Icons.Default.Notifications, title = "News Alerts", subtitle = "Critical market news and earnings reports", checked = newsAlertNotif, onCheckedChange = { viewModel.newsAlertNotifEnabled.value = it })
                }
            }
        }

        // Section: Smart Features & App Preferences
        item {
            Spacer(modifier = Modifier.height(10.dp))
            AccountSectionHeader(title = "SMART FEATURES & PREFERENCES")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountSwitchRow(
                        icon = Icons.Default.CandlestickChart,
                        title = "TradingView Advanced Charts",
                        subtitle = "Full-featured candlestick charting with indicators",
                        checked = advancedCharts,
                        onCheckedChange = { viewModel.advancedChartsEnabled.value = it }
                    )
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountSwitchRow(
                        icon = Icons.Default.Tag,
                        title = "Watchlist Smart Tags",
                        subtitle = "Show Top Gainers, Losers, 52W High tags on cards",
                        checked = watchlistTags,
                        onCheckedChange = { viewModel.watchlistTagsEnabled.value = it }
                    )
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    AccountSwitchRow(
                        icon = Icons.Default.Search,
                        title = "Smart Search (F&O, Equities, Commodities)",
                        subtitle = "Multi-segment fuzzy search engine",
                        checked = smartSearch,
                        onCheckedChange = { viewModel.smartSearchEnabled.value = it }
                    )
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleTheme() }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Dark Theme Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Angel One Midnight Pro palette", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = themeMode == com.example.ui.theme.ThemeMode.DARK,
                            onCheckedChange = { viewModel.toggleTheme() },
                            colors = SwitchDefaults.colors(checkedThumbColor = NavyDeep, checkedTrackColor = SleekLavender)
                        )
                    }
                }
            }
        }

        // Logout Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.logout(onLoggedOut = onNavigateToAuth) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Out from Angel One Account", color = CrimsonRed, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add Funds Dialog
    if (showAddFundsDialog) {
        var addAmountText by remember { mutableStateOf("10000") }
        AlertDialog(
            onDismissRequest = { showAddFundsDialog = false },
            containerColor = NavyCardElevated,
            title = { Text("Add Trading Funds", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Transfer funds from linked account: ${userDetails["bankName"]} (${userDetails["bankAccount"]})", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = addAmountText,
                        onValueChange = { addAmountText = it },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = addAmountText.toDoubleOrNull() ?: 0.0
                        viewModel.addVirtualFunds(amt)
                        showAddFundsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekLavender)
                ) {
                    Text("Add via UPI / NetBanking", color = NavyDeep, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddFundsDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Device Management Dialog
    if (showDeviceManagementDialog) {
        AlertDialog(
            onDismissRequest = { showDeviceManagementDialog = false },
            containerColor = NavyCardElevated,
            title = { Text("Active Trusted Devices", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("These devices currently have valid sessions for your Angel One account:", fontSize = 12.sp, color = TextSecondary)

                    Card(colors = CardDefaults.cardColors(containerColor = NavySurface)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Pixel 8 Pro (This Device)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Active Now • Bangalore, IN", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = NavySurface)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = SleekLavender)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Chrome on macOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Last active 2 hrs ago • Mumbai, IN", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDeviceManagementDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SleekLavender)) {
                    Text("Close", color = NavyDeep)
                }
            }
        )
    }

    // Mode Selection Bottom Sheet (Groww + Angel One Style)
    if (showModeSelectionSheet) {
        ModeSelectionBottomSheet(
            currentMode = tradingMode,
            onDismissRequest = { showModeSelectionSheet = false },
            onModeConfirmed = { newMode ->
                viewModel.switchTradingMode(newMode)
            }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = userDetails["name"] ?: "Rakesh Nironi",
            initialEmail = userDetails["email"] ?: "rakeshnironi@gmail.com",
            initialPhone = userDetails["phone"] ?: "+91 9876543210",
            initialClientCode = userDetails["clientCode"] ?: "ANGEL_RN99",
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, email, phone, clientCode ->
                viewModel.updateUserProfile(name, email, phone, clientCode)
            }
        )
    }
}

@Composable
private fun AccountSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = SleekLavender,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun AccountDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, fontSize = 12.sp, color = TextSecondary)
        }
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun AccountSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(subtitle, fontSize = 11.sp, color = TextSecondary)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NavyDeep,
                checkedTrackColor = SleekLavender,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = NavySurface
            )
        )
    }
}
