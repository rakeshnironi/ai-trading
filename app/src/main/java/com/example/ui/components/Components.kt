package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradingMode
import com.example.ui.theme.LiveMarketGreen
import com.example.ui.theme.PaperTradingBlue
import com.example.data.model.AlertCondition
import com.example.data.model.MarketIndex
import com.example.data.model.RiskLevel
import com.example.data.model.SignalType
import com.example.data.model.StockQuote
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
import java.util.Locale

@Composable
fun MarketIndexCard(
    index: MarketIndex,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val tickBorderColor by androidx.compose.animation.animateColorAsState(
        targetValue = when (index.lastTickDirection) {
            com.example.data.model.PriceTickDirection.UP -> EmeraldGreen.copy(alpha = 0.6f)
            com.example.data.model.PriceTickDirection.DOWN -> CrimsonRed.copy(alpha = 0.6f)
            else -> NavyBorder
        },
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
        label = "index_tick_border"
    )

    Card(
        modifier = modifier
            .width(150.dp)
            .clickable { onClick() }
            .testTag("index_card_${index.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, tickBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldGreen,
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Text(
                        text = index.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = if (index.isPositive) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f),
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = if (index.isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (index.isPositive) EmeraldGreen else CrimsonRed,
                        modifier = Modifier.padding(3.dp)
                    )
                }
            }

            Text(
                text = index.formattedValue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = index.formattedChange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (index.isPositive) EmeraldGreen else CrimsonRed
                )
            }
        }
    }
}

@Composable
fun StockCard(
    stock: StockQuote,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteToggle: (() -> Unit)? = null
) {
    val animatedBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = when (stock.lastTickDirection) {
            com.example.data.model.PriceTickDirection.UP -> EmeraldGreen.copy(alpha = 0.08f)
            com.example.data.model.PriceTickDirection.DOWN -> CrimsonRed.copy(alpha = 0.08f)
            else -> NavyCard
        },
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500),
        label = "stock_tick_bg"
    )

    val animatedBorderColor by androidx.compose.animation.animateColorAsState(
        targetValue = when (stock.lastTickDirection) {
            com.example.data.model.PriceTickDirection.UP -> EmeraldGreen.copy(alpha = 0.45f)
            com.example.data.model.PriceTickDirection.DOWN -> CrimsonRed.copy(alpha = 0.45f)
            else -> NavyBorder
        },
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500),
        label = "stock_tick_border"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("stock_card_${stock.symbol.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, animatedBorderColor)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.06f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stock.symbol.take(1),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stock.symbol,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NavyBorder.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = stock.exchange.name,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        if (stock.lastTickDirection != com.example.data.model.PriceTickDirection.UNCHANGED) {
                            Surface(
                                shape = CircleShape,
                                color = if (stock.lastTickDirection == com.example.data.model.PriceTickDirection.UP) EmeraldGreen else CrimsonRed,
                                modifier = Modifier.size(6.dp)
                            ) {}
                        }
                    }
                    Text(
                        text = stock.companyName,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stock.formattedPrice,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (stock.lastTickDirection == com.example.data.model.PriceTickDirection.UP) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "Uptick",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        } else if (stock.lastTickDirection == com.example.data.model.PriceTickDirection.DOWN) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "Downtick",
                                tint = CrimsonRed,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = "${stock.formattedPercent} (${stock.formattedChange})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (stock.isPositive) EmeraldGreen else CrimsonRed
                    )
                }

                if (onFavoriteToggle != null) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) AmberGold else TextMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real-Time Streaming Live Market Ticker Marquee Bar
 */
@Composable
fun LiveMarketTickerBar(
    indices: List<MarketIndex>,
    stocks: List<StockQuote>,
    isLiveActive: Boolean,
    onToggleLive: () -> Unit,
    onStockClick: (String) -> Unit,
    onLiveClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = NavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Status Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isLiveActive) EmeraldGreen.copy(alpha = 0.15f) else AmberGold.copy(alpha = 0.15f),
                modifier = Modifier
                    .clickable { 
                        if (onLiveClick != null) onLiveClick() else onToggleLive() 
                    }
                    .testTag("ticker_live_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isLiveActive) EmeraldGreen else AmberGold,
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Text(
                        text = if (isLiveActive) "LIVE" else "PAUSED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLiveActive) EmeraldGreen else AmberGold
                    )
                }
            }

            // Horizontal Ticker Stream
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(indices.size) { idx ->
                    val index = indices[idx]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(index.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(index.formattedValue, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            (if (index.isPositive) "+" else "") + String.format(Locale.US, "%.2f%%", index.percentChange),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (index.isPositive) EmeraldGreen else CrimsonRed
                        )
                    }
                }

                items(stocks.take(8).size) { idx ->
                    val st = stocks[idx]
                    Row(
                        modifier = Modifier.clickable { onStockClick(st.symbol) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(st.symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        Text(st.formattedPrice, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            st.formattedPercent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (st.isPositive) EmeraldGreen else CrimsonRed
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5-Level Real-time Market Depth (Level 2 Order Book)
 */
@Composable
fun LiveMarketDepthTable(
    marketDepth: List<com.example.data.model.MarketDepthItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Market Depth (L2 Live Book)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = CircleShape, color = EmeraldGreen, modifier = Modifier.size(6.dp)) {}
                    Text("Live Quotes", fontSize = 10.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Buy Qty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.weight(1f))
                Text("Bid Price", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.weight(1f))
                Text("Ask Price", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrimsonRed, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                Text("Sell Qty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrimsonRed, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }

            Spacer(modifier = Modifier.height(6.dp))

            marketDepth.take(5).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(String.format(Locale.US, "%,d", item.buyQty), fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                    Text(String.format(Locale.US, "₹%.2f", item.buyPrice), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EmeraldGreen, modifier = Modifier.weight(1f))
                    Text(String.format(Locale.US, "₹%.2f", item.sellPrice), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CrimsonRed, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    Text(String.format(Locale.US, "%,d", item.sellQty), fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }
        }
    }
}

@Composable
fun SignalBadge(
    signal: SignalType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (signal) {
        SignalType.BUY -> Triple(EmeraldGreen.copy(alpha = 0.18f), EmeraldGreen, "BUY")
        SignalType.SELL -> Triple(CrimsonRed.copy(alpha = 0.18f), CrimsonRed, "SELL")
        SignalType.HOLD -> Triple(AmberGold.copy(alpha = 0.18f), AmberGold, "HOLD")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RiskLevelBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (riskLevel) {
        RiskLevel.LOW -> Triple(EmeraldGreen.copy(alpha = 0.15f), EmeraldGreen, "LOW RISK")
        RiskLevel.MODERATE -> Triple(AmberGold.copy(alpha = 0.15f), AmberGold, "MODERATE RISK")
        RiskLevel.HIGH -> Triple(CrimsonRed.copy(alpha = 0.15f), CrimsonRed, "HIGH RISK")
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun OrderExecutionDialog(
    stock: StockQuote,
    action: SignalType, // BUY or SELL
    virtualBalance: Double,
    maxSellQuantity: Int = 0,
    tradingMode: TradingMode = TradingMode.PAPER,
    prediction: com.example.data.model.MLPredictionResult? = null,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    val totalAmount = stock.currentPrice * quantity
    val hasSufficientBalance = action == SignalType.SELL || virtualBalance >= totalAmount
    val hasSufficientShares = action == SignalType.BUY || quantity <= maxSellQuantity
    val isLive = tradingMode == TradingMode.LIVE
    val modeColor = if (isLive) LiveMarketGreen else PaperTradingBlue

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Surface(
                        color = modeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, modeColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = if (isLive) "● LIVE MANUAL ORDER" else "● PAPER PRACTICE ORDER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = modeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${action.name} ${stock.symbol}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                SignalBadge(signal = action)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // AI Price Direction Prediction Card (Predicts whether price will increase or decrease)
                prediction?.let { pred ->
                    val isPredIncrease = pred.isIncrease
                    val isPredDecrease = pred.isDecrease
                    val predBg = if (isPredIncrease) EmeraldGreen.copy(alpha = 0.12f) else if (isPredDecrease) CrimsonRed.copy(alpha = 0.12f) else NavyCard
                    val predBorder = if (isPredIncrease) EmeraldGreen.copy(alpha = 0.5f) else if (isPredDecrease) CrimsonRed.copy(alpha = 0.5f) else NavyBorder
                    val predTextColor = if (isPredIncrease) EmeraldGreen else if (isPredDecrease) CrimsonRed else AmberGold

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = predBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, predBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                                        imageVector = if (isPredIncrease) Icons.AutoMirrored.Filled.TrendingUp else if (isPredDecrease) Icons.AutoMirrored.Filled.TrendingDown else Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = predTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isPredIncrease) "PREDICTED TO INCREASE ↗" else if (isPredDecrease) "PREDICTED TO DECREASE ↘" else "PREDICTED SIDEWAYS ↔",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = predTextColor
                                    )
                                }
                                Text(
                                    text = "${pred.confidence}% Conf.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }

                            val alignmentText = when {
                                action == SignalType.BUY && isPredIncrease ->
                                    "✅ Favorable: Price is predicted to INCREASE (+${String.format(Locale.US, "%.2f", pred.expectedReturnPercent)}%) towards ₹${String.format(Locale.US, "%,.2f", pred.predictedPrice)}."
                                action == SignalType.BUY && isPredDecrease ->
                                    "⚠️ Caution: Price is predicted to DECREASE (${String.format(Locale.US, "%.2f", pred.expectedReturnPercent)}%) towards ₹${String.format(Locale.US, "%,.2f", pred.predictedPrice)}. High risk for manual long trade."
                                action == SignalType.SELL && isPredDecrease ->
                                    "✅ Favorable: Price is predicted to DECREASE (${String.format(Locale.US, "%.2f", pred.expectedReturnPercent)}%). Good timing for manual profit booking / exit."
                                else ->
                                    "ℹ️ Note: Target forecast ₹${String.format(Locale.US, "%,.2f", pred.predictedPrice)} with ${if (pred.expectedReturnPercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", pred.expectedReturnPercent)}% expected move."
                            }

                            Text(
                                text = alignmentText,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Stock LTP & Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isLive) "Live Market LTP (SmartAPI)" else "Simulated Market LTP",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(stock.formattedPrice, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                // Quantity Selector
                Column {
                    Text("Select Quantity", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyCard, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary)
                        }

                        Text(
                            text = "$quantity Shares",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )

                        IconButton(
                            onClick = { quantity++ }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary)
                        }
                    }
                }

                // Total Calculation Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyCardElevated, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Order Value", fontSize = 12.sp, color = TextMuted)
                        Text("₹${String.format(Locale.US, "%,.2f", totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isLive) "Available Demat Margin" else "Available Virtual Cash", fontSize = 12.sp, color = TextMuted)
                        Text("₹${String.format(Locale.US, "%,.2f", virtualBalance)}", fontSize = 13.sp, color = if (isLive) LiveMarketGreen else CyanAccent)
                    }
                    if (action == SignalType.SELL) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Holdings", fontSize = 12.sp, color = TextMuted)
                            Text("$maxSellQuantity Shares", fontSize = 13.sp, color = AmberGold)
                        }
                    }
                }

                if (!hasSufficientBalance) {
                    Text(
                        text = if (isLive) "Insufficient Demat trading margin for this order." else "Insufficient virtual balance for this order.",
                        fontSize = 12.sp,
                        color = CrimsonRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!hasSufficientShares && action == SignalType.SELL) {
                    Text(
                        text = "Cannot sell more shares than currently owned ($maxSellQuantity).",
                        fontSize = 12.sp,
                        color = CrimsonRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Strict Manual Control Notice
                Surface(
                    shape = RoundedCornerShape(6.dp),
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
                            text = "Strict Manual Placement: Orders NEVER execute automatically. You must manually confirm every trade.",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(quantity) },
                enabled = hasSufficientBalance && hasSufficientShares,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (action == SignalType.BUY) (if (isLive) LiveMarketGreen else EmeraldGreen) else CrimsonRed
                ),
                modifier = Modifier.testTag("confirm_order_button")
            ) {
                Text(
                    text = if (isLive) "Place Real ${action.name}" else "Execute Paper ${action.name}",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreatePriceAlertDialog(
    stock: StockQuote,
    onDismiss: () -> Unit,
    onConfirm: (targetPrice: Double, condition: AlertCondition) -> Unit
) {
    var targetPriceStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", stock.currentPrice * 1.02)) }
    var condition by remember { mutableStateOf(AlertCondition.ABOVE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Text(
                text = "Set Price Alert: ${stock.symbol}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Current LTP: ${stock.formattedPrice}",
                    fontSize = 13.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = targetPriceStr,
                    onValueChange = { targetPriceStr = it },
                    label = { Text("Target Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Column {
                    Text("Trigger Condition", fontSize = 13.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = condition == AlertCondition.ABOVE,
                            onClick = { condition = AlertCondition.ABOVE },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                        )
                        Text("Crosses Above", fontSize = 13.sp, color = TextPrimary, modifier = Modifier.clickable { condition = AlertCondition.ABOVE })

                        Spacer(modifier = Modifier.width(16.dp))

                        RadioButton(
                            selected = condition == AlertCondition.BELOW,
                            onClick = { condition = AlertCondition.BELOW },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                        )
                        Text("Crosses Below", fontSize = 13.sp, color = TextPrimary, modifier = Modifier.clickable { condition = AlertCondition.BELOW })
                    }
                }

                Text(
                    text = "When triggered, an in-app event & WhatsApp notification will be dispatched.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = targetPriceStr.toDoubleOrNull() ?: stock.currentPrice
                    onConfirm(price, condition)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                modifier = Modifier.testTag("save_alert_button")
            ) {
                Text("Create Alert", color = NavyDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
