package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrendDirection
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
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
fun AIPredictionScreen(
    viewModel: TradingViewModel,
    symbol: String,
    onNavigateBack: () -> Unit
) {
    val stock = viewModel.selectedStock.collectAsState().value
    val prediction by viewModel.mlPrediction.collectAsState()
    val horizon by viewModel.predictionHorizon.collectAsState()

    val horizons = listOf("1 Day", "3 Days", "1 Week")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .testTag("ai_prediction_screen"),
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
                        text = "${stock?.symbol ?: symbol} AI Prediction",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Deep Learning LSTM Model",
                        fontSize = 12.sp,
                        color = CyanAccent
                    )
                }
            }
        }

        // Zero Auto-Execution Assurance Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "PREDICTIVE ANALYSIS ONLY (NO AUTO-ORDERS)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent
                        )
                        Text(
                            text = "The AI predicts whether the price will INCREASE or DECREASE. It will NEVER execute orders automatically. All trades require your manual confirmation.",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Horizon Selector Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                horizons.forEach { h ->
                    val isSelected = horizon == h
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setPredictionHorizon(h) },
                        label = { Text("Horizon: $h", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = NavyDeep,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Main Prediction Result Hero Card (Will it Increase or Decrease?)
        prediction?.let { pred ->
            item {
                val isPredIncrease = pred.isIncrease
                val isPredDecrease = pred.isDecrease
                val accentColor = if (isPredIncrease) EmeraldGreen else if (isPredDecrease) CrimsonRed else AmberGold
                val dirIcon = if (isPredIncrease) Icons.AutoMirrored.Filled.TrendingUp else if (isPredDecrease) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingFlat

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(listOf(accentColor, CyanAccent.copy(alpha = 0.5f)))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Big Directional Question & Verdict
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "WILL THE PRICE INCREASE OR DECREASE?",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isPredIncrease) "WILL INCREASE ↗" else if (isPredDecrease) "WILL DECREASE ↘" else "SIDEWAYS / NEUTRAL ↔",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentColor
                                    )
                                    Text(
                                        text = "Lookahead Horizon: ${pred.horizon}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = dirIcon,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        // Dual Probability Split Gauge: Chance of Increase vs Decrease
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                    Text("Increase Chance: ${pred.increaseProbability}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(14.dp))
                                    Text("Decrease Chance: ${pred.decreaseProbability}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                                }
                            }

                            // Visual Split Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .background(CrimsonRed.copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = (pred.increaseProbability / 100f).coerceIn(0.05f, 0.95f))
                                        .height(10.dp)
                                        .background(EmeraldGreen, RoundedCornerShape(5.dp))
                                )
                            }
                        }

                        // Current Price vs Predicted Delta
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavySurface, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Current Live LTP", fontSize = 11.sp, color = TextMuted)
                                Text("₹${String.format(Locale.US, "%,.2f", pred.currentPrice)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Forecast Target", fontSize = 11.sp, color = TextMuted)
                                Text("₹${String.format(Locale.US, "%,.2f", pred.predictedPrice)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CyanAccent)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Expected Return", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = "${if (pred.expectedReturnPercent >= 0) "+" else ""}${String.format("%.2f", pred.expectedReturnPercent)}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pred.expectedReturnPercent >= 0) EmeraldGreen else CrimsonRed
                                )
                            }
                        }

                        // Trade Impact Analysis (What this means for your trade)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NavyCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "TRADE OUTLOOK & ACTION ANALYSIS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                                Text(
                                    text = pred.tradeOutlookSummary,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Confidence Gauge
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TFLite Model Confidence", fontSize = 12.sp, color = TextSecondary)
                                Text("${pred.confidence}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                            LinearProgressIndicator(
                                progress = { pred.confidence / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = CyanAccent,
                                trackColor = NavySurface
                            )
                        }

                        // TFLite Execution Stats Chip Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NavySurface
                            ) {
                                Text(
                                    text = "⚡ Latency: ${pred.inferenceTimeMs}ms",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NavySurface
                            ) {
                                Text(
                                    text = "Tensor: ${pred.inputTensorShape} → ${pred.outputTensorShape}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Multi-Horizon Forecast Steps
            if (pred.multiStepForecast.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TFLite Multi-Horizon Forecast",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "5 Step Lookahead",
                                fontSize = 11.sp,
                                color = CyanAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pred.multiStepForecast.forEach { step ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(NavySurface.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(step.stepLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text(
                                                text = "Risk Corridor: ₹${String.format(Locale.US, "%,.0f", step.lowerBound)} - ₹${String.format(Locale.US, "%,.0f", step.upperBound)}",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "₹${String.format(Locale.US, "%,.2f", step.predictedPrice)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${if (step.returnPercent >= 0) "+" else ""}${String.format("%.2f", step.returnPercent)}%",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (step.returnPercent >= 0) EmeraldGreen else CrimsonRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Feature Importance & Input Signals Breakdown
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "LSTM Feature Weight Matrix",
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
                            pred.featureMetrics.forEach { (metricName, score) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(metricName, fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        text = String.format("%.2f", score),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Machine Learning Architecture Specs
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "TensorFlow Lite Runtime Specs",
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
                            SpecRow("Inference Engine", "TensorFlow Lite v2.16.1 Native")
                            SpecRow("Execution Delegate", pred.executionDelegate)
                            SpecRow("Input Tensor", "${pred.inputTensorShape} Float32")
                            SpecRow("Output Tensor", "${pred.outputTensorShape} Float32 (Multi-Step)")
                            SpecRow("Lookback Window", "30 SmartAPI Historical Candles")
                            SpecRow("Features", "OHLCV + RSI(14) + MACD + EMA21 Dev")
                            SpecRow("Data Source", "Angel One SmartAPI V2 Feed")
                            SpecRow("Inference Latency", "${pred.inferenceTimeMs} ms")
                            SpecRow("Generated Time", pred.trainedTimestamp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(key, fontSize = 11.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
