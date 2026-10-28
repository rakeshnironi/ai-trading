package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CandleStick
import com.example.data.model.PriceTickDirection
import com.example.data.model.SignalType
import com.example.data.model.StockQuote
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class ChartType {
    CANDLESTICK, LINE, AREA
}

/**
 * Modern High-Performance Interactive Candlestick Chart Component.
 * Supports smooth pinch-to-zoom, horizontal panning, live candle pulses,
 * dynamic crosshairs with HUD inspector, and 1-tap enlargement.
 */
@Composable
fun InteractiveTradingChart(
    candles: List<CandleStick>,
    modifier: Modifier = Modifier,
    chartType: ChartType = ChartType.CANDLESTICK,
    showSma: Boolean = true,
    showEma: Boolean = true,
    showBollingerBands: Boolean = false,
    showVolume: Boolean = true,
    isEnlargedMode: Boolean = false,
    onEnlargeClick: (() -> Unit)? = null
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(if (isEnlargedMode) 380.dp else 260.dp)
                .background(NavyCard, RoundedCornerShape(16.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = CyanAccent,
                    strokeWidth = 2.dp
                )
                Text("Loading Real-Time Chart...", color = TextSecondary, fontSize = 13.sp)
            }
        }
        return
    }

    // Zoom and Pan State
    // visibleCount controls the number of candles displayed (zoom level)
    val defaultVisible = if (isEnlargedMode) 35f else 30f
    var visibleCount by remember(candles.size) { mutableFloatStateOf(defaultVisible.coerceAtMost(candles.size.toFloat())) }
    var panOffsetRatio by remember { mutableFloatStateOf(0f) } // 0f = latest candles (right aligned)

    // Scrubbing / Crosshair State
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var touchX by remember { mutableStateOf<Float?>(null) }
    var touchY by remember { mutableStateOf<Float?>(null) }

    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.US) }
    val activeCandle = selectedIndex?.let { if (it in candles.indices) candles[it] else candles.last() } ?: candles.last()

    // Real-Time Live Candle Pulse Transition
    val infiniteTransition = rememberInfiniteTransition(label = "live_candle_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Calculate window of visible candles based on visibleCount and panOffset
    val countInt = visibleCount.roundToInt().coerceIn(8, candles.size)
    val maxPan = (candles.size - countInt).coerceAtLeast(0)
    val currentPan = (panOffsetRatio * maxPan).roundToInt().coerceIn(0, maxPan)
    val startIndex = (candles.size - countInt - currentPan).coerceIn(0, candles.size - countInt)
    val endIndex = (startIndex + countInt).coerceIn(startIndex + 1, candles.size)
    val windowedCandles = remember(candles, startIndex, endIndex) {
        candles.subList(startIndex, endIndex)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NavyCard, RoundedCornerShape(16.dp))
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .padding(if (isEnlargedMode) 10.dp else 14.dp)
    ) {
        // Top Info & HUD Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (activeCandle.isBullish) EmeraldGreen else CrimsonRed, CircleShape)
                    )
                    Text(
                        text = "LIVE ${sdf.format(Date(activeCandle.timestamp))}",
                        style = TextStyle(fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                    )
                    if (selectedIndex != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "INSPECTOR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("O: ₹${String.format(Locale.US, "%.1f", activeCandle.open)}", fontSize = 11.sp, color = TextSecondary)
                    Text("H: ₹${String.format(Locale.US, "%.1f", activeCandle.high)}", fontSize = 11.sp, color = EmeraldGreen)
                    Text("L: ₹${String.format(Locale.US, "%.1f", activeCandle.low)}", fontSize = 11.sp, color = CrimsonRed)
                    Text(
                        text = "C: ₹${String.format(Locale.US, "%.1f", activeCandle.close)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeCandle.isBullish) EmeraldGreen else CrimsonRed
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (activeCandle.isBullish) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (activeCandle.isBullish) "BULLISH" else "BEARISH",
                        color = if (activeCandle.isBullish) EmeraldGreen else CrimsonRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (onEnlargeClick != null && !isEnlargedMode) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyanAccent.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onEnlargeClick() }
                            .testTag("chart_enlarge_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Enlarge Chart",
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Enlarge & Zoom",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                    }
                }
            }
        }

        // Interactive Canvas Container with Multi-Touch Pinch Zoom + Pan + Scrubbing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isEnlargedMode) 340.dp else 230.dp)
                .clickable(enabled = onEnlargeClick != null && !isEnlargedMode) {
                    onEnlargeClick?.invoke()
                }
                .pointerInput(windowedCandles) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Smooth pinch zoom
                        if (zoom != 1f) {
                            val newCount = (visibleCount / zoom).coerceIn(8f, candles.size.toFloat())
                            visibleCount = newCount
                        }
                        // Smooth horizontal pan
                        if (pan.x != 0f) {
                            val panDelta = -pan.x / size.width
                            panOffsetRatio = (panOffsetRatio + panDelta).coerceIn(0f, 1f)
                        }
                    }
                }
                .pointerInput(windowedCandles) {
                    detectTapGestures(
                        onPress = { offset ->
                            if (windowedCandles.isNotEmpty()) {
                                val slotWidth = size.width / windowedCandles.size
                                val slotIndex = (offset.x / slotWidth).toInt().coerceIn(0, windowedCandles.size - 1)
                                val globalIdx = startIndex + slotIndex
                                selectedIndex = globalIdx.coerceIn(0, candles.size - 1)
                                touchX = offset.x
                                touchY = offset.y
                            }
                            tryAwaitRelease()
                            selectedIndex = null
                            touchX = null
                            touchY = null
                        },
                        onDoubleTap = {
                            if (onEnlargeClick != null) {
                                onEnlargeClick()
                            } else {
                                // Toggle zoom on double tap in enlarged mode
                                visibleCount = if (visibleCount > 20f) 12f else defaultVisible
                            }
                        }
                    )
                }
        ) {
            val chartBorderColor = NavyBorder
            val chartAccentColor = CyanAccent
            val chartTextPrimaryColor = TextPrimary
            val chartBullishColor = EmeraldGreen
            val chartBearishColor = CrimsonRed
            val textMutedColor = TextMuted

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("market_chart_canvas")
            ) {
                val width = size.width
                val height = size.height
                val priceScaleWidth = 52.dp.toPx()
                val activeWidth = width - priceScaleWidth
                val volumeAreaHeight = if (showVolume) height * 0.18f else 0f
                val chartAreaHeight = height - volumeAreaHeight - 14f

                if (windowedCandles.isEmpty()) return@Canvas

                val minPrice = windowedCandles.minOf { it.low } * 0.9985
                val maxPrice = windowedCandles.maxOf { it.high } * 1.0015
                val priceRange = if (maxPrice > minPrice) maxPrice - minPrice else 1.0
                val maxVolume = windowedCandles.maxOf { it.volume }.coerceAtLeast(1L)

                val slotWidth = activeWidth / windowedCandles.size
                val candleWidth = slotWidth
                val bodyWidth = (candleWidth * 0.68f).coerceIn(2.5f, 22f)

                fun priceToY(price: Double): Float {
                    return (chartAreaHeight - ((price - minPrice) / priceRange * chartAreaHeight)).toFloat().coerceIn(4f, chartAreaHeight)
                }

                // 1. Grid Lines and Y-Axis Price Labels
                val gridLines = 4
                val textPaint = Paint().apply {
                    color = textMutedColor.toArgb()
                    textSize = 9.sp.toPx()
                    isAntiAlias = true
                    typeface = Typeface.DEFAULT_BOLD
                }

                for (i in 0..gridLines) {
                    val y = (chartAreaHeight / gridLines) * i
                    val priceAtY = maxPrice - (i.toDouble() / gridLines * priceRange)

                    // Dashed horizontal grid line
                    drawLine(
                        color = chartBorderColor.copy(alpha = 0.45f),
                        start = Offset(0f, y),
                        end = Offset(activeWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Y-Axis Price Label on right edge
                    drawContext.canvas.nativeCanvas.drawText(
                        String.format(Locale.US, "%.1f", priceAtY),
                        activeWidth + 6.dp.toPx(),
                        y + 3.dp.toPx(),
                        textPaint
                    )
                }

                // Vertical separator for price scale
                drawLine(
                    color = chartBorderColor.copy(alpha = 0.5f),
                    start = Offset(activeWidth, 0f),
                    end = Offset(activeWidth, height),
                    strokeWidth = 1f
                )

                // 2. Bollinger Bands
                if (showBollingerBands && windowedCandles.size > 8) {
                    val upperPath = Path()
                    val lowerPath = Path()
                    val bandFillPath = Path()
                    val period = 14.coerceAtMost(windowedCandles.size)

                    for (i in windowedCandles.indices) {
                        val slice = windowedCandles.take(i + 1).takeLast(period).map { it.close }
                        val avg = slice.average()
                        val std = kotlin.math.sqrt(slice.map { (it - avg) * (it - avg) }.average())
                        val up = avg + (2.0 * std)
                        val down = avg - (2.0 * std)

                        val x = (i * slotWidth) + (slotWidth / 2)
                        val yUp = priceToY(up)
                        val yDown = priceToY(down)

                        if (i == 0) {
                            upperPath.moveTo(x, yUp)
                            lowerPath.moveTo(x, yDown)
                            bandFillPath.moveTo(x, yUp)
                        } else {
                            upperPath.lineTo(x, yUp)
                            lowerPath.lineTo(x, yDown)
                            bandFillPath.lineTo(x, yUp)
                        }
                    }

                    for (i in windowedCandles.indices.reversed()) {
                        val slice = windowedCandles.take(i + 1).takeLast(period).map { it.close }
                        val avg = slice.average()
                        val std = kotlin.math.sqrt(slice.map { (it - avg) * (it - avg) }.average())
                        val down = avg - (2.0 * std)
                        val x = (i * slotWidth) + (slotWidth / 2)
                        val yDown = priceToY(down)
                        bandFillPath.lineTo(x, yDown)
                    }
                    bandFillPath.close()

                    drawPath(bandFillPath, color = chartAccentColor.copy(alpha = 0.06f))
                    drawPath(upperPath, color = chartAccentColor.copy(alpha = 0.45f), style = Stroke(width = 1.2f))
                    drawPath(lowerPath, color = chartAccentColor.copy(alpha = 0.45f), style = Stroke(width = 1.2f))
                }

                // 3. Technical Indicators: SMA (10) & EMA (9)
                if (showSma && windowedCandles.size >= 4) {
                    val smaPath = Path()
                    for (i in windowedCandles.indices) {
                        val slice = windowedCandles.take(i + 1).takeLast(10).map { it.close }
                        val sma = slice.average()
                        val x = (i * slotWidth) + (slotWidth / 2)
                        val y = priceToY(sma)
                        if (i == 0) smaPath.moveTo(x, y) else smaPath.lineTo(x, y)
                    }
                    drawPath(smaPath, color = Color(0xFFFFD54F), style = Stroke(width = 1.8f))
                }

                if (showEma && windowedCandles.size >= 4) {
                    val emaPath = Path()
                    var ema = windowedCandles.first().close
                    val k = 2.0 / (9 + 1)
                    for (i in windowedCandles.indices) {
                        ema = (windowedCandles[i].close * k) + (ema * (1.0 - k))
                        val x = (i * slotWidth) + (slotWidth / 2)
                        val y = priceToY(ema)
                        if (i == 0) emaPath.moveTo(x, y) else emaPath.lineTo(x, y)
                    }
                    drawPath(emaPath, color = Color(0xFFB388FF), style = Stroke(width = 1.8f))
                }

                // 4. Candlesticks or Line / Area Chart
                when (chartType) {
                    ChartType.CANDLESTICK -> {
                        for (i in windowedCandles.indices) {
                            val c = windowedCandles[i]
                            val centerX = (i * slotWidth) + (slotWidth / 2)
                            val candleColor = if (c.isBullish) chartBullishColor else chartBearishColor

                            // Upper and Lower Wick
                            val highY = priceToY(c.high)
                            val lowY = priceToY(c.low)
                            drawLine(
                                color = candleColor,
                                start = Offset(centerX, highY),
                                end = Offset(centerX, lowY),
                                strokeWidth = if (bodyWidth > 10f) 2f else 1.4f
                            )

                            // Candlestick Real Body
                            val openY = priceToY(c.open)
                            val closeY = priceToY(c.close)
                            val topY = min(openY, closeY)
                            val bodyH = max(abs(openY - closeY), 2.5f)

                            drawRoundRect(
                                color = candleColor,
                                topLeft = Offset(centerX - (bodyWidth / 2), topY),
                                size = Size(bodyWidth, bodyH),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )

                            // Volume Bar at Bottom
                            if (showVolume) {
                                val volH = ((c.volume.toFloat() / maxVolume) * volumeAreaHeight).coerceAtLeast(2f)
                                val volTopY = height - volH
                                drawRect(
                                    color = candleColor.copy(alpha = 0.35f),
                                    topLeft = Offset(centerX - (bodyWidth / 2), volTopY),
                                    size = Size(bodyWidth, volH)
                                )
                            }
                        }

                        // Live Latest Candle Reference Line & Pulse
                        if (windowedCandles.isNotEmpty() && endIndex == candles.size) {
                            val lastCandle = windowedCandles.last()
                            val lastY = priceToY(lastCandle.close)
                            val lastCenterX = ((windowedCandles.size - 1) * slotWidth) + (slotWidth / 2)
                            val liveColor = if (lastCandle.isBullish) chartBullishColor else chartBearishColor

                            // Horizontal LTP dashed reference line
                            drawLine(
                                color = liveColor.copy(alpha = 0.75f),
                                start = Offset(0f, lastY),
                                end = Offset(activeWidth, lastY),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                            )

                            // Pulsing glow aura at the live candle close price
                            drawCircle(
                                color = liveColor.copy(alpha = pulseAlpha),
                                radius = pulseRadius + 4f,
                                center = Offset(lastCenterX, lastY)
                            )
                            drawCircle(
                                color = liveColor,
                                radius = 3.5f,
                                center = Offset(lastCenterX, lastY)
                            )

                            // Live LTP badge on right Y-axis
                            drawRoundRect(
                                color = liveColor,
                                topLeft = Offset(activeWidth + 2.dp.toPx(), lastY - 8.dp.toPx()),
                                size = Size(priceScaleWidth - 4.dp.toPx(), 16.dp.toPx()),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                            val ltpTextPaint = Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 8.5f.sp.toPx()
                                isAntiAlias = true
                                typeface = Typeface.DEFAULT_BOLD
                            }
                            drawContext.canvas.nativeCanvas.drawText(
                                String.format(Locale.US, "%.1f", lastCandle.close),
                                activeWidth + 6.dp.toPx(),
                                lastY + 4.dp.toPx(),
                                ltpTextPaint
                            )
                        }
                    }

                    ChartType.LINE, ChartType.AREA -> {
                        val linePath = Path()
                        val areaPath = Path()

                        for (i in windowedCandles.indices) {
                            val c = windowedCandles[i]
                            val x = (i * slotWidth) + (slotWidth / 2)
                            val y = priceToY(c.close)

                            if (i == 0) {
                                linePath.moveTo(x, y)
                                areaPath.moveTo(x, chartAreaHeight)
                                areaPath.lineTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                                areaPath.lineTo(x, y)
                            }

                            if (i == windowedCandles.size - 1) {
                                areaPath.lineTo(x, chartAreaHeight)
                                areaPath.close()
                            }
                        }

                        if (chartType == ChartType.AREA) {
                            drawPath(
                                areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(chartAccentColor.copy(alpha = 0.28f), chartAccentColor.copy(alpha = 0.02f)),
                                    startY = 0f,
                                    endY = chartAreaHeight
                                )
                            )
                        }
                        drawPath(linePath, color = chartAccentColor, style = Stroke(width = 2.4f))

                        if (showVolume) {
                            for (i in windowedCandles.indices) {
                                val c = windowedCandles[i]
                                val centerX = (i * slotWidth) + (slotWidth / 2)
                                val volH = ((c.volume.toFloat() / maxVolume) * volumeAreaHeight).coerceAtLeast(2f)
                                val volTopY = height - volH
                                drawRect(
                                    color = (if (c.isBullish) chartBullishColor else chartBearishColor).copy(alpha = 0.25f),
                                    topLeft = Offset(centerX - (bodyWidth / 2), volTopY),
                                    size = Size(bodyWidth, volH)
                                )
                            }
                        }

                        if (windowedCandles.isNotEmpty() && endIndex == candles.size) {
                            val lastCandle = windowedCandles.last()
                            val lastY = priceToY(lastCandle.close)
                            val lastX = ((windowedCandles.size - 1) * slotWidth) + (slotWidth / 2)

                            drawLine(
                                color = chartAccentColor.copy(alpha = 0.75f),
                                start = Offset(0f, lastY),
                                end = Offset(activeWidth, lastY),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                            )
                            drawCircle(
                                color = chartAccentColor.copy(alpha = pulseAlpha),
                                radius = pulseRadius + 4f,
                                center = Offset(lastX, lastY)
                            )
                            drawCircle(
                                color = chartAccentColor,
                                radius = 4f,
                                center = Offset(lastX, lastY)
                            )
                        }
                    }
                }

                // 5. Crosshair Cursor & Value Badges on Touch Drag
                touchX?.let { tx ->
                    if (tx <= activeWidth) {
                        val clampedX = tx.coerceIn(0f, activeWidth)
                        val slotIdx = (clampedX / slotWidth).toInt().coerceIn(0, windowedCandles.size - 1)
                        val targetCandle = windowedCandles[slotIdx]
                        val targetY = priceToY(targetCandle.close)

                        // Vertical Dashed Crosshair Line
                        drawLine(
                            color = chartTextPrimaryColor.copy(alpha = 0.75f),
                            start = Offset(clampedX, 0f),
                            end = Offset(clampedX, height),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )

                        // Horizontal Dashed Crosshair Line
                        drawLine(
                            color = chartTextPrimaryColor.copy(alpha = 0.75f),
                            start = Offset(0f, targetY),
                            end = Offset(activeWidth, targetY),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )

                        // Crosshair Junction Dot
                        drawCircle(
                            color = chartAccentColor,
                            radius = 4.5f,
                            center = Offset(clampedX, targetY)
                        )

                        // Price tag badge on right axis for cursor
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(activeWidth + 2.dp.toPx(), targetY - 8.dp.toPx()),
                            size = Size(priceScaleWidth - 4.dp.toPx(), 16.dp.toPx()),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        val cursorPricePaint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 8.5f.sp.toPx()
                            isAntiAlias = true
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            String.format(Locale.US, "%.1f", targetCandle.close),
                            activeWidth + 6.dp.toPx(),
                            targetY + 4.dp.toPx(),
                            cursorPricePaint
                        )
                    }
                }
            }
        }

        // Bottom Indicator Legend Bar & Pinch Zoom Hint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showSma) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFFFD54F), CircleShape))
                        Text("SMA(10)", fontSize = 10.sp, color = TextMuted)
                    }
                }
                if (showEma) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFB388FF), CircleShape))
                        Text("EMA(9)", fontSize = 10.sp, color = TextMuted)
                    }
                }
                if (showBollingerBands) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(CyanAccent, CircleShape))
                        Text("Bollinger", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }

            // Gesture tip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Text(
                    text = if (isEnlargedMode) "Pinch to zoom • Drag to pan" else "Tap to enlarge & zoom",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * Dedicated Enlarged Live Candle Chart Studio Dialog (Groww / Angel One / TradingView style)
 * Fullscreen studio with multi-touch pinch-to-zoom, pan, indicator toggles, timeframe switcher,
 * and instant 1-tap Buy/Sell order placement.
 */
@Composable
fun EnlargedLiveCandleChartStudio(
    stock: StockQuote,
    candles: List<CandleStick>,
    selectedTimeframe: String,
    onTimeframeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onBuyClick: (() -> Unit)? = null,
    onSellClick: (() -> Unit)? = null
) {
    var chartType by remember { mutableStateOf(ChartType.CANDLESTICK) }
    var showSma by remember { mutableStateOf(true) }
    var showEma by remember { mutableStateOf(true) }
    var showBollinger by remember { mutableStateOf(true) }
    var showVolume by remember { mutableStateOf(true) }

    val timeframes = listOf("1 Minute", "5 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "1 Day", "1 Week")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDeep)
                .testTag("enlarged_chart_studio_dialog"),
            color = NavyDeep
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // 1. Studio Header Toolbar with Back, Symbol, Exchange, and Live LTP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .background(NavyCardElevated, CircleShape)
                                .border(1.dp, NavyBorder, CircleShape)
                                .testTag("close_enlarged_chart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Fullscreen",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stock.symbol,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CyanAccent.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = stock.exchange.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
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

                    // Live Price & Tick Badge
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(if (stock.isPositive) EmeraldGreen else CrimsonRed, CircleShape)
                            )
                            Text(
                                text = stock.formattedPrice,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${stock.formattedChange} (${stock.formattedPercent})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stock.isPositive) EmeraldGreen else CrimsonRed
                        )
                    }
                }

                // 2. Timeframe Bar (Groww / Angel One style pill selector)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(timeframes) { tf ->
                            val isSelected = selectedTimeframe == tf
                            val shortTf = tf.replace("Minutes", "m").replace("Minute", "m").replace("Hour", "h").replace("Day", "D").replace("Week", "W")
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CyanAccent else NavyCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) CyanAccent else NavyBorder
                                ),
                                modifier = Modifier
                                    .clickable { onTimeframeChange(tf) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = shortTf,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) NavyDeep else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Technical Indicator Toggles Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chart Type Toggle (Candles vs Line vs Area)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (chartType == ChartType.CANDLESTICK) CyanAccent.copy(alpha = 0.2f) else NavyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (chartType == ChartType.CANDLESTICK) CyanAccent else NavyBorder),
                        modifier = Modifier.clickable {
                            chartType = when (chartType) {
                                ChartType.CANDLESTICK -> ChartType.LINE
                                ChartType.LINE -> ChartType.AREA
                                ChartType.AREA -> ChartType.CANDLESTICK
                            }
                        }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (chartType) {
                                    ChartType.CANDLESTICK -> Icons.Default.CandlestickChart
                                    ChartType.LINE, ChartType.AREA -> Icons.AutoMirrored.Filled.ShowChart
                                },
                                contentDescription = null,
                                tint = if (chartType == ChartType.CANDLESTICK) CyanAccent else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (chartType) {
                                    ChartType.CANDLESTICK -> "Candles"
                                    ChartType.LINE -> "Line"
                                    ChartType.AREA -> "Area"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (chartType == ChartType.CANDLESTICK) CyanAccent else TextSecondary
                            )
                        }
                    }

                    // SMA Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showSma) Color(0xFFFFD54F).copy(alpha = 0.2f) else NavyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showSma) Color(0xFFFFD54F) else NavyBorder),
                        modifier = Modifier.clickable { showSma = !showSma }
                    ) {
                        Text(
                            text = "SMA(10)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showSma) Color(0xFFFFD54F) else TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    // EMA Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showEma) Color(0xFFB388FF).copy(alpha = 0.2f) else NavyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showEma) Color(0xFFB388FF) else NavyBorder),
                        modifier = Modifier.clickable { showEma = !showEma }
                    ) {
                        Text(
                            text = "EMA(9)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showEma) Color(0xFFB388FF) else TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    // Bollinger Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showBollinger) CyanAccent.copy(alpha = 0.2f) else NavyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showBollinger) CyanAccent else NavyBorder),
                        modifier = Modifier.clickable { showBollinger = !showBollinger }
                    ) {
                        Text(
                            text = "Bollinger",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showBollinger) CyanAccent else TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    // Volume Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showVolume) EmeraldGreen.copy(alpha = 0.2f) else NavyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showVolume) EmeraldGreen else NavyBorder),
                        modifier = Modifier.clickable { showVolume = !showVolume }
                    ) {
                        Text(
                            text = "Volume",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showVolume) EmeraldGreen else TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 4. Large Interactive Candlestick Canvas with Smooth Zoom & Pan
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    InteractiveTradingChart(
                        candles = candles,
                        chartType = chartType,
                        showSma = showSma,
                        showEma = showEma,
                        showBollingerBands = showBollinger,
                        showVolume = showVolume,
                        isEnlargedMode = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Groww / Angel One Quick 1-Tap Trading Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onBuyClick != null) {
                        Button(
                            onClick = onBuyClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("enlarged_chart_buy_button")
                        ) {
                            Text(
                                text = "BUY (LTP ₹${String.format(Locale.US, "%,.2f", stock.currentPrice)})",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (onSellClick != null) {
                        Button(
                            onClick = onSellClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("enlarged_chart_sell_button")
                        ) {
                            Text(
                                text = "SELL / SHORT",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
