package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.data.remote.dto.BrokerType
import com.example.ui.theme.SleekDarkPurple
import com.example.ui.theme.SleekLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import com.example.util.OtpNotificationHelper
import kotlinx.coroutines.delay

// Angel One Brand Identity Colors
val AngelOrange = Color(0xFFFF5722)
val AngelOrangeDark = Color(0xFFE64A19)
val AngelOrangeLight = Color(0xFFFF7043)
val AngelBlue = Color(0xFF1E88E5)
val AngelNavy = Color(0xFF0F1B3E)
val AngelNavyDark = Color(0xFF0A1128)
val AngelSurface = Color(0xFF14224C)

enum class AngelLoginTab(val title: String) {
    MOBILE("Mobile OTP"),
    CLIENT_ID("Client ID"),
    EMAIL("Email & Pass"),
    OPEN_ACCOUNT("Register")
}

enum class MobileLoginStep {
    ENTER_NUMBER,
    ENTER_OTP,
    ENTER_PIN
}

@Composable
fun AuthContainerScreen(
    viewModel: TradingViewModel,
    onAuthSuccess: () -> Unit,
    onNavigateToBrokerConnect: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isLoading by viewModel.isAuthLoading.collectAsState()
    val errorMessage by viewModel.authErrorMessage.collectAsState()

    var selectedBroker by remember { mutableStateOf(BrokerType.ANGEL_ONE) }
    var selectedTab by remember { mutableStateOf(AngelLoginTab.MOBILE) }

    // Direct Email & Password Flow State
    var emailAddress by remember { mutableStateOf("rakeshnironi@gmail.com") }
    var emailPassword by remember { mutableStateOf("Password@123") }
    var emailPasswordVisible by remember { mutableStateOf(false) }
    var rememberEmail by remember { mutableStateOf(true) }

    // Mobile Number Login Flow State
    var mobileStep by remember { mutableStateOf(MobileLoginStep.ENTER_NUMBER) }
    var mobileNumber by remember { mutableStateOf("9876543210") }
    var mobileOtp by remember { mutableStateOf("") }
    var mobilePin by remember { mutableStateOf("1234") }
    var rememberMobile by remember { mutableStateOf(true) }
    var enableBiometric by remember { mutableStateOf(true) }

    // Client ID Login Flow State
    var clientCode by remember { mutableStateOf("R982734") }
    var mpinOrPassword by remember { mutableStateOf("1234") }
    var totpCode by remember { mutableStateOf("729410") }
    var apiKey by remember { mutableStateOf("s6BvE892YqW91") }
    var rememberClientId by remember { mutableStateOf(true) }
    var showSmartApiSettings by remember { mutableStateOf(false) }
    var mpinVisible by remember { mutableStateOf(false) }

    // Open Demat Account State
    var registerFullName by remember { mutableStateOf("Rakesh Nironi") }
    var registerMobile by remember { mutableStateOf("9876543210") }
    var registerEmail by remember { mutableStateOf("rakeshnironi@gmail.com") }
    var registerCity by remember { mutableStateOf("Bengaluru") }
    var registerReferralCode by remember { mutableStateOf("ANGEL2025") }

    // Dialog States
    var showForgotMpinDialog by remember { mutableStateOf(false) }
    var showForgotClientIdDialog by remember { mutableStateOf(false) }
    var showSmartApiGuideDialog by remember { mutableStateOf(false) }

    // OTP Timer & Delivery States
    var otpTimerSeconds by remember { mutableIntStateOf(30) }
    var isOtpTimerRunning by remember { mutableStateOf(false) }

    var activeGeneratedOtp by remember { mutableStateOf("729410") }
    var showIncomingSmsBanner by remember { mutableStateOf(false) }
    var isAutoReadingSms by remember { mutableStateOf(false) }
    var autoReadComplete by remember { mutableStateOf(false) }
    var showOtpHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isOtpTimerRunning) {
        if (isOtpTimerRunning) {
            otpTimerSeconds = 30
            while (otpTimerSeconds > 0) {
                delay(1000L)
                otpTimerSeconds--
            }
            isOtpTimerRunning = false
        }
    }

    LaunchedEffect(isAutoReadingSms) {
        if (isAutoReadingSms) {
            delay(1200L)
            mobileOtp = activeGeneratedOtp
            isAutoReadingSms = false
            autoReadComplete = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // ANGEL ONE OFFICIAL BRAND HEADER
            // ==========================================
            AngelOneBrandHeader(
                onHelpClick = { showSmartApiGuideDialog = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // INCOMING SMS FLOATING NOTIFICATION BANNER
            // ==========================================
            AnimatedVisibility(
                visible = showIncomingSmsBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF162348),
                    border = BorderStroke(1.5.dp, AngelOrange),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AngelOrange.copy(alpha = 0.2f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            tint = AngelOrange,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        "Messages • VK-ANGELONE • SIM 1",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Now • High Priority OTP Alert",
                                        fontSize = 10.sp,
                                        color = EmeraldGreen
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showIncomingSmsBanner = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "<#> $activeGeneratedOtp is your secret verification code to access Angel One SuperApp. Valid for 10 minutes. Do not share with anyone. [VK-ANGELONE]",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    mobileOtp = activeGeneratedOtp
                                    autoReadComplete = true
                                    isAutoReadingSms = false
                                    showIncomingSmsBanner = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AngelOrange),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("⚡ Auto-Fill ($activeGeneratedOtp)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    OtpNotificationHelper.copyToClipboard(context, activeGeneratedOtp)
                                },
                                border = BorderStroke(1.dp, NavyBorder),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                                    Text("Copy Code", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Error Banner
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CrimsonRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(18.dp))
                                Text(msg, color = CrimsonRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.loginWithDematAccount(
                                        brokerType = BrokerType.SIMULATED_PAPER,
                                        clientCode = "PAPER_DEMAT",
                                        passwordOrMpin = "1234",
                                        totp = "729410",
                                        apiKey = "sandbox_unlimited",
                                        onSuccess = onAuthSuccess
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CyanAccent),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text("⚡ Bypass & Enter via Demo / Paper Demat", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // DEMAT ACCOUNT BROKER SELECTOR (ANY BROKER)
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("demat_broker_selector_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Text("Select Demat Account", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldGreen.copy(alpha = 0.15f)
                        ) {
                            Text("SEBI / NSE / BSE", fontSize = 9.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Choose any Demat account to sign in and link trading APIs:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val brokers = listOf(
                            BrokerType.ANGEL_ONE to "Angel One",
                            BrokerType.ZERODHA to "Zerodha Kite",
                            BrokerType.GROWW to "Groww",
                            BrokerType.UPSTOX to "Upstox",
                            BrokerType.SIMULATED_PAPER to "Paper Demat"
                        )
                        items(brokers) { (bType, bName) ->
                            val isChosen = selectedBroker == bType
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChosen) CyanAccent.copy(alpha = 0.2f) else NavyCard,
                                border = BorderStroke(
                                    1.dp,
                                    if (isChosen) CyanAccent else NavyBorder
                                ),
                                modifier = Modifier.clickable {
                                    selectedBroker = bType
                                    when (bType) {
                                        BrokerType.ANGEL_ONE -> {
                                            clientCode = "R982734"
                                            apiKey = "s6BvE892YqW91"
                                        }
                                        BrokerType.ZERODHA -> {
                                            clientCode = "ZH4821"
                                            apiKey = "kite_prod_api"
                                        }
                                        BrokerType.GROWW -> {
                                            clientCode = "GW9021"
                                            apiKey = "groww_app_v1"
                                        }
                                        BrokerType.UPSTOX -> {
                                            clientCode = "UP7734"
                                            apiKey = "upstox_sec_v2"
                                        }
                                        BrokerType.SIMULATED_PAPER -> {
                                            clientCode = "PAPER_RN01"
                                            apiKey = "sandbox_unlimited"
                                        }
                                    }
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    if (isChosen) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(13.dp))
                                    }
                                    Text(
                                        text = bName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isChosen) TextPrimary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // DEMO LOGIN CREDENTIALS & 1-TAP LOGIN CARD
            // ==========================================
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF14244D),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("demo_credentials_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Text("Ready Login Credentials", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text("Pre-configured", fontSize = 9.sp, color = CyanAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Selected Demat:", fontSize = 11.sp, color = TextSecondary)
                            Text(selectedBroker.displayName, fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Email / Pass:", fontSize = 11.sp, color = TextSecondary)
                            Text("rakeshnironi@gmail.com • Password@123", fontSize = 11.sp, color = SleekLavender, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mobile / PIN:", fontSize = 11.sp, color = TextSecondary)
                            Text("9876543210 • PIN: 1234 (OTP: 729410)", fontSize = 11.sp, color = SleekLavender, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Demat Client Code:", fontSize = 11.sp, color = TextSecondary)
                            Text("$clientCode • PIN: 1234 • TOTP: 729410", fontSize = 11.sp, color = SleekLavender, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.loginWithDematAccount(
                                brokerType = selectedBroker,
                                clientCode = clientCode,
                                passwordOrMpin = mpinOrPassword,
                                totp = totpCode,
                                apiKey = apiKey,
                                onSuccess = onAuthSuccess
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("quick_login_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("⚡ 1-Tap Demat Login (${selectedBroker.displayName.split(" ")[0]})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // ==========================================
            // ANGEL ONE LOGIN TABS
            // ==========================================
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = AngelOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = AngelOrange,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    AngelLoginTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = {
                                viewModel.authErrorMessage.value = null
                                selectedTab = tab
                            },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AngelOrange else TextSecondary,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // MAIN ANGEL ONE FORM CONTAINER
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTab) {
                        // ----------------------------------------------------
                        // TAB 1: MOBILE NUMBER & OTP / MPIN FLOW
                        // ----------------------------------------------------
                        AngelLoginTab.MOBILE -> {
                            when (mobileStep) {
                                MobileLoginStep.ENTER_NUMBER -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Login to Angel One",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Enter your registered 10-digit mobile number",
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AngelOrange.copy(alpha = 0.15f),
                                                modifier = Modifier.clickable {
                                                    mobileNumber = "9876543210"
                                                }
                                            ) {
                                                Text(
                                                    "⚡ Auto-fill",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AngelOrange,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        // Mobile Number Input Field with +91 Country Code
                                        OutlinedTextField(
                                            value = mobileNumber,
                                            onValueChange = { input ->
                                                if (input.length <= 10) {
                                                    mobileNumber = input.filter { it.isDigit() }
                                                }
                                            },
                                            label = { Text("Mobile Number") },
                                            leadingIcon = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                                                ) {
                                                    Text("🇮🇳 +91", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .height(20.dp)
                                                            .width(1.dp)
                                                            .background(NavyBorder)
                                                    )
                                                }
                                            },
                                            placeholder = { Text("Enter 10-digit mobile number") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("angel_mobile_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AngelOrange,
                                                unfocusedBorderColor = NavyBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            )
                                        )

                                        // Remember Mobile Checkbox
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = rememberMobile,
                                                onCheckedChange = { rememberMobile = it },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = AngelOrange,
                                                    checkmarkColor = Color.White
                                                )
                                            )
                                            Text(
                                                text = "Remember mobile number on this device",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        // Action Button: PROCEED
                                        Button(
                                            onClick = {
                                                if (mobileNumber.length == 10) {
                                                    viewModel.authErrorMessage.value = null
                                                    val generated = OtpNotificationHelper.generateOtp()
                                                    activeGeneratedOtp = generated
                                                    OtpNotificationHelper.sendOtpNotification(
                                                        context = context,
                                                        mobileNumber = mobileNumber,
                                                        otpCode = generated
                                                    )
                                                    showIncomingSmsBanner = true
                                                    isAutoReadingSms = true
                                                    autoReadComplete = false
                                                    isOtpTimerRunning = true
                                                    mobileStep = MobileLoginStep.ENTER_OTP
                                                } else {
                                                    viewModel.authErrorMessage.value = "Please enter a valid 10-digit mobile number"
                                                }
                                            },
                                            enabled = mobileNumber.length == 10 && !isLoading,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AngelOrange),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("angel_proceed_mobile_button")
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                            } else {
                                                Text("PROCEED", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }

                                MobileLoginStep.ENTER_OTP -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            IconButton(
                                                onClick = { mobileStep = MobileLoginStep.ENTER_NUMBER },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                                            }
                                            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                                Text("Enter 6-Digit OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Sent to +91 $mobileNumber", fontSize = 12.sp, color = TextSecondary)
                                                    IconButton(
                                                        onClick = { mobileStep = MobileLoginStep.ENTER_NUMBER },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit Number", tint = AngelOrange, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                            IconButton(
                                                onClick = { showOtpHelpDialog = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.HelpOutline,
                                                    contentDescription = "OTP Help",
                                                    tint = AngelOrange,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        // Auto-Detecting SMS Status Chip
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (autoReadComplete) EmeraldGreen.copy(alpha = 0.12f) else AngelBlue.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, if (autoReadComplete) EmeraldGreen.copy(alpha = 0.4f) else AngelBlue.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                if (isAutoReadingSms) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(14.dp),
                                                            strokeWidth = 2.dp,
                                                            color = AngelBlue
                                                        )
                                                        Text("Auto-reading incoming SMS for +91 $mobileNumber...", fontSize = 11.sp, color = AngelBlue, fontWeight = FontWeight.Medium)
                                                    }
                                                } else if (autoReadComplete) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                                        Text("SMS OTP Auto-Detected: $activeGeneratedOtp", fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(
                                                        text = "Clear",
                                                        fontSize = 11.sp,
                                                        color = TextMuted,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.clickable {
                                                            mobileOtp = ""
                                                            autoReadComplete = false
                                                        }
                                                    )
                                                } else {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AngelOrange, modifier = Modifier.size(16.dp))
                                                        Text("Active Verification Code: $activeGeneratedOtp", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(
                                                        text = "⚡ 1-Tap Fill",
                                                        fontSize = 11.sp,
                                                        color = AngelOrange,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.clickable {
                                                            mobileOtp = activeGeneratedOtp
                                                            autoReadComplete = true
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // OTP Field
                                        OutlinedTextField(
                                            value = mobileOtp,
                                            onValueChange = { input ->
                                                if (input.length <= 6) {
                                                    mobileOtp = input.filter { it.isDigit() }
                                                }
                                            },
                                            label = { Text("6-Digit OTP") },
                                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = AngelOrange) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                fontSize = 22.sp,
                                                letterSpacing = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("angel_otp_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AngelOrange,
                                                unfocusedBorderColor = NavyBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            )
                                        )

                                        // Dedicated "SMS Not Receiving on Physical Phone?" Assistance Card
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF131D38),
                                            border = BorderStroke(1.dp, NavyBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Icon(Icons.Default.Smartphone, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                                                        Text("Didn't receive SMS on personal phone?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                    }
                                                    IconButton(
                                                        onClick = { showOtpHelpDialog = true },
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(Icons.Default.Info, contentDescription = "Why?", tint = TextMuted, modifier = Modifier.size(14.dp))
                                                    }
                                                }

                                                Text(
                                                    text = "In this cloud preview environment, carrier SMS is dispatched directly onto this device via Android System Notification & Auto-Detect!",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary,
                                                    lineHeight = 15.sp
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    // Quick Auto-Fill
                                                    OutlinedButton(
                                                        onClick = {
                                                            mobileOtp = activeGeneratedOtp
                                                            autoReadComplete = true
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.6f)),
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(13.dp))
                                                            Text("Auto-Fill ($activeGeneratedOtp)", fontSize = 10.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    // Send to WhatsApp
                                                    OutlinedButton(
                                                        onClick = {
                                                            OtpNotificationHelper.sendOtpViaWhatsApp(context, mobileNumber, activeGeneratedOtp)
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, AngelBlue.copy(alpha = 0.6f)),
                                                        modifier = Modifier.weight(1f).height(34.dp),
                                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = AngelBlue, modifier = Modifier.size(13.dp))
                                                            Text("Send to WhatsApp", fontSize = 10.sp, color = AngelBlue, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Resend Countdown Timer & WhatsApp Link
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isOtpTimerRunning) {
                                                Text(
                                                    text = "Resend OTP in ${otpTimerSeconds}s",
                                                    fontSize = 12.sp,
                                                    color = TextMuted
                                                )
                                            } else {
                                                TextButton(
                                                    onClick = {
                                                        val regenerated = OtpNotificationHelper.generateOtp()
                                                        activeGeneratedOtp = regenerated
                                                        OtpNotificationHelper.sendOtpNotification(context, mobileNumber, regenerated)
                                                        showIncomingSmsBanner = true
                                                        isAutoReadingSms = true
                                                        autoReadComplete = false
                                                        isOtpTimerRunning = true
                                                    },
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = AngelOrange, modifier = Modifier.size(14.dp))
                                                        Text("Resend SMS & Alert", fontSize = 12.sp, color = AngelOrange, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.clickable {
                                                    OtpNotificationHelper.sendOtpViaWhatsApp(context, mobileNumber, activeGeneratedOtp)
                                                }
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(13.dp))
                                                Text(
                                                    text = "Send to WhatsApp",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = EmeraldGreen
                                                )
                                            }
                                        }

                                        // Proceed to MPIN
                                        Button(
                                            onClick = {
                                                if (mobileOtp.length == 6) {
                                                    mobileStep = MobileLoginStep.ENTER_PIN
                                                } else {
                                                    viewModel.authErrorMessage.value = "Please enter complete 6-digit OTP"
                                                }
                                            },
                                            enabled = mobileOtp.length == 6 && !isLoading,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AngelOrange),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("angel_verify_otp_button")
                                        ) {
                                            Text("VERIFY OTP", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                        }
                                    }
                                }

                                MobileLoginStep.ENTER_PIN -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            IconButton(
                                                onClick = { mobileStep = MobileLoginStep.ENTER_OTP },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                                            }
                                            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                                Text("Enter 4-Digit Trading PIN (MPIN)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Text("Secure 2FA authentication for Angel One", fontSize = 11.sp, color = TextSecondary)
                                            }
                                        }

                                        // MPIN Field
                                        OutlinedTextField(
                                            value = mobilePin,
                                            onValueChange = { input ->
                                                if (input.length <= 4) {
                                                    mobilePin = input.filter { it.isDigit() }
                                                }
                                            },
                                            label = { Text("4-Digit MPIN") },
                                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AngelOrange) },
                                            visualTransformation = if (mpinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                            trailingIcon = {
                                                IconButton(onClick = { mpinVisible = !mpinVisible }) {
                                                    Icon(
                                                        imageVector = if (mpinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                        contentDescription = "Toggle PIN visibility",
                                                        tint = TextMuted
                                                    )
                                                }
                                            },
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                fontSize = 22.sp,
                                                letterSpacing = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("angel_mpin_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AngelOrange,
                                                unfocusedBorderColor = NavyBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            )
                                        )

                                        // Forgot MPIN and Biometric Toggle
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(18.dp))
                                                Text("Biometric Login", fontSize = 12.sp, color = TextSecondary)
                                                Switch(
                                                    checked = enableBiometric,
                                                    onCheckedChange = { enableBiometric = it },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color.White,
                                                        checkedTrackColor = SleekLavender
                                                    ),
                                                    modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                                )
                                            }

                                            Text(
                                                text = "Forgot PIN / MPIN?",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AngelOrange,
                                                modifier = Modifier.clickable { showForgotMpinDialog = true }
                                            )
                                        }

                                        // LOG IN TO ANGEL ONE Button
                                        Button(
                                            onClick = {
                                                viewModel.loginWithAngelOneMobile(
                                                    mobile = mobileNumber,
                                                    otp = mobileOtp.ifBlank { activeGeneratedOtp },
                                                    mpin = mobilePin.ifBlank { "1234" },
                                                    onSuccess = onAuthSuccess
                                                )
                                            },
                                            enabled = mobilePin.length == 4 && !isLoading,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AngelOrange),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("angel_login_submit_button")
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                    Text("LOG IN TO ANGEL ONE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ----------------------------------------------------
                        // TAB 2: CLIENT ID / SMARTAPI CREDENTIALS
                        // ----------------------------------------------------
                        AngelLoginTab.CLIENT_ID -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "SmartAPI Trader Login",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Angel One Client Code & SmartAPI Credentials",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    // One-tap quick demo login
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AngelOrange.copy(alpha = 0.15f),
                                        modifier = Modifier.clickable {
                                            clientCode = "R982734"
                                            mpinOrPassword = "1234"
                                            totpCode = "729410"
                                            apiKey = "s6BvE892YqW91"
                                        }
                                    ) {
                                        Text(
                                            "⚡ Auto-fill",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AngelOrange,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Client ID Field
                                OutlinedTextField(
                                    value = clientCode,
                                    onValueChange = { clientCode = it.uppercase() },
                                    label = { Text("Angel One Client ID / Code") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AngelOrange) },
                                    placeholder = { Text("e.g. R982734") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Characters,
                                        keyboardType = KeyboardType.Text
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("angel_client_id_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AngelOrange,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                // Password / MPIN Field
                                OutlinedTextField(
                                    value = mpinOrPassword,
                                    onValueChange = { mpinOrPassword = it },
                                    label = { Text("4-Digit PIN / Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AngelOrange) },
                                    visualTransformation = if (mpinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = { mpinVisible = !mpinVisible }) {
                                            Icon(
                                                imageVector = if (mpinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password visibility",
                                                tint = TextMuted
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("angel_password_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AngelOrange,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                // 6-digit TOTP / 2FA Code Field
                                OutlinedTextField(
                                    value = totpCode,
                                    onValueChange = { input ->
                                        if (input.length <= 6) {
                                            totpCode = input.filter { it.isDigit() }
                                        }
                                    },
                                    label = { Text("6-Digit TOTP / Authenticator Code") },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = SleekLavender) },
                                    placeholder = { Text("Google Authenticator / SMS") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 18.sp,
                                        letterSpacing = 4.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("angel_totp_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AngelOrange,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                // Recovery Links (Forgot Client ID? & Forgot Password?)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Forgot Client ID?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AngelBlue,
                                        modifier = Modifier.clickable { showForgotClientIdDialog = true }
                                    )

                                    Text(
                                        text = "Forgot PIN / Password?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AngelOrange,
                                        modifier = Modifier.clickable { showForgotMpinDialog = true }
                                    )
                                }

                                // Expandable SmartAPI Developer Settings
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = NavyCard,
                                    border = BorderStroke(1.dp, NavyBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showSmartApiSettings = !showSmartApiSettings }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.Code, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(16.dp))
                                                Text("SmartAPI Algorithmic Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            }
                                            Text(
                                                text = if (showSmartApiSettings) "Hide" else "Configure",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AngelOrange
                                            )
                                        }

                                        if (showSmartApiSettings) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            OutlinedTextField(
                                                value = apiKey,
                                                onValueChange = { apiKey = it },
                                                label = { Text("SmartAPI Key") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = AngelOrange,
                                                    unfocusedBorderColor = NavyBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                "Pre-configured with Angel One live sandbox endpoints.",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                // Quick 1-Tap Demo Login Button
                                OutlinedButton(
                                    onClick = {
                                        viewModel.loginWithDematAccount(
                                            brokerType = selectedBroker,
                                            clientCode = clientCode.ifBlank { "R982734" },
                                            passwordOrMpin = "1234",
                                            totp = "729410",
                                            apiKey = apiKey.ifBlank { "s6BvE892YqW91" },
                                            onSuccess = onAuthSuccess
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, AngelOrange.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AngelOrange),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("angel_quick_demo_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("⚡ Quick 1-Tap ${selectedBroker.displayName.split(" ")[0]} Demat Login", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                // Primary Login Button
                                Button(
                                    onClick = {
                                        viewModel.loginWithDematAccount(
                                            brokerType = selectedBroker,
                                            clientCode = clientCode,
                                            passwordOrMpin = mpinOrPassword,
                                            totp = totpCode,
                                            apiKey = apiKey,
                                            onSuccess = onAuthSuccess
                                        )
                                    },
                                    enabled = clientCode.isNotBlank() && mpinOrPassword.isNotBlank() && !isLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AngelOrange),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("angel_client_login_button")
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Text("LOG IN TO ANGEL ONE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // ----------------------------------------------------
                        // TAB 3: EMAIL & PASSWORD LOGIN FLOW
                        // ----------------------------------------------------
                        AngelLoginTab.EMAIL -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Email & Password Login",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Enter your registered email and password",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SleekLavender.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            "BCrypt + JWT",
                                            fontSize = 10.sp,
                                            color = SleekLavender,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                // Quick Auto-fill button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF162348),
                                    border = BorderStroke(1.dp, SleekLavender.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            emailAddress = "rakeshnironi@gmail.com"
                                            emailPassword = "Password@123"
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(14.dp))
                                            Text("Fill: rakeshnironi@gmail.com / Password@123", fontSize = 11.sp, color = TextPrimary)
                                        }
                                        Text("Auto-fill", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedTextField(
                                    value = emailAddress,
                                    onValueChange = { emailAddress = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SleekLavender) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    modifier = Modifier.fillMaxWidth().testTag("email_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SleekLavender,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                OutlinedTextField(
                                    value = emailPassword,
                                    onValueChange = { emailPassword = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SleekLavender) },
                                    trailingIcon = {
                                        IconButton(onClick = { emailPasswordVisible = !emailPasswordVisible }) {
                                            Icon(
                                                imageVector = if (emailPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (emailPasswordVisible) "Hide Password" else "Show Password",
                                                tint = TextMuted
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (emailPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    modifier = Modifier.fillMaxWidth().testTag("password_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SleekLavender,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { rememberEmail = !rememberEmail }
                                    ) {
                                        Checkbox(
                                            checked = rememberEmail,
                                            onCheckedChange = { rememberEmail = it },
                                            colors = CheckboxDefaults.colors(checkedColor = SleekLavender)
                                        )
                                        Text("Remember login", fontSize = 12.sp, color = TextSecondary)
                                    }

                                    TextButton(
                                        onClick = { showForgotMpinDialog = true },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("Forgot Password?", fontSize = 12.sp, color = SleekLavender, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.login(
                                            email = emailAddress.trim(),
                                            password = emailPassword,
                                            onSuccess = onAuthSuccess
                                        )
                                    },
                                    enabled = emailAddress.isNotBlank() && emailPassword.isNotBlank() && !isLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekLavender, contentColor = SleekDarkPurple),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("email_login_button")
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = SleekDarkPurple, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("LOG IN WITH EMAIL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // ----------------------------------------------------
                        // TAB 4: OPEN FREE DEMAT ACCOUNT (REGISTER)
                        // ----------------------------------------------------
                        AngelLoginTab.OPEN_ACCOUNT -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Column {
                                    Text(
                                        text = "Open Free Demat & Trading Account",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Zero AMC for 1st Year • Flat ₹20 Brokerage",
                                        fontSize = 12.sp,
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Benefits Chips Grid
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AngelOrange.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, AngelOrange.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                            Text("₹0 Account Opening Fee & Free AMC (1st Year)", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                            Text("₹0 Brokerage on Equity Delivery Trades", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                            Text("Flat ₹20 / order on Intraday, F&O & Commodities", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                            Text("Instant ₹1,00,000 Virtual Margin & SmartAPI Access", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                // Registration Inputs
                                OutlinedTextField(
                                    value = registerFullName,
                                    onValueChange = { registerFullName = it },
                                    label = { Text("Full Name (as per PAN)") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AngelOrange) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AngelOrange,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                OutlinedTextField(
                                    value = registerMobile,
                                    onValueChange = { input ->
                                        if (input.length <= 10) registerMobile = input.filter { it.isDigit() }
                                    },
                                    label = { Text("Mobile Number (+91)") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldGreen) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AngelOrange,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                OutlinedTextField(
                                    value = registerEmail,
                                    onValueChange = { registerEmail = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AngelBlue) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AngelOrange,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = registerCity,
                                        onValueChange = { registerCity = it },
                                        label = { Text("City") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AngelOrange,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )

                                    OutlinedTextField(
                                        value = registerReferralCode,
                                        onValueChange = { registerReferralCode = it.uppercase() },
                                        label = { Text("Referral (Opt)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AngelOrange,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                }

                                // Open Account Button
                                Button(
                                    onClick = {
                                        viewModel.register(
                                            name = registerFullName,
                                            email = registerEmail,
                                            phone = registerMobile,
                                            password = "Password@123",
                                            onSuccess = {
                                                viewModel.loginWithAngelOne(
                                                    clientCode = "R" + registerMobile.takeLast(6),
                                                    passwordOrMpin = "1234",
                                                    totp = "729410",
                                                    apiKey = "s6BvE892YqW91",
                                                    onSuccess = onAuthSuccess
                                                )
                                            }
                                        )
                                    },
                                    enabled = registerFullName.isNotBlank() && registerMobile.length == 10 && !isLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AngelOrange),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("angel_open_account_button")
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("OPEN FREE DEMAT ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // ANGEL ONE REGULATORY & TRUST FOOTER
            // ==========================================
            AngelOneSecurityTrustFooter()
        }
    }

    // ==========================================
    // FORGOT MPIN DIALOG
    // ==========================================
    if (showForgotMpinDialog) {
        AlertDialog(
            onDismissRequest = { showForgotMpinDialog = false },
            containerColor = NavySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AngelOrange)
                    Text("Reset Angel One MPIN", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "To reset your 4-digit Trading PIN (MPIN), you can verify using your Registered Mobile (+91 $mobileNumber) or PAN Card.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AngelOrange.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AngelOrange.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Default sandbox test MPIN is '1234'. Enter this to proceed immediately.",
                            fontSize = 11.sp,
                            color = AngelOrange,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mobilePin = "1234"
                        showForgotMpinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AngelOrange)
                ) {
                    Text("Auto-fill MPIN: 1234", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotMpinDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // ==========================================
    // FORGOT CLIENT ID DIALOG
    // ==========================================
    if (showForgotClientIdDialog) {
        AlertDialog(
            onDismissRequest = { showForgotClientIdDialog = false },
            containerColor = NavySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AngelBlue)
                    Text("Find Your Client ID", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your 7-character Angel One Client ID (e.g. R982734) can be found in:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text("• SMS from 'ANGELB' sent during registration", fontSize = 12.sp, color = TextPrimary)
                    Text("• Welcome Email kit sent to your email address", fontSize = 12.sp, color = TextPrimary)
                    Text("• Monthly Demat Account Statement & Contract Notes", fontSize = 12.sp, color = TextPrimary)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AngelBlue.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AngelBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ Default demo Client ID is 'R982734'.",
                            fontSize = 11.sp,
                            color = AngelBlue,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clientCode = "R982734"
                        showForgotClientIdDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AngelBlue)
                ) {
                    Text("Use Demo ID: R982734", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotClientIdDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }

    // ==========================================
    // SMARTAPI 2FA GUIDE DIALOG
    // ==========================================
    if (showSmartApiGuideDialog) {
        AlertDialog(
            onDismissRequest = { showSmartApiGuideDialog = false },
            containerColor = NavySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SleekLavender)
                    Text("SmartAPI & 2FA TOTP Guide", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Angel One uses SmartAPI V2 with mandatory Time-based One-Time Password (TOTP) 2FA:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text("1. Log in to Angel One web portal (trade.angelone.in)", fontSize = 11.sp, color = TextPrimary)
                    Text("2. Navigate to Profile > Settings > Enable TOTP", fontSize = 11.sp, color = TextPrimary)
                    Text("3. Scan the QR code using Google Authenticator or Microsoft Authenticator", fontSize = 11.sp, color = TextPrimary)
                    Text("4. Enter the generated 6-digit TOTP code during login", fontSize = 11.sp, color = TextPrimary)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldGreen.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 For sandbox testing, use TOTP: 729410",
                            fontSize = 11.sp,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        totpCode = "729410"
                        showSmartApiGuideDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekLavender)
                ) {
                    Text("Fill Demo TOTP: 729410", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmartApiGuideDialog = false }) {
                    Text("Got It", color = TextMuted)
                }
            }
        )
    }

    // ==========================================
    // OTP DELIVERY & HELP DIALOG
    // ==========================================
    if (showOtpHelpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpHelpDialog = false },
            containerColor = NavySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Smartphone, contentDescription = null, tint = AngelOrange)
                    Text("OTP Delivery & Verification", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your verification code has been generated for +91 $mobileNumber:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B284D),
                        border = BorderStroke(1.dp, AngelOrange.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Current OTP: $activeGeneratedOtp", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AngelOrange)
                            Text("1. Pull down your Android Status Bar at the top of your screen to see the official Angel One notification.", fontSize = 11.sp, color = TextPrimary)
                            Text("2. Tap 'Auto-Fill' to populate the OTP into the field automatically.", fontSize = 11.sp, color = TextPrimary)
                            Text("3. Tap 'Send to WhatsApp' to receive the OTP on your real WhatsApp messenger.", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mobileOtp = activeGeneratedOtp
                        autoReadComplete = true
                        showOtpHelpDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AngelOrange)
                ) {
                    Text("Auto-Fill $activeGeneratedOtp", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpHelpDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }
}

/**
 * Official Angel One Brand Header with custom Vector Spark Emblem
 */
@Composable
private fun AngelOneBrandHeader(
    onHelpClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Angel One Emblem + Wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Angel One Signature Spark / Wings Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AngelNavy,
                    border = BorderStroke(1.5.dp, AngelOrange.copy(alpha = 0.5f)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(26.dp)) {
                            val w = size.width
                            val h = size.height

                            // Left Orange Wing
                            val pathOrange = Path().apply {
                                moveTo(w * 0.45f, 0f)
                                lineTo(0f, h * 0.5f)
                                lineTo(w * 0.45f, h)
                                lineTo(w * 0.25f, h * 0.5f)
                                close()
                            }
                            drawPath(pathOrange, color = AngelOrange)

                            // Right Blue Wing
                            val pathBlue = Path().apply {
                                moveTo(w * 0.55f, 0f)
                                lineTo(w, h * 0.5f)
                                lineTo(w * 0.55f, h)
                                lineTo(w * 0.75f, h * 0.5f)
                                close()
                            }
                            drawPath(pathBlue, color = AngelBlue)
                        }
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Angel",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "One",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AngelOrange
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AngelOrange.copy(alpha = 0.15f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                "SuperApp",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AngelOrange,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "SmartAPI V2 Automated Trading",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SleekLavender
                    )
                }
            }

            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Help Guide",
                    tint = AngelOrange,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tagline & Value Prop
        Text(
            text = "Invest & Trade in Stocks, F&O, Mutual Funds & Commodities",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldGreen.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(11.dp))
                    Text("2.5+ Crore Indians", fontSize = 10.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = AngelBlue.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AngelBlue, modifier = Modifier.size(11.dp))
                    Text("SEBI: INZ000161534", fontSize = 10.sp, color = AngelBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * Official Angel One Security, SEBI Compliance & Customer Care Footer
 */
@Composable
private fun AngelOneSecurityTrustFooter() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(14.dp))
            Text(
                text = "256-Bit Bank Grade SSL Encryption • 2FA Compliant",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        Text(
            text = "NSE: 12707 | BSE: 612 | MCX: 12685 | CDSL Depository",
            fontSize = 10.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Angel One Support: 080-47480048 • support@angelbroking.com",
            fontSize = 10.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
