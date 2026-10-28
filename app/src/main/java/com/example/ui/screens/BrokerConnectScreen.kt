package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.dto.BrokerAccountInfo
import com.example.data.remote.dto.BrokerSessionState
import com.example.data.remote.dto.BrokerType
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrokerConnectScreen(
    viewModel: TradingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val sessionState by viewModel.brokerSessionState.collectAsState()
    val accountInfo by viewModel.brokerAccountInfo.collectAsState()
    val lastAuthError by viewModel.brokerAuthError.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val brokerTabs = listOf("Angel One", "Zerodha", "Groww / Upstox")

    // Form inputs
    var clientCode by remember(profile) { mutableStateOf(profile?.angelClientId ?: "") }
    var passwordOrMpin by remember { mutableStateOf("") }
    var totpCode by remember { mutableStateOf("") }
    var apiKey by remember(profile) { mutableStateOf(profile?.angelApiKey ?: "") }
    var jwtTokenInput by remember { mutableStateOf("") }
    var feedTokenInput by remember { mutableStateOf("") }
    var authMethodIndex by remember { mutableIntStateOf(0) } // 0: MPIN + TOTP, 1: Direct Session JWT
    var passwordVisible by remember { mutableStateOf(false) }

    // Dialog states
    var showForgotCredentialsDialog by remember { mutableStateOf(false) }
    var showSmartApiGuideDialog by remember { mutableStateOf(false) }
    var showDisconnectConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("broker_connect_screen"),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("broker_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Column {
                        Text(
                            text = "Connect Broker Account",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Official SmartAPI V2 Authentication",
                            fontSize = 11.sp,
                            color = CyanAccent
                        )
                    }
                }

                IconButton(onClick = { showSmartApiGuideDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help Guide", tint = CyanAccent)
                }
            }
        }

        // Security Assurance Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Direct Official Broker Flow",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Your credentials authenticate directly with Angel One servers over TLS/HTTPS. Passwords and OTPs are used in-memory and never stored.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Active Connection / Expired / Disconnected State Content
        if (sessionState == BrokerSessionState.CONNECTED && accountInfo != null) {
            item {
                ConnectedBrokerDetailsCard(
                    account = accountInfo!!,
                    onDisconnectClick = { showDisconnectConfirmDialog = true },
                    onGoToDashboard = onNavigateToDashboard,
                    onSyncDataClick = { viewModel.syncBrokerData() }
                )
            }
        } else {
            // Session Expired Warning Banner if applicable
            if (sessionState == BrokerSessionState.EXPIRED) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AmberGold.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberGold, modifier = Modifier.size(22.dp))
                            Column {
                                Text(
                                    text = "Broker Session Expired",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                                Text(
                                    text = "Your 24-hour SmartAPI JWT token has expired. Enter a fresh 6-digit TOTP below to reconnect.",
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Broker Selection Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = NavyDeep,
                    contentColor = CyanAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = CyanAccent,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    brokerTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) CyanAccent else TextMuted
                                )
                            }
                        )
                    }
                }
            }

            // Tab 0: Angel One (Official SmartAPI V2)
            if (selectedTabIndex == 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = BorderStroke(1.dp, NavyBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Angel One SmartAPI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Official Token-based Login", fontSize = 11.sp, color = CyanAccent)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "LIVE API",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Demat Sync Information Banner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CyanAccent.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "How Demat Account Sync Works",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent
                                        )
                                    }
                                    Text(
                                        text = "To fetch your original Demat balance, profile, and stock holdings, Angel One SmartAPI requires your Client ID, API Key, and either your MPIN+TOTP or an active JWT Session Token.",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            // Auth Method Selector (MPIN+TOTP vs Direct JWT)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NavySurface, RoundedCornerShape(10.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { authMethodIndex = 0 },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (authMethodIndex == 0) CyanAccent else Color.Transparent,
                                        contentColor = if (authMethodIndex == 0) NavyDeep else TextSecondary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("MPIN & TOTP (2FA)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { authMethodIndex = 1 },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (authMethodIndex == 1) CyanAccent else Color.Transparent,
                                        contentColor = if (authMethodIndex == 1) NavyDeep else TextSecondary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Direct JWT Token", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Error Display
                            AnimatedVisibility(visible = lastAuthError != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CrimsonRed.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = lastAuthError ?: "Authentication error",
                                            fontSize = 12.sp,
                                            color = CrimsonRed
                                        )
                                    }
                                }
                            }

                            // Common Field: Client ID / Angel One Mobile Number
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Client ID / Angel One Client Code", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                OutlinedTextField(
                                    value = clientCode,
                                    onValueChange = { clientCode = it.uppercase() },
                                    placeholder = { Text("e.g. R982734 or D82914", color = TextMuted, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedContainerColor = NavySurface,
                                        unfocusedContainerColor = NavySurface,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("broker_client_id_input")
                                )
                            }

                            // Common Field: SmartAPI Key
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("SmartAPI Key (X-PrivateKey)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                    Text(
                                        text = "How to get key?",
                                        fontSize = 11.sp,
                                        color = SleekLavender,
                                        modifier = Modifier.clickable { showSmartApiGuideDialog = true }
                                    )
                                }
                                OutlinedTextField(
                                    value = apiKey,
                                    onValueChange = { apiKey = it },
                                    placeholder = { Text("e.g. s6BvE892YqW91", color = TextMuted, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = if (authMethodIndex == 0) ImeAction.Next else ImeAction.Done),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = NavyBorder,
                                        focusedContainerColor = NavySurface,
                                        unfocusedContainerColor = NavySurface,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("broker_api_key_input")
                                )
                            }

                            if (authMethodIndex == 0) {
                                // Password / MPIN Field
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("PIN / Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                        Text(
                                            text = "Forgot credentials?",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CyanAccent,
                                            modifier = Modifier
                                                .clickable { showForgotCredentialsDialog = true }
                                                .testTag("forgot_credentials_button")
                                        )
                                    }
                                    OutlinedTextField(
                                        value = passwordOrMpin,
                                        onValueChange = { passwordOrMpin = it },
                                        placeholder = { Text("Enter 4-digit MPIN or Password", color = TextMuted, fontSize = 13.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                Icon(
                                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = "Toggle visibility",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanAccent,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedContainerColor = NavySurface,
                                            unfocusedContainerColor = NavySurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("broker_password_input")
                                    )
                                }

                                // TOTP / 2FA OTP Field
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("6-Digit TOTP / Authenticator OTP", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                    OutlinedTextField(
                                        value = totpCode,
                                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) totpCode = it },
                                        placeholder = { Text("Enter 6-digit TOTP (e.g. 729410)", color = TextMuted, fontSize = 13.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Security, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = {
                                            viewModel.connectAngelOne(clientCode, passwordOrMpin, totpCode, apiKey)
                                        }),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanAccent,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedContainerColor = NavySurface,
                                            unfocusedContainerColor = NavySurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("broker_totp_input")
                                    )
                                    Text(
                                        text = "Generated in Google Authenticator or Angel One app",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            } else {
                                // Direct JWT Token Field
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("JWT Session Token (Bearer)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                        Text(
                                            text = "Instant Demat Sync",
                                            fontSize = 11.sp,
                                            color = EmeraldGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    OutlinedTextField(
                                        value = jwtTokenInput,
                                        onValueChange = { jwtTokenInput = it },
                                        placeholder = { Text("eyJhbGciOiJIUzUxMi...", color = TextMuted, fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Key, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                        },
                                        singleLine = false,
                                        maxLines = 3,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanAccent,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedContainerColor = NavySurface,
                                            unfocusedContainerColor = NavySurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("broker_jwt_token_input")
                                    )
                                    Text(
                                        text = "Paste the 24-hour session JWT token from SmartAPI developer dashboard or login response",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }

                                // Optional Feed Token Field
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Feed Token (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                    OutlinedTextField(
                                        value = feedTokenInput,
                                        onValueChange = { feedTokenInput = it },
                                        placeholder = { Text("Optional WebSocket live feed token", color = TextMuted, fontSize = 13.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Refresh, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(18.dp))
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = {
                                            viewModel.connectAngelOneWithJwt(clientCode, apiKey, jwtTokenInput, feedTokenInput)
                                        }),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanAccent,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedContainerColor = NavySurface,
                                            unfocusedContainerColor = NavySurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("broker_feed_token_input")
                                    )
                                }
                            }

                            // Helper Actions Row for Quick Fill & Sandbox
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        clientCode = "R982734"
                                        passwordOrMpin = "1234"
                                        totpCode = "729410"
                                        apiKey = "s6BvE892YqW91"
                                        authMethodIndex = 0
                                    },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fill Demo Sample", fontSize = 11.sp, color = SleekLavender)
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.connectDemoBroker(clientCode.ifBlank { "DEMO1001" })
                                    },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Try Demo Sandbox", fontSize = 11.sp, color = AmberGold)
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Connect Button
                            Button(
                                onClick = {
                                    if (authMethodIndex == 0) {
                                        viewModel.connectAngelOne(
                                            clientCode = clientCode,
                                            passwordOrMpin = passwordOrMpin,
                                            totpOrOtp = totpCode,
                                            apiKey = apiKey
                                        )
                                    } else {
                                        viewModel.connectAngelOneWithJwt(
                                            clientCode = clientCode,
                                            apiKey = apiKey,
                                            jwtToken = jwtTokenInput,
                                            feedToken = feedTokenInput
                                        )
                                    }
                                },
                                enabled = sessionState != BrokerSessionState.CONNECTING,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("connect_broker_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = NavyDeep)
                            ) {
                                if (sessionState == BrokerSessionState.CONNECTING) {
                                    CircularProgressIndicator(color = NavyDeep, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Fetching Demat Account...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (authMethodIndex == 0) "Connect & Fetch Demat Account" else "Authenticate with Session Token",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (selectedTabIndex == 1) {
                // Tab 1: Zerodha Kite Connect
                item {
                    OtherBrokerConnectCard(
                        brokerName = "Zerodha Kite Connect",
                        apiPortalUrl = "https://kite.trade",
                        description = "Zerodha Kite Connect uses standard API key & API secret OAuth redirect flows. Connect to execute paper and live equity trades.",
                        onConnect = { id, key, secret ->
                            viewModel.connectOtherBroker(BrokerType.ZERODHA, id, key, secret)
                        },
                        isConnecting = sessionState == BrokerSessionState.CONNECTING
                    )
                }
            } else {
                // Tab 2: Groww / Upstox
                item {
                    OtherBrokerConnectCard(
                        brokerName = "Upstox / Groww Open API",
                        apiPortalUrl = "https://upstox.com/developer/api-documentation/",
                        description = "Integrate your Upstox or Groww trading account via official developer tokens for algorithmic order execution and real-time portfolio tracking.",
                        onConnect = { id, key, secret ->
                            viewModel.connectOtherBroker(BrokerType.UPSTOX, id, key, secret)
                        },
                        isConnecting = sessionState == BrokerSessionState.CONNECTING
                    )
                }
            }
        }
    }

    // Forgot Credentials Dialog
    if (showForgotCredentialsDialog) {
        AlertDialog(
            onDismissRequest = { showForgotCredentialsDialog = false },
            containerColor = NavyCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CyanAccent)
                    Text("Forgot Angel One Credentials", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "To reset your MPIN, retrieve your Client ID, or re-enable 2FA Authenticator, please visit the official Angel One Trade Portal.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "1. MPIN Reset: Use the 'Forgot PIN' link on the Angel One mobile app.\n2. Client Code: Check your welcome email or account statements from Angel One.\n3. TOTP: Regenerate your QR code in Profile -> Security & TOTP on Angel One.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotCredentialsDialog = false
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://trade.angelone.in/forgot-login-details"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignored
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = NavyDeep)
                ) {
                    Text("Open Angel One Portal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotCredentialsDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }

    // SmartAPI Credentials Setup Guide Dialog
    if (showSmartApiGuideDialog) {
        AlertDialog(
            onDismissRequest = { showSmartApiGuideDialog = false },
            containerColor = NavyCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent)
                    Text("SmartAPI Setup Guide", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(280.dp)) {
                    item {
                        Text(
                            text = "How to connect with Official Angel One SmartAPI:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    item {
                        StepItem(number = "1", title = "Create SmartAPI Account", desc = "Go to https://smartapi.angelbroking.com and register using your Angel One Client ID.")
                    }
                    item {
                        StepItem(number = "2", title = "Create an API App", desc = "Click 'Create an App', select 'Trading API', and name it (e.g. 'AIMLTradingApp').")
                    }
                    item {
                        StepItem(number = "3", title = "Copy API Key (X-PrivateKey)", desc = "Copy the generated API Key and paste it into the SmartAPI Key input field.")
                    }
                    item {
                        StepItem(number = "4", title = "Enable TOTP / 2FA", desc = "Enable Authenticator App (Google Authenticator) inside Angel One security settings.")
                    }
                    item {
                        StepItem(number = "5", title = "Authenticate", desc = "Enter your Client ID, MPIN, and 6-digit live TOTP to initiate secure trading.")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSmartApiGuideDialog = false
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://smartapi.angelbroking.com"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignored
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = NavyDeep)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Open SmartAPI Portal")
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmartApiGuideDialog = false }) {
                    Text("Got it", color = TextSecondary)
                }
            }
        )
    }

    // Disconnect Confirmation Dialog
    if (showDisconnectConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectConfirmDialog = false },
            containerColor = NavyCard,
            title = {
                Text("Disconnect Broker Account?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "This will immediately revoke active JWT session tokens and disconnect your live trading gateway. You can reconnect anytime.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisconnectConfirmDialog = false
                        viewModel.disconnectBroker()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_disconnect_button")
                ) {
                    Text("Disconnect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConnectedBrokerDetailsCard(
    account: BrokerAccountInfo,
    onDisconnectClick: () -> Unit,
    onGoToDashboard: () -> Unit,
    onSyncDataClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text("Broker Connected", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(account.brokerName, fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(EmeraldGreen, CircleShape)
                        )
                        Text("ACTIVE LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                }
            }

            // Live RMS Funds & Portfolio Highlights from API Key
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live Angel One RMS & Portfolio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        IconButton(
                            onClick = onSyncDataClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Data", tint = CyanAccent, modifier = Modifier.size(18.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Available Funds", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", account.availableCash)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Holdings Count", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "${account.holdingsCount} stocks",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    if (account.totalHoldingValue > 0.0 || account.totalHoldingPnL != 0.0) {
                        HorizontalDivider(color = NavyBorder.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Holdings Value", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = "₹${String.format(Locale.US, "%,.2f", account.totalHoldingValue)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Holdings P&L", fontSize = 11.sp, color = TextMuted)
                                val isProfit = account.totalHoldingPnL >= 0
                                Text(
                                    text = "${if (isProfit) "+" else ""}₹${String.format(Locale.US, "%,.2f", account.totalHoldingPnL)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isProfit) EmeraldGreen else CrimsonRed
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = NavyBorder)

            // Account Details
            Text("Authorized Account Profile", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)

            DetailRow(label = "Account Holder", value = account.holderName)
            DetailRow(label = "Client Code", value = account.clientCode)
            DetailRow(label = "Registered Email", value = account.email)
            DetailRow(label = "Registered Mobile", value = account.mobile)
            DetailRow(label = "Brokerage Group", value = account.brokerageGroup)

            // Active Exchanges
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Authorized Trading Exchanges", fontSize = 11.sp, color = TextMuted)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    account.exchanges.forEach { ex ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NavySurface,
                            border = BorderStroke(1.dp, NavyBorder)
                        ) {
                            Text(
                                text = ex,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = NavyBorder)

            // Session Security Info
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Session & Token Security", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                DetailRow(label = "Session JWT", value = account.jwtTokenMasked)
                DetailRow(label = "Feed Token", value = account.feedTokenMasked)
                DetailRow(label = "Connected At", value = dateFormat.format(Date(account.connectedAt)))
                DetailRow(label = "Session Expiry", value = dateFormat.format(Date(account.expiresAt)))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDisconnectClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("disconnect_broker_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CrimsonRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSyncDataClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("sync_broker_data_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyanAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Live", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onGoToDashboard,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(46.dp)
                        .testTag("go_to_dashboard_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = NavyDeep)
                ) {
                    Text("Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun StepItem(number: String, title: String, desc: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Surface(
            shape = CircleShape,
            color = CyanAccent.copy(alpha = 0.2f),
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(desc, fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun OtherBrokerConnectCard(
    brokerName: String,
    apiPortalUrl: String,
    description: String,
    onConnect: (id: String, key: String, secret: String) -> Unit,
    isConnecting: Boolean
) {
    val context = LocalContext.current
    var clientId by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var apiSecret by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(brokerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(description, fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)

            OutlinedTextField(
                value = clientId,
                onValueChange = { clientId = it },
                label = { Text("Client ID / User ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("App API Key") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = apiSecret,
                onValueChange = { apiSecret = it },
                label = { Text("API Secret / PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { onConnect(clientId, apiKey, apiSecret) },
                enabled = !isConnecting && clientId.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = NavyDeep)
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NavyDeep)
                } else {
                    Text("Connect $brokerName", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apiPortalUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Ignored
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Visit Developer Portal", color = SleekLavender, fontSize = 12.sp)
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
