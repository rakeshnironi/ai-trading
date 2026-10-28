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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.components.OrderExecutionDialog
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import java.util.Locale

@Composable
fun RecommendationScreen(
    viewModel: TradingViewModel,
    symbol: String,
    onNavigateBack: () -> Unit,
    onNavigateToPaperTrading: () -> Unit
) {
    val stock = viewModel.selectedStock.collectAsState().value
    val recommendation by viewModel.aiRecommendation.collectAsState()
    val prediction by viewModel.mlPrediction.collectAsState()
    val tradingMode by viewModel.tradingMode.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val holdings by viewModel.portfolioHoldings.collectAsState()

    var showOrderDialog by remember { mutableStateOf(false) }

    val currentHolding = holdings.find { it.symbol == (stock?.symbol ?: symbol) }
    val ownedQuantity = currentHolding?.quantity ?: 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("recommendation_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
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
                        text = "${stock?.symbol ?: symbol} AI Signal Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Synthesized ML & Technical Consensus",
                        fontSize = 12.sp,
                        color = CyanAccent
                    )
                }
            }
        }

        // Hero Recommendation Card
        recommendation?.let { rec ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            if (rec.signal == SignalType.BUY) listOf(EmeraldGreen, CyanAccent)
                            else if (rec.signal == SignalType.SELL) listOf(CrimsonRed, AmberGold)
                            else listOf(AmberGold, CyanAccent)
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Recommendation Verdict", fontSize = 12.sp, color = TextSecondary)
                                Text(stock?.companyName ?: symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SignalBadge(signal = rec.signal)
                                RiskLevelBadge(riskLevel = rec.riskLevel)
                            }
                        }

                        // Price Movement Direction (Will it Increase or Decrease?)
                        val isRecIncrease = rec.signal == SignalType.BUY
                        val isRecDecrease = rec.signal == SignalType.SELL
                        val dirColor = if (isRecIncrease) EmeraldGreen else if (isRecDecrease) CrimsonRed else AmberGold

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = dirColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, dirColor.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("PRICE MOVEMENT PREDICTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isRecIncrease) "PREDICTED TO INCREASE ↗" else if (isRecDecrease) "PREDICTED TO DECREASE ↘" else "PREDICTED SIDEWAYS ↔",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = dirColor
                                    )
                                    Text(
                                        text = if (isRecIncrease) "Bullish consensus: Technical & ML indicators forecast upward price gain" else if (isRecDecrease) "Bearish consensus: High probability of downward price decline" else "Neutral: Price expected to consolidate within range",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        // Price Target & ATR Dynamic Stop Loss
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavySurface, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Current LTP", fontSize = 11.sp, color = TextMuted)
                                Text("₹${String.format(Locale.US, "%,.2f", rec.currentPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Target Price (2.5× ATR)", fontSize = 11.sp, color = TextMuted)
                                Text("₹${String.format(Locale.US, "%,.2f", rec.targetPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Stop Loss (1.5× ATR)", fontSize = 11.sp, color = TextMuted)
                                Text("₹${String.format(Locale.US, "%,.2f", rec.stopLoss)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                            }
                        }

                        // Confidence Indicator
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Random Forest Ensemble Confidence", fontSize = 12.sp, color = TextSecondary)
                                Text("${rec.confidence}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                            LinearProgressIndicator(
                                progress = { rec.confidence / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = CyanAccent,
                                trackColor = NavySurface
                            )
                        }

                        // Risk Alert & Volatility Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (rec.isRiskAlert) CrimsonRed.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (rec.isRiskAlert) Icons.Default.Warning else Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (rec.isRiskAlert) CrimsonRed else EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (rec.isRiskAlert) rec.riskAlertMessage else "Risk: ${String.format(Locale.US, "%.1f", rec.riskPercent)}% SL Risk (ATR: ₹${String.format(Locale.US, "%.2f", rec.atrValue)}) — Controlled Volatility",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (rec.isRiskAlert) CrimsonRed else EmeraldGreen
                                )
                            }
                        }

                        // Action Buttons: Send via WhatsApp & Execute Paper Trade
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerManualAiWhatsAppSignal(rec) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("send_whatsapp_signal_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Text("Send WhatsApp Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            Button(
                                onClick = { showOrderDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("paper_trade_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = NavyDeep, modifier = Modifier.size(16.dp))
                                    Text("Paper Trade", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDeep)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Architecture Flow Pipeline Card (Angel One Style)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "AI & Algorithm Architecture (Angel One Style)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ArchitecturePipelineStep(
                                stepNumber = "1",
                                title = "Live Market Data Feed (SmartAPI)",
                                subtitle = "OHLCV Candles + WebSocket Tick Data: Open, High, Low, Close, Volume"
                            )
                            ArchitecturePipelineStep(
                                stepNumber = "2",
                                title = "Technical Indicator Engine",
                                subtitle = "EMA (9, 20, 50, 200), RSI (14), MACD, Bollinger Bands, VWAP, Supertrend, ATR, Volume+OI"
                            )
                            ArchitecturePipelineStep(
                                stepNumber = "3",
                                title = "AI / ML Prediction Layer",
                                subtitle = "LSTM (Price Trend) + XGBoost (Buy/Hold/Sell) + Random Forest (Confidence Scoring)"
                            )
                            ArchitecturePipelineStep(
                                stepNumber = "4",
                                title = "Algorithmic Decision Execution",
                                subtitle = "Verdict: ${rec.signal.name} | Dynamic ATR Stop-Loss: ₹${String.format(Locale.US, "%.2f", rec.stopLoss)} | Target: ₹${String.format(Locale.US, "%.2f", rec.targetPrice)}"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // XGBoost Decision Rule Matrix Engine Card
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "XGBoost Decision Rule Engine",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Algo Logic Matrix",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rec.decisionRuleChecks.forEach { rule ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NavySurface, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (rule.isSatisfied) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (rule.isSatisfied) EmeraldGreen else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(rule.ruleLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text(rule.weight, fontSize = 10.sp, color = CyanAccent)
                                        }
                                        Text(rule.observedValue, fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Random Forest Feature Importance Ranking
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Random Forest Feature Importance Ranking",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rec.featureRankings.forEach { feature ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(feature.featureName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        Text("${feature.importancePercent}% (${feature.source})", fontSize = 11.sp, color = CyanAccent)
                                    }
                                    LinearProgressIndicator(
                                        progress = { feature.importancePercent / 30f },
                                        modifier = Modifier.fillMaxWidth().height(4.dp),
                                        color = if (feature.importancePercent > 18) EmeraldGreen else CyanAccent,
                                        trackColor = NavySurface
                                    )
                                    Text(feature.observedValue, fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // 9 Engineered Features Matrix Card
            rec.engineeredFeatures?.let { ef ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Engineered Feature Inputs (9 Vectors)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FeatureMatrixRow(
                                    label1 = "1. OHLCV Close", value1 = "₹${String.format(Locale.US, "%.2f", ef.close)}",
                                    label2 = "2. Volume (Live)", value2 = formatVolumeCompact(ef.volume)
                                )
                                FeatureMatrixRow(
                                    label1 = "3. RSI (14)", value1 = String.format(Locale.US, "%.1f", ef.rsi),
                                    label2 = "4. EMA 20 / EMA 50", value2 = "₹${String.format(Locale.US, "%.0f", ef.ema20)} / ₹${String.format(Locale.US, "%.0f", ef.ema50)}"
                                )
                                FeatureMatrixRow(
                                    label1 = "5. MACD Histogram", value1 = "${if (ef.macdHistogram >= 0) "+" else ""}${String.format(Locale.US, "%.2f", ef.macdHistogram)}",
                                    label2 = "6. VWAP Benchmark", value2 = "₹${String.format(Locale.US, "%.2f", ef.vwap)}"
                                )
                                FeatureMatrixRow(
                                    label1 = "7. ATR Volatility", value1 = "₹${String.format(Locale.US, "%.2f", ef.atr)}",
                                    label2 = "8. Put-Call Ratio (PCR)", value2 = String.format(Locale.US, "%.2f", ef.putCallRatio)
                                )
                                FeatureMatrixRow(
                                    label1 = "9. Open Interest (OI)", value1 = formatVolumeCompact(ef.openInterest),
                                    label2 = "Model Status", value2 = "XGBoost Active"
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Primary Algorithmic Reasons List
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Algorithmic Factor Justifications",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
                            rec.primaryReasons.forEach { reason ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(8.dp)
                                            .background(CyanAccent, CircleShape)
                                    )
                                    Text(
                                        text = reason,
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Educational Disclaimer
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Educational Disclaimer: All recommendations and predictions generated by this application are algorithmic simulations for research purposes only. Paper trade simulation does not involve real capital.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showOrderDialog && stock != null && recommendation != null) {
        OrderExecutionDialog(
            stock = stock,
            action = if (recommendation?.signal == SignalType.SELL) SignalType.SELL else SignalType.BUY,
            virtualBalance = profile?.virtualBalance ?: 100000.0,
            maxSellQuantity = ownedQuantity,
            tradingMode = tradingMode,
            prediction = prediction,
            onDismiss = { showOrderDialog = false },
            onConfirm = { qty ->
                viewModel.executeTrade(stock.symbol, if (recommendation?.signal == SignalType.SELL) SignalType.SELL else SignalType.BUY, qty)
                showOrderDialog = false
            }
        )
    }
}

@Composable
private fun ArchitecturePipelineStep(
    stepNumber: String,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavySurface, RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(CyanAccent.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyanAccent
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun FeatureMatrixRow(
    label1: String,
    value1: String,
    label2: String,
    value2: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(NavySurface, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label1, fontSize = 10.sp, color = TextMuted)
            Text(text = value1, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .background(NavySurface, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label2, fontSize = 10.sp, color = TextMuted)
            Text(text = value2, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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

