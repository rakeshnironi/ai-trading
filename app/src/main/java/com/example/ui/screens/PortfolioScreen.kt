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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveSipItem
import com.example.data.model.HoldingItem
import com.example.data.model.MutualFund
import com.example.data.model.SignalType
import com.example.data.remote.dto.BrokerSessionState
import com.example.ui.components.OrderExecutionDialog
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
import com.example.ui.theme.LiveMarketGreen
import com.example.ui.theme.PaperTradingBlue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ExperimentalMaterial3Api
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: TradingViewModel,
    onNavigateToStockDetail: (String) -> Unit,
    onNavigateToBrokerConnect: () -> Unit = {}
) {
    val holdings by viewModel.portfolioHoldings.collectAsState()
    val summary by viewModel.portfolioSummary.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val brokerSessionState by viewModel.brokerSessionState.collectAsState()
    val brokerAccountInfo by viewModel.brokerAccountInfo.collectAsState()
    val mutualFunds by viewModel.mutualFunds.collectAsState()
    val activeSips by viewModel.activeSips.collectAsState()
    val tradingMode by viewModel.tradingMode.collectAsState()
    val isLiveMode = tradingMode == TradingMode.LIVE

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Holdings", "Mutual Funds", "SIPs", "P&L Report")

    var sellHoldingTarget by remember { mutableStateOf<HoldingItem?>(null) }
    var showDownloadReportDialog by remember { mutableStateOf(false) }
    var showModeSelectionSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("portfolio_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header Bar
        item {
            Surface(
                color = NavySurface,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val modeColor = if (isLiveMode) LiveMarketGreen else PaperTradingBlue
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Portfolio", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showModeSelectionSheet = true }
                                    .testTag("portfolio_mode_badge"),
                                color = modeColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, modeColor.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(6.dp).background(modeColor, CircleShape))
                                    Text(
                                        text = if (isLiveMode) "LIVE" else "PAPER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = modeColor
                                    )
                                    Icon(Icons.Default.Sync, contentDescription = "Switch", tint = modeColor, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                        Text(
                            text = if (isLiveMode) "Real Demat Holdings • Angel One SmartAPI" else "Virtual Practice Portfolio • ₹1,00,000 Balance",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = { showDownloadReportDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SleekLavender),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("download_pnl_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = SleekLavender, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("P&L Report", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                    }
                }
            }
        }

        // Live Angel One SmartAPI Integration Card (if connected)
        item {
            if (brokerSessionState == BrokerSessionState.CONNECTED && brokerAccountInfo != null) {
                val account = brokerAccountInfo!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Angel One SmartAPI Live", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            IconButton(onClick = { viewModel.syncBrokerData() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = CyanAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Client Code: ${account.clientCode}", fontSize = 12.sp, color = TextSecondary)
                            Text("Trading Margin: ₹${String.format(Locale.US, "%,.2f", summary?.virtualCashBalance ?: 148250.0)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }
                }
            }
        }

        // Angel One Super Portfolio Card
        item {
            val totalPortfolioValue = summary?.totalPortfolioValue ?: (if (isLiveMode) 148250.0 else 100000.0)
            val totalInvested = summary?.totalInvested ?: 0.0
            val currentValuation = summary?.currentValuation ?: 0.0
            val todayPnl = summary?.todayPnl ?: 0.0
            val totalPnl = summary?.totalPnl ?: 0.0
            val formattedReturn = summary?.formattedReturn ?: "+0.00%"
            val isOverallPositive = totalPnl >= 0
            val isTodayPositive = todayPnl >= 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("portfolio_super_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Total Portfolio Value
                    val isLive = tradingMode == TradingMode.LIVE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLive) "LIVE DEMAT PORTFOLIO" else "VIRTUAL PRACTICE PORTFOLIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLive) LiveMarketGreen else SleekLavender,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (isLive) "Real RMS" else "₹1L Virtual",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format(Locale.US, "%,.2f", totalPortfolioValue)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Quadrants: Invested, Current Value, Today's P/L, Overall P/L
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Invested", fontSize = 11.sp, color = TextSecondary)
                            Text("₹${String.format(Locale.US, "%,.2f", totalInvested)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Current Value", fontSize = 11.sp, color = TextSecondary)
                            Text("₹${String.format(Locale.US, "%,.2f", currentValuation)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Today's P/L", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${if (isTodayPositive) "+" else ""}₹${String.format(Locale.US, "%,.2f", todayPnl)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTodayPositive) EmeraldGreen else CrimsonRed
                            )
                        }
                        Column {
                            Text("Overall P/L", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${if (isOverallPositive) "+" else ""}₹${String.format(Locale.US, "%,.2f", totalPnl)} ($formattedReturn)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverallPositive) EmeraldGreen else CrimsonRed
                            )
                        }
                    }
                }
            }
        }

        // Sub-Navigation Tabs: Holdings, Mutual Funds, SIPs, P&L Report
        item {
            Surface(
                color = NavySurface,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = SleekLavender,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = SleekLavender,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                if (index == 3) {
                                    showDownloadReportDialog = true
                                } else {
                                    selectedTabIndex = index
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTabIndex == index) SleekLavender else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> {
                // HOLDINGS SECTION
                if (holdings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No Equity Holdings Yet", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Execute trades or sync Angel One to see live holdings", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                } else {
                    items(holdings) { holding ->
                        val pnl = holding.pnl
                        val isPositive = pnl >= 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                .clickable { onNavigateToStockDetail(holding.symbol) },
                            colors = CardDefaults.cardColors(containerColor = NavyCard),
                            border = BorderStroke(1.dp, NavyBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(holding.symbol, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(holding.companyName, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${String.format(Locale.US, "%,.2f", holding.currentValue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(
                                            text = "${if (isPositive) "+" else ""}₹${String.format(Locale.US, "%,.2f", pnl)} (${String.format(Locale.US, "%.2f%%", holding.pnlPercentage)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPositive) EmeraldGreen else CrimsonRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Qty: ${holding.quantity} • Avg: ₹${String.format(Locale.US, "%.2f", holding.avgBuyPrice)} • LTP: ₹${String.format(Locale.US, "%.2f", holding.currentLtp)}", fontSize = 11.sp, color = TextSecondary)

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { sellHoldingTarget = holding },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed.copy(alpha = 0.2f)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("SELL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // MUTUAL FUNDS SECTION
                items(mutualFunds) { fund ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = BorderStroke(1.dp, NavyBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fund.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(fund.category.displayName, fontSize = 11.sp, color = SleekLavender)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(fund.formattedNav, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("1Y: ${fund.formatted1Y}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Min SIP: ₹${String.format(Locale.US, "%.0f", fund.minSipAmount)}", fontSize = 11.sp, color = TextSecondary)
                                Text("AUM: ${fund.aum}", fontSize = 11.sp, color = TextSecondary)
                                Text("Rating: ★★★★★".take(fund.starRating + 8), fontSize = 11.sp, color = AmberGold)
                            }
                        }
                    }
                }
            }

            2 -> {
                // SIPS SECTION
                items(activeSips) { sip ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = BorderStroke(1.dp, NavyBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sip.schemeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Next Due: ${sip.nextDueDate}", fontSize = 11.sp, color = TextSecondary)
                                }

                                Surface(
                                    color = if (sip.status == "ACTIVE") EmeraldGreen.copy(alpha = 0.15f) else AmberGold.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = sip.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sip.status == "ACTIVE") EmeraldGreen else AmberGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Monthly SIP", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${String.format(Locale.US, "%,.0f", sip.monthlyAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekLavender)
                                }
                                Column {
                                    Text("Invested (${sip.totalInstallmentsPaid} Inst.)", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${String.format(Locale.US, "%,.0f", sip.totalInvested)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Current Value", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${String.format(Locale.US, "%,.0f", sip.currentValuation)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(
                                    onClick = { viewModel.pauseSip(sip.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(if (sip.status == "ACTIVE") Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (sip.status == "ACTIVE") "Pause SIP" else "Resume", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sell Holding Execution Dialog
    sellHoldingTarget?.let { holding ->
        var sellQty by remember { mutableIntStateOf(holding.quantity) }
        AlertDialog(
            onDismissRequest = { sellHoldingTarget = null },
            containerColor = NavyCardElevated,
            title = {
                Text("Exit Holding: ${holding.symbol}", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(holding.companyName, fontSize = 13.sp, color = TextSecondary)
                    Text("LTP: ₹${String.format(Locale.US, "%,.2f", holding.currentLtp)}", fontSize = 13.sp, color = CyanAccent)
                    Text("Total holding: ${holding.quantity} shares", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (sellQty > 1) sellQty-- },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

                        Text("$sellQty", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                        OutlinedButton(
                            onClick = { if (sellQty < holding.quantity) sellQty++ },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavySurface,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Strict Manual Confirmation: This order will only be submitted when you click Confirm Sell. No trades ever happen automatically.",
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeTrade(holding.symbol, SignalType.SELL, sellQty)
                        sellHoldingTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm Sell", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { sellHoldingTarget = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Download P&L Report Dialog
    if (showDownloadReportDialog) {
        var selectedRange by remember { mutableStateOf("FY 2024-25") }
        var reportGenerated by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDownloadReportDialog = false },
            containerColor = NavyCardElevated,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Download P&L Report", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = { showDownloadReportDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Generate a comprehensive tax and trading P&L statement as provided by Angel One.", fontSize = 12.sp, color = TextSecondary)

                    Text("Select Financial Period", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("FY 2024-25", "Current Q3", "Last 30 Days").forEach { range ->
                            FilterChip(
                                selected = selectedRange == range,
                                onClick = { selectedRange = range },
                                label = { Text(range, fontSize = 11.sp) }
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        border = BorderStroke(1.dp, NavyBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Realized Equity Profit", fontSize = 12.sp, color = TextSecondary)
                                Text("₹14,350.00", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Unrealized Profit", fontSize = 12.sp, color = TextSecondary)
                                Text("₹${String.format(Locale.US, "%,.2f", summary?.totalPnl ?: 0.0)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Charges & STT", fontSize = 12.sp, color = TextSecondary)
                                Text("₹342.50", fontSize = 12.sp, color = CrimsonRed)
                            }
                        }
                    }

                    if (reportGenerated) {
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Report downloaded: AngelOne_PnL_$selectedRange.pdf", fontSize = 11.sp, color = EmeraldGreen)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { reportGenerated = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekLavender),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Statement (PDF)", color = NavyDeep, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDownloadReportDialog = false }, shape = RoundedCornerShape(8.dp)) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
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
