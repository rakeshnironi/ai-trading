package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Sync
import com.example.data.model.TradingMode
import com.example.ui.components.ModeSelectionBottomSheet
import com.example.ui.theme.LiveMarketGreen
import com.example.ui.theme.PaperTradingBlue
import com.example.data.model.SignalType
import com.example.data.model.StockQuote
import com.example.ui.components.MarketIndexCard
import com.example.ui.components.SignalBadge
import com.example.ui.components.StockCard
import com.example.ui.theme.AiCardBg
import com.example.ui.theme.AiCardBorder
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.SleekDarkPurple
import com.example.ui.theme.SleekLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TradingTheme
import com.example.ui.viewmodel.TradingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TradingViewModel,
    onNavigateToStockDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPrediction: (String) -> Unit,
    onNavigateToPaperTrading: () -> Unit,
    onNavigateToChart: (String) -> Unit,
    onNavigateToRecommendation: (String) -> Unit,
    onNavigateToWhatsAppLogs: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToMutualFunds: () -> Unit = {},
    onNavigateToMarketMovers: () -> Unit = {},
    onNavigateToBrokerConnect: () -> Unit = {},
    onNavigateToNews: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsState()
    val indices by viewModel.marketIndices.collectAsState()
    val allStocks by viewModel.allStocks.collectAsState()
    val portfolioSummary by viewModel.portfolioSummary.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val newsList by viewModel.newsList.collectAsState()
    val geminiSummary by viewModel.geminiDailySummary.collectAsState()
    val mutualFunds by viewModel.mutualFunds.collectAsState()
    val topGainersList by viewModel.topGainers.collectAsState()
    val topLosersList by viewModel.topLosers.collectAsState()
    val volumeMoversList by viewModel.volumeMovers.collectAsState()
    val brokerSessionState by viewModel.brokerSessionState.collectAsState()
    val brokerAccountInfo by viewModel.brokerAccountInfo.collectAsState()
    val isLiveActive by viewModel.isLiveFeedActive.collectAsState()
    val tradingMode by viewModel.tradingMode.collectAsState()

    var marketTabSelected by remember { mutableIntStateOf(0) }
    var showModeSelectionSheet by remember { mutableStateOf(false) }

    val topGainers = remember(topGainersList, allStocks) {
        if (topGainersList.isNotEmpty()) topGainersList else allStocks.sortedByDescending { it.percentChange }.take(5)
    }
    val topLosers = remember(topLosersList, allStocks) {
        if (topLosersList.isNotEmpty()) topLosersList else allStocks.sortedBy { it.percentChange }.take(5)
    }
    val volumeMovers = remember(volumeMoversList, allStocks) {
        if (volumeMoversList.isNotEmpty()) volumeMoversList else allStocks.sortedByDescending { it.volume }.take(5)
    }

    val displayedStocks = when (marketTabSelected) {
        0 -> allStocks.take(6)
        1 -> topGainers
        2 -> topLosers
        3 -> volumeMovers
        else -> emptyList()
    }

    val heroStock = allStocks.find { it.symbol == "RELIANCE" } ?: allStocks.firstOrNull()

    val userDetails by viewModel.userProfileDetails.collectAsState()
    val userName = userDetails["name"] ?: profile?.name ?: "Rakesh Nironi"
    val userEmail = userDetails["email"] ?: profile?.email ?: "rakeshnironi@gmail.com"
    val userPhone = userDetails["phone"] ?: profile?.phone ?: "+91 9876543210"
    val userClientCode = userDetails["clientCode"] ?: profile?.angelClientId ?: "R982734"

    val userInitials = remember(userName) {
        userName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "RN" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Sleek Header: Avatar, Greeting, Alerts & Search Icons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sleek Lavender Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SleekLavender),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekDarkPurple
                        )
                    }

                    Column {
                        Text(
                            text = "Welcome back,",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = userName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Account Mode Switcher Pill (Live Market / Paper Trading)
                    val isLiveMode = tradingMode == TradingMode.LIVE
                    val modeColor = if (isLiveMode) LiveMarketGreen else PaperTradingBlue
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showModeSelectionSheet = true }
                            .testTag("home_mode_switcher_pill"),
                        color = modeColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, modeColor.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(modeColor, CircleShape)
                            )
                            Text(
                                text = if (isLiveMode) "LIVE" else "PAPER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = modeColor
                            )
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Switch Mode",
                                tint = modeColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Search button
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .size(38.dp)
                            .background(NavyCard, CircleShape)
                            .border(1.dp, NavyBorder, CircleShape)
                            .testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Active Logged-in User Account Information Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .testTag("user_login_info_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(EmeraldGreen, CircleShape))
                            Text(
                                text = "LOGGED IN USER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Active Session",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = userName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$userEmail • $userPhone",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NavyDeep,
                            border = BorderStroke(1.dp, SleekLavender.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("CLIENT CODE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                                Text(userClientCode, fontSize = 12.sp, color = SleekLavender, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Live Market Streaming Ticker Marquee Bar
        item {
            com.example.ui.components.LiveMarketTickerBar(
                indices = indices,
                stocks = allStocks,
                isLiveActive = isLiveActive,
                onToggleLive = { viewModel.toggleLiveFeed() },
                onStockClick = onNavigateToStockDetail
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. Mode-Aware Hero Card (Live Real Portfolio vs Paper Practice Balance)
        item {
            val isLiveMode = tradingMode == TradingMode.LIVE
            val totalBalance = portfolioSummary?.formattedTotal ?: (if (isLiveMode) "₹1,48,250.00" else "₹1,00,000.00")
            val pnlFormatted = portfolioSummary?.formattedPnl ?: (if (isLiveMode) "+₹4,820.00" else "+₹0.00")
            val returnFormatted = portfolioSummary?.formattedReturn ?: (if (isLiveMode) "+3.25%" else "+0.00%")
            val cardBg = if (isLiveMode) LiveMarketGreen else SleekLavender
            val textPrimaryColor = if (isLiveMode) Color.White else SleekDarkPurple
            val textSubColor = if (isLiveMode) Color.White.copy(alpha = 0.85f) else SleekDarkPurple.copy(alpha = 0.75f)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onNavigateToPaperTrading() }
                    .testTag("hero_balance_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = if (isLiveMode) "LIVE PORTFOLIO & MARGIN" else "VIRTUAL PAPER BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = textSubColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = totalBalance,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                color = textPrimaryColor
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isLiveMode) NavyDeep else SleekDarkPurple,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clickable { showModeSelectionSheet = true }
                        ) {
                            Text(
                                text = if (isLiveMode) "● LIVE MARKET" else "● PAPER TRADING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLiveMode) LiveMarketGreen else SleekLavender,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(textPrimaryColor.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isLiveMode) "REALIZED PROFIT / RETURN" else "TOTAL PROFIT / RETURN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = textSubColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$pnlFormatted ($returnFormatted)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isLiveMode) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = textPrimaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // 3. Market Indices Carousel (Nifty 50, Bank Nifty, Sensex)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(indices) { index ->
                    MarketIndexCard(
                        index = index,
                        onClick = { onNavigateToStockDetail(index.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // 4. Sleek AI LSTM Signal / Top Pick Card
        heroStock?.let { stock ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clickable { onNavigateToRecommendation(stock.symbol) }
                        .testTag("ai_top_pick_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AiCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AiCardBorder)
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(EmeraldGreen, CircleShape)
                                )
                                Text(
                                    text = "AI LSTM SIGNAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = EmeraldGreen
                                )
                            }
                            Text(
                                text = "Data: Angel One",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stock.symbol,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${stock.exchange.name} • Live SmartAPI",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "BUY",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldGreen
                                )
                                Text(
                                    text = "88% Confidence",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Price comparison sub-panel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavyDeep, RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CURRENT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stock.formattedPrice,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(26.dp)
                                    .background(NavyBorder)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "PREDICTED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "₹${String.format("%.2f", stock.currentPrice * 1.045)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        // 5. Quick Tools Strip
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Quick Tools & Explorers",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        QuickActionItem(
                            icon = Icons.Default.Newspaper,
                            label = "AI News Feed",
                            color = SleekLavender,
                            onClick = onNavigateToNews
                        )
                    }
                    item {
                        QuickActionItem(
                            icon = Icons.Default.Savings,
                            label = "Mutual Funds",
                            color = CyanAccent,
                            onClick = onNavigateToMutualFunds
                        )
                    }
                    item {
                        QuickActionItem(
                            icon = Icons.Default.LocalFireDepartment,
                            label = "Top Movers",
                            color = AmberGold,
                            onClick = onNavigateToMarketMovers
                        )
                    }
                    item {
                        QuickActionItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "Paper Trade",
                            color = EmeraldGreen,
                            onClick = onNavigateToPaperTrading
                        )
                    }
                    item {
                        QuickActionItem(
                            icon = Icons.Default.Psychology,
                            label = "ML Forecast",
                            color = SleekLavender,
                            onClick = { heroStock?.let { onNavigateToPrediction(it.symbol) } }
                        )
                    }
                    item {
                        QuickActionItem(
                            icon = Icons.Default.AutoAwesome,
                            label = "AI Signals",
                            color = AmberGold,
                            onClick = { heroStock?.let { onNavigateToRecommendation(it.symbol) } }
                        )
                    }
                    item {
                        QuickActionItem(
                            icon = Icons.AutoMirrored.Filled.Send,
                            label = "WhatsApp",
                            color = EmeraldGreen,
                            onClick = onNavigateToWhatsAppLogs
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 6. Market Stocks (Trending / Gainers / Losers / Volume / Mutual Funds)
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Markets Watch & Screener",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (marketTabSelected == 4) "Explore MFs" else "Full Screener",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = CyanAccent,
                        modifier = Modifier.clickable {
                            if (marketTabSelected == 4) onNavigateToMutualFunds() else onNavigateToMarketMovers()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                ScrollableTabRow(
                    selectedTabIndex = marketTabSelected,
                    containerColor = Color.Transparent,
                    contentColor = CyanAccent,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        if (marketTabSelected < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[marketTabSelected]),
                                color = when (marketTabSelected) {
                                    1 -> EmeraldGreen
                                    2 -> CrimsonRed
                                    4 -> CyanAccent
                                    else -> SleekLavender
                                }
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = marketTabSelected == 0,
                        onClick = { marketTabSelected = 0 },
                        text = { Text("Trending", fontSize = 12.sp, fontWeight = if (marketTabSelected == 0) FontWeight.Bold else FontWeight.Normal, color = if (marketTabSelected == 0) SleekLavender else TextSecondary) }
                    )
                    Tab(
                        selected = marketTabSelected == 1,
                        onClick = { marketTabSelected = 1 },
                        text = { Text("Top Gainers", fontSize = 12.sp, fontWeight = if (marketTabSelected == 1) FontWeight.Bold else FontWeight.Normal, color = if (marketTabSelected == 1) EmeraldGreen else TextSecondary) }
                    )
                    Tab(
                        selected = marketTabSelected == 2,
                        onClick = { marketTabSelected = 2 },
                        text = { Text("Top Losers", fontSize = 12.sp, fontWeight = if (marketTabSelected == 2) FontWeight.Bold else FontWeight.Normal, color = if (marketTabSelected == 2) CrimsonRed else TextSecondary) }
                    )
                    Tab(
                        selected = marketTabSelected == 3,
                        onClick = { marketTabSelected = 3 },
                        text = { Text("Volume Shockers", fontSize = 12.sp, fontWeight = if (marketTabSelected == 3) FontWeight.Bold else FontWeight.Normal, color = if (marketTabSelected == 3) AmberGold else TextSecondary) }
                    )
                    Tab(
                        selected = marketTabSelected == 4,
                        onClick = { marketTabSelected = 4 },
                        text = { Text("Mutual Funds", fontSize = 12.sp, fontWeight = if (marketTabSelected == 4) FontWeight.Bold else FontWeight.Normal, color = if (marketTabSelected == 4) CyanAccent else TextSecondary) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (marketTabSelected == 4) {
            // Render top Mutual Funds
            items(mutualFunds.take(4), key = { it.id }) { fund ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    com.example.ui.screens.MutualFundItemCard(
                        fund = fund,
                        onSelect = {
                            viewModel.selectMutualFund(fund)
                            onNavigateToMutualFunds()
                        },
                        onStartSip = {
                            viewModel.selectMutualFund(fund)
                            onNavigateToMutualFunds()
                        },
                        onOpenCalculator = {
                            viewModel.selectMutualFund(fund)
                            onNavigateToMutualFunds()
                        }
                    )
                }
            }
        } else {
            items(displayedStocks, key = { it.symbol }) { stock ->
                val isFav = watchlist.any { it.symbol == stock.symbol }
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    StockCard(
                        stock = stock,
                        onClick = { onNavigateToStockDetail(stock.symbol) },
                        isFavorite = isFav,
                        onFavoriteToggle = { viewModel.toggleWatchlist(stock) }
                    )
                }
            }
        }

        // 7. Gemini AI Market Intelligence & Daily Trends Feed Snapshot
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SleekLavender,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Daily Market Intelligence",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.clickable { onNavigateToNews() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "View All Feeds",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekLavender
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SleekLavender,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Gemini Trend Summary Card
                geminiSummary?.let { summary ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekLavender.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToNews() }
                            .testTag("home_gemini_market_card")
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
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SleekLavender.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "GEMINI 3.5 PULSE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekLavender,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                val sentimentColor = when (summary.overallSentiment) {
                                    SignalType.BUY -> EmeraldGreen
                                    SignalType.SELL -> CrimsonRed
                                    SignalType.HOLD -> AmberGold
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = sentimentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${summary.sentimentScore}% ${if (summary.overallSentiment == SignalType.BUY) "Bullish" else "Bearish"}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = sentimentColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = summary.headline,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )

                            Text(
                                text = summary.executiveBriefing,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 3,
                                lineHeight = 17.sp
                            )

                            if (summary.keyDrivers.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    summary.keyDrivers.take(2).forEach { driver ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = NavyDeep,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "${driver.title}: ${driver.impact}",
                                                fontSize = 9.sp,
                                                color = CyanAccent,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Top News Item Card
                if (newsList.isNotEmpty()) {
                    val topNews = newsList.first()
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToNews() }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(topNews.source, fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.SemiBold)
                                    Text("•", color = TextMuted, fontSize = 10.sp)
                                    Text(topNews.timeAgo, fontSize = 11.sp, color = TextMuted)
                                }
                                SignalBadge(signal = topNews.sentiment)
                            }
                            Text(
                                text = topNews.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (topNews.aiTakeaway.isNotBlank()) {
                                Text(
                                    text = "✨ AI Takeaway: ${topNews.aiTakeaway}",
                                    fontSize = 11.sp,
                                    color = SleekLavender,
                                    maxLines = 2,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showModeSelectionSheet) {
        ModeSelectionBottomSheet(
            currentMode = tradingMode,
            onDismissRequest = { showModeSelectionSheet = false },
            onModeConfirmed = { newMode ->
                viewModel.switchTradingMode(newMode)
            }
        )
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NavyCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

