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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ChartType
import com.example.ui.components.EnlargedLiveCandleChartStudio
import com.example.ui.components.InteractiveTradingChart
import com.example.ui.components.SignalBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import java.util.Locale

@Composable
fun TechnicalChartScreen(
    viewModel: TradingViewModel,
    symbol: String,
    onNavigateBack: () -> Unit
) {
    val stock = viewModel.selectedStock.collectAsState().value
    val candles by viewModel.currentCandles.collectAsState()
    val selectedTf by viewModel.selectedTimeframe.collectAsState()
    val chartType by viewModel.selectedChartType.collectAsState()
    val showSma by viewModel.showSma.collectAsState()
    val showEma by viewModel.showEma.collectAsState()
    val showBollinger by viewModel.showBollinger.collectAsState()
    val showVolume by viewModel.showVolume.collectAsState()
    val indicators by viewModel.technicalIndicators.collectAsState()
    var showEnlargedStudio by remember { mutableStateOf(false) }

    val timeframes = listOf("1 Minute", "5 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "1 Day", "1 Week", "1 Month")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("technical_chart_screen"),
        contentPadding = PaddingValues(bottom = 40.dp)
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
                    Text(
                        text = "${stock?.symbol ?: symbol} Technical Studio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Angel One OHLCV Engine",
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                }
            }
        }

        // Chart Type & Indicators Toolbar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chart Type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = chartType == ChartType.CANDLESTICK,
                        onClick = { viewModel.selectedChartType.value = ChartType.CANDLESTICK },
                        label = { Text("Candlestick", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.CandlestickChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = NavyDeep,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = chartType == ChartType.LINE,
                        onClick = { viewModel.selectedChartType.value = ChartType.LINE },
                        label = { Text("Line", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = NavyDeep,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = chartType == ChartType.AREA,
                        onClick = { viewModel.selectedChartType.value = ChartType.AREA },
                        label = { Text("Area", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = NavyDeep,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        )
                    )
                }

                // Timeframe Bar
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(timeframes) { tf ->
                        val isSelected = selectedTf == tf
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTimeframe(tf) },
                            label = { Text(tf.replace("Minutes", "m").replace("Minute", "m").replace("Hour", "h").replace("Day", "D").replace("Week", "W").replace("Month", "M"), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                selectedLabelColor = NavyDeep,
                                containerColor = NavyCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                // Technical Overlays Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = showSma,
                        onClick = { viewModel.showSma.value = !showSma },
                        label = { Text("SMA", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD54F),
                            selectedLabelColor = NavyDeep,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = showEma,
                        onClick = { viewModel.showEma.value = !showEma },
                        label = { Text("EMA", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB388FF),
                            selectedLabelColor = NavyDeep,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = showBollinger,
                        onClick = { viewModel.showBollinger.value = !showBollinger },
                        label = { Text("Bollinger", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = NavyDeep,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = showVolume,
                        onClick = { viewModel.showVolume.value = !showVolume },
                        label = { Text("Volume", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = NavyDeep,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Full Interactive Chart Canvas
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                InteractiveTradingChart(
                    candles = candles,
                    chartType = chartType,
                    showSma = showSma,
                    showEma = showEma,
                    showBollingerBands = showBollinger,
                    showVolume = showVolume,
                    onEnlargeClick = { showEnlargedStudio = true }
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Detailed Indicator Calculations Breakdown
        indicators?.let { ind ->
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Technical Indicators Analysis",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        SignalBadge(signal = ind.overallSentiment)
                    }

                    // Indicator Grid Cards
                    IndicatorItemCard(
                        name = "Exponential Moving Averages (EMA 9, 20, 50, 200)",
                        value = "EMA9: ₹${String.format("%.1f", ind.ema9)} | EMA20: ₹${String.format("%.1f", ind.ema20)}\nEMA50: ₹${String.format("%.1f", ind.ema50)} | EMA200: ₹${String.format("%.1f", ind.ema200)}",
                        status = if (ind.ema20 > ind.ema50) "EMA20 > EMA50 (Golden Trend)" else "EMA20 < EMA50 (Bearish Alignment)",
                        statusColor = if (ind.ema20 > ind.ema50) EmeraldGreen else CrimsonRed
                    )

                    IndicatorItemCard(
                        name = "RSI (14) Momentum",
                        value = "Value: ${String.format("%.2f", ind.rsi)}",
                        status = when {
                            ind.rsi > 70 -> "Overbought (>70)"
                            ind.rsi < 30 -> "Oversold (<30) Bullish Setup"
                            else -> "Healthy Neutral (${String.format("%.1f", ind.rsi)})"
                        },
                        statusColor = when {
                            ind.rsi > 70 -> CrimsonRed
                            ind.rsi < 30 -> EmeraldGreen
                            else -> AmberGold
                        }
                    )

                    IndicatorItemCard(
                        name = "MACD (12, 26, 9) Momentum",
                        value = "Line: ${String.format("%.2f", ind.macd)} | Signal: ${String.format("%.2f", ind.macdSignal)} | Hist: ${if (ind.macdHistogram >= 0) "+" else ""}${String.format("%.2f", ind.macdHistogram)}",
                        status = if (ind.isMacdCrossAbove || ind.macd > ind.macdSignal) "Bullish Crossover" else "Bearish / Negative Divergence",
                        statusColor = if (ind.isMacdCrossAbove || ind.macd > ind.macdSignal) EmeraldGreen else CrimsonRed
                    )

                    IndicatorItemCard(
                        name = "Bollinger Bands (20, 2) Volatility",
                        value = "Upper: ₹${String.format("%.2f", ind.upperBollinger)} | Mid: ₹${String.format("%.2f", ind.middleBollinger)} | Lower: ₹${String.format("%.2f", ind.lowerBollinger)}",
                        status = "Bandwidth: ${String.format("%.2f", ind.bollingerBandwidth)}%",
                        statusColor = CyanAccent
                    )

                    IndicatorItemCard(
                        name = "Volume & 20-Day Volume Moving Average",
                        value = "Current Vol: ${formatVolumeCompact(ind.currentVolume)} | 20D Avg: ${formatVolumeCompact(ind.volume20Avg)}",
                        status = if (ind.isVolumeSpike) "Volume Spike (> 20D Avg)" else "Normal Participation",
                        statusColor = if (ind.isVolumeSpike) EmeraldGreen else AmberGold
                    )

                    IndicatorItemCard(
                        name = "ATR (14) Volatility & Stop-Loss Engine",
                        value = "ATR: ₹${String.format("%.2f", ind.atr)} | Suggested 1.5× SL Buffer: ₹${String.format("%.2f", ind.atr * 1.5)}",
                        status = "SL Baseline Active",
                        statusColor = AmberGold
                    )

                    IndicatorItemCard(
                        name = "SuperTrend (10, 3) Entry/Exit",
                        value = "SuperTrend Value: ₹${String.format("%.2f", ind.superTrend)}",
                        status = if (ind.isSuperTrendBullish) "Bullish Regime Active" else "Bearish Regime Active",
                        statusColor = if (ind.isSuperTrendBullish) EmeraldGreen else CrimsonRed
                    )

                    IndicatorItemCard(
                        name = "VWAP Institutional Benchmark",
                        value = "₹${String.format("%.2f", ind.vwap)}",
                        status = if ((stock?.currentPrice ?: 0.0) > ind.vwap) "Above VWAP (Institutional Long)" else "Below VWAP (Institutional Short)",
                        statusColor = if ((stock?.currentPrice ?: 0.0) > ind.vwap) EmeraldGreen else CrimsonRed
                    )

                    IndicatorItemCard(
                        name = "F&O Participation & PCR Sentiment",
                        value = "Put-Call Ratio: ${String.format("%.2f", ind.putCallRatio)} | Open Interest: ${formatVolumeCompact(ind.openInterest)}",
                        status = if (ind.putCallRatio > 1.0) "Bullish Bias (PCR > 1.0)" else "Cautious Sentiment",
                        statusColor = if (ind.putCallRatio > 1.0) EmeraldGreen else AmberGold
                    )

                    IndicatorItemCard(
                        name = "Support & Resistance Pivot Points",
                        value = "Support S1: ₹${String.format("%.2f", ind.supportLevel1)}",
                        status = "Resistance R1: ₹${String.format("%.2f", ind.resistanceLevel1)}",
                        statusColor = AmberGold
                    )
                }
            }
        }
    }

    if (showEnlargedStudio && stock != null) {
        EnlargedLiveCandleChartStudio(
            stock = stock,
            candles = candles,
            selectedTimeframe = selectedTf,
            onTimeframeChange = { viewModel.setTimeframe(it) },
            onDismiss = { showEnlargedStudio = false }
        )
    }
}

@Composable
private fun IndicatorItemCard(
    name: String,
    value: String,
    status: String,
    statusColor: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(value, fontSize = 12.sp, color = TextSecondary)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun formatVolumeCompact(v: Long): String {
    return when {
        v >= 10000000 -> String.format(Locale.US, "%.2f Cr", v / 10000000.0)
        v >= 100000 -> String.format(Locale.US, "%.2f L", v / 100000.0)
        v >= 1000 -> String.format(Locale.US, "%.1f K", v / 1000.0)
        else -> v.toString()
    }
}

