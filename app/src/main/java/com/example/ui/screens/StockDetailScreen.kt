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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SignalType
import com.example.ui.components.CreatePriceAlertDialog
import com.example.ui.components.EnlargedLiveCandleChartStudio
import com.example.ui.components.InteractiveTradingChart
import com.example.ui.components.OrderExecutionDialog
import com.example.ui.components.OptionChainModal
import com.example.ui.components.RiskLevelBadge
import com.example.ui.components.SignalBadge
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
import com.example.ui.theme.LiveMarketGreen
import com.example.ui.theme.PaperTradingBlue
import java.util.Locale

@Composable
fun StockDetailScreen(
    viewModel: TradingViewModel,
    symbol: String,
    onNavigateBack: () -> Unit,
    onNavigateToChart: (String) -> Unit,
    onNavigateToPrediction: (String) -> Unit,
    onNavigateToRecommendation: (String) -> Unit,
    onNavigateToPaperTrading: () -> Unit = {}
) {
    val stock = viewModel.selectedStock.collectAsState().value
    val candles by viewModel.currentCandles.collectAsState()
    val selectedTf by viewModel.selectedTimeframe.collectAsState()
    val indicators by viewModel.technicalIndicators.collectAsState()
    val prediction by viewModel.mlPrediction.collectAsState()
    val recommendation by viewModel.aiRecommendation.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val holdings by viewModel.portfolioHoldings.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val tradingMode by viewModel.tradingMode.collectAsState()

    var showOrderDialog by remember { mutableStateOf(false) }
    var orderAction by remember { mutableStateOf(SignalType.BUY) }
    var showAlertDialog by remember { mutableStateOf(false) }
    var showEnlargedChartStudio by remember { mutableStateOf(false) }
    var showOptionChainDialog by remember { mutableStateOf(false) }

    if (stock == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(NavyDeep),
            contentAlignment = Alignment.Center
        ) {
            Text("Stock not found", color = TextPrimary)
        }
        return
    }

    val isFavorite = watchlist.any { it.symbol == stock.symbol }
    val currentHolding = holdings.find { it.symbol == stock.symbol }
    val ownedQuantity = currentHolding?.quantity ?: 0

    val timeframes = listOf("1 Minute", "5 Minutes", "15 Minutes", "1 Hour", "1 Day", "1 Week")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("stock_detail_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detail_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(stock.symbol, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NavyCardElevated
                                ) {
                                    Text(
                                        text = stock.exchange.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(stock.companyName, fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    Row {
                        IconButton(onClick = { viewModel.toggleWatchlist(stock) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Watchlist",
                                tint = if (isFavorite) AmberGold else TextMuted
                            )
                        }
                    }
                }
            }

            // Price & Day Change with Live Tick Indicator
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stock.formattedPrice,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        if (stock.lastTickDirection == com.example.data.model.PriceTickDirection.UP) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Uptick", tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                                    Text("UP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                            }
                        } else if (stock.lastTickDirection == com.example.data.model.PriceTickDirection.DOWN) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CrimsonRed.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Downtick", tint = CrimsonRed, modifier = Modifier.size(12.dp))
                                    Text("DOWN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                                }
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${stock.formattedChange} (${stock.formattedPercent})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (stock.isPositive) EmeraldGreen else CrimsonRed
                        )
                        val isLive = tradingMode == TradingMode.LIVE
                        val streamColor = if (isLive) LiveMarketGreen else PaperTradingBlue
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable { onNavigateToPaperTrading() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = streamColor,
                                modifier = Modifier.size(6.dp)
                            ) {}
                            Text(
                                text = if (isLive) "Live Real-Time Candles" else "Historical + Simulated Candles",
                                fontSize = 11.sp,
                                color = streamColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Timeframe Selector & Fullscreen Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(timeframes) { tf ->
                            val isSelected = selectedTf == tf
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setTimeframe(tf) },
                                label = { Text(tf.replace("Minutes", "m").replace("Minute", "m").replace("Hour", "h").replace("Day", "D").replace("Week", "W"), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent,
                                    selectedLabelColor = NavyDeep,
                                    containerColor = NavyCard,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SleekLavender.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, SleekLavender),
                            modifier = Modifier
                                .clickable { showOptionChainDialog = true }
                                .padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Option Chain",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekLavender,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }

                        IconButton(
                            onClick = { showEnlargedChartStudio = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Full Chart Studio", tint = CyanAccent)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Interactive Candlestick Chart
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    InteractiveTradingChart(
                        candles = candles,
                        showSma = true,
                        showEma = true,
                        showVolume = true,
                        onEnlargeClick = { showEnlargedChartStudio = true }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Machine Learning Price Direction Predictor Card (Will it Increase or Decrease?)
            prediction?.let { pred ->
                item {
                    val isPredIncrease = pred.isIncrease
                    val isPredDecrease = pred.isDecrease
                    val accentColor = if (isPredIncrease) EmeraldGreen else if (isPredDecrease) CrimsonRed else AmberGold
                    val dirIcon = if (isPredIncrease) Icons.AutoMirrored.Filled.TrendingUp else if (isPredDecrease) Icons.AutoMirrored.Filled.TrendingDown else Icons.Default.Psychology

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable { onNavigateToPrediction(stock.symbol) }
                            .testTag("detail_ml_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                    Text("AI PRICE DIRECTION PREDICTOR", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = CyanAccent)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = accentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "${pred.confidence}% Confidence",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Big Verdict: Will it Increase or Decrease?
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accentColor.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "PRICE MOVEMENT VERDICT (${pred.horizon})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isPredIncrease) "WILL INCREASE ↗" else if (isPredDecrease) "WILL DECREASE ↘" else "SIDEWAYS / NEUTRAL ↔",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = accentColor
                                        )
                                    }
                                    Icon(
                                        imageVector = dirIcon,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            // Dual Probability Gauge: Chance of Increase vs Decrease
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Increase Chance: ${pred.increaseProbability}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                    Text(
                                        text = "Decrease Chance: ${pred.decreaseProbability}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonRed
                                    )
                                }

                                // Custom Dual-Colored Split Progress Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(CrimsonRed.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = (pred.increaseProbability / 100f).coerceIn(0.05f, 0.95f))
                                            .height(8.dp)
                                            .background(EmeraldGreen, RoundedCornerShape(4.dp))
                                    )
                                }
                            }

                            // Forecast Target & Expected Return
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Forecast Target (${pred.horizon})", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${String.format(Locale.US, "%,.2f", pred.predictedPrice)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Expected Move", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        text = "${if (pred.expectedReturnPercent >= 0) "+" else ""}${String.format("%.2f", pred.expectedReturnPercent)}%",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pred.expectedReturnPercent >= 0) EmeraldGreen else CrimsonRed
                                    )
                                }
                            }

                            // Trade Outlook Summary
                            Text(
                                text = pred.tradeOutlookSummary,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )

                            // Zero Auto-Execution Assurance Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NavyCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "Zero Auto-Execution: Orders are NEVER placed automatically. Review this forecast and place manual trades when ready.",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            // Action footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("LSTM Multi-Step Forecasting", fontSize = 10.sp, color = TextMuted)
                                Text("Open Deep Prediction Analysis ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // AI Recommendation Consensus Banner (Shortcut to RecommendationScreen)
            recommendation?.let { rec ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable { onNavigateToRecommendation(stock.symbol) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SignalBadge(signal = rec.signal)
                                Column {
                                    Text("AI Algorithm Consensus", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(rec.indicatorConsensus, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyanAccent)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 52-Week & Day High-Low Visual Range
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Price Performance Ranges", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                        // Day Range
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Day Low: ₹${stock.low}", fontSize = 12.sp, color = TextSecondary)
                                Text("Today's Range", fontSize = 11.sp, color = TextMuted)
                                Text("Day High: ₹${stock.high}", fontSize = 12.sp, color = TextSecondary)
                            }
                            val dayFraction = if (stock.high > stock.low) ((stock.currentPrice - stock.low) / (stock.high - stock.low)).toFloat().coerceIn(0f, 1f) else 0.5f
                            LinearProgressIndicator(
                                progress = { dayFraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = CyanAccent,
                                trackColor = NavySurface
                            )
                        }

                        // 52W Range
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("52W Low: ₹${stock.low52Week}", fontSize = 12.sp, color = TextSecondary)
                                Text("52-Week Range", fontSize = 11.sp, color = TextMuted)
                                Text("52W High: ₹${stock.high52Week}", fontSize = 12.sp, color = TextSecondary)
                            }
                            val yearFraction = if (stock.high52Week > stock.low52Week) ((stock.currentPrice - stock.low52Week) / (stock.high52Week - stock.low52Week)).toFloat().coerceIn(0f, 1f) else 0.5f
                            LinearProgressIndicator(
                                progress = { yearFraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = EmeraldGreen,
                                trackColor = NavySurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Real-Time Level 2 Market Depth (Order Book)
            if (stock.marketDepth.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        com.example.ui.components.LiveMarketDepthTable(marketDepth = stock.marketDepth)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Key Market & Fundamental Metrics Grid
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Fundamental & Market Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                            MetricRow("Open Price", "₹${stock.open}", "Previous Close", "₹${stock.previousClose}")
                            MetricRow("Volume", String.format(Locale.US, "%,d", stock.volume), "Sector", stock.sector)
                            MetricRow("Market Cap", stock.marketCap, "Instrument Token", stock.token)
                            indicators?.let { ind ->
                                MetricRow("RSI (14)", String.format("%.2f", ind.rsi), "VWAP", "₹${String.format("%.2f", ind.vwap)}")
                                MetricRow("SMA 50", "₹${String.format("%.2f", ind.sma50)}", "EMA 21", "₹${String.format("%.2f", ind.ema21)}")
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Execution Bar (Buy & Sell Paper Trade)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = NavySurface,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = {
                        orderAction = SignalType.BUY
                        showOrderDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tradingMode == TradingMode.LIVE) LiveMarketGreen else EmeraldGreen
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("detail_buy_button")
                ) {
                    Text(
                        text = if (tradingMode == TradingMode.LIVE) "BUY (Live)" else "BUY (Paper)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Button(
                    onClick = {
                        orderAction = SignalType.SELL
                        showOrderDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("detail_sell_button")
                ) {
                    Text(
                        text = if (tradingMode == TradingMode.LIVE) "SELL (Live)" else "SELL (Paper)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Enlarged Live Candle Chart Studio (Groww / Angel One Fullscreen Mode)
        if (showEnlargedChartStudio) {
            EnlargedLiveCandleChartStudio(
                stock = stock,
                candles = candles,
                selectedTimeframe = selectedTf,
                onTimeframeChange = { viewModel.setTimeframe(it) },
                onDismiss = { showEnlargedChartStudio = false },
                onBuyClick = {
                    orderAction = SignalType.BUY
                    showOrderDialog = true
                },
                onSellClick = {
                    orderAction = SignalType.SELL
                    showOrderDialog = true
                }
            )
        }

        // Order Execution Dialog
        if (showOrderDialog) {
            OrderExecutionDialog(
                stock = stock,
                action = orderAction,
                virtualBalance = profile?.virtualBalance ?: 100000.0,
                maxSellQuantity = ownedQuantity,
                tradingMode = tradingMode,
                prediction = prediction,
                onDismiss = { showOrderDialog = false },
                onConfirm = { qty ->
                    viewModel.executeTrade(stock.symbol, orderAction, qty)
                    showOrderDialog = false
                }
            )
        }

        // Price Alert Dialog
        if (showAlertDialog) {
            CreatePriceAlertDialog(
                stock = stock,
                onDismiss = { showAlertDialog = false },
                onConfirm = { price, cond ->
                    viewModel.createPriceAlert(stock.symbol, price, cond)
                    showAlertDialog = false
                }
            )
        }

        // Option Chain Modal
        if (showOptionChainDialog) {
            OptionChainModal(
                symbol = stock.symbol,
                viewModel = viewModel,
                onDismiss = { showOptionChainDialog = false }
            )
        }
    }
}

@Composable
private fun MetricRow(
    label1: String,
    val1: String,
    label2: String,
    val2: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label1, fontSize = 11.sp, color = TextMuted)
            Text(val1, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(label2, fontSize = 11.sp, color = TextMuted)
            Text(val2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}
