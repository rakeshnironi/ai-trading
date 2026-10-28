package com.example.domain.ml.tflite

import com.example.data.model.CandleStick
import com.example.domain.indicators.TechnicalIndicatorCalculator
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

/**
 * Feature engineering pipeline for Angel One SmartAPI OHLCV data.
 * Transforms raw time-series candles into normalized tensor input for the TFLite LSTM model.
 */
class MarketDataFeatureExtractor {

    data class ExtractedTensor(
        val byteBuffer: ByteBuffer,
        val floatArray: Array<FloatArray>, // [30][8]
        val minPrice: Double,
        val maxPrice: Double,
        val lastClose: Double,
        val avgVolume: Double
    )

    /**
     * Extracts and normalizes the last 30 time-steps into a Float32 ByteBuffer tensor [1, 30, 8]
     */
    fun extractFeatures(
        symbol: String,
        currentPrice: Double,
        candles: List<CandleStick>
    ): ExtractedTensor {
        val windowSize = TFLiteModelConfig.LOOKBACK_SEQUENCE_LENGTH
        val featureCount = TFLiteModelConfig.FEATURE_COUNT

        // Ensure we have at least windowSize candles; pad if fewer
        val sequenceCandles = if (candles.size >= windowSize) {
            candles.takeLast(windowSize)
        } else {
            val paddingNeeded = windowSize - candles.size
            val firstCandle = candles.firstOrNull() ?: CandleStick(
                timestamp = System.currentTimeMillis(),
                open = currentPrice,
                high = currentPrice * 1.01,
                low = currentPrice * 0.99,
                close = currentPrice,
                volume = 500000L
            )
            List(paddingNeeded) { firstCandle } + candles
        }

        // Calculate technical indicators across the candle history
        val indicators = TechnicalIndicatorCalculator.calculateIndicators(symbol, candles)

        // Compute local price scale bounds for Min-Max normalization
        var minP = Double.MAX_VALUE
        var maxP = Double.MIN_VALUE
        var totalVol = 0.0

        for (c in sequenceCandles) {
            minP = min(minP, c.low)
            maxP = max(maxP, c.high)
            totalVol += c.volume
        }
        if (minP >= maxP) {
            minP = currentPrice * 0.95
            maxP = currentPrice * 1.05
        }
        val priceRange = (maxP - minP).coerceAtLeast(1.0)
        val avgVol = (totalVol / sequenceCandles.size).coerceAtLeast(1.0)

        // Allocate direct ByteBuffer for TensorFlow Lite C-API (Float32 = 4 bytes)
        val byteBuffer = ByteBuffer.allocateDirect(1 * windowSize * featureCount * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        val floatMatrix = Array(windowSize) { FloatArray(featureCount) }

        for (t in sequenceCandles.indices) {
            val candle = sequenceCandles[t]

            // 1. Normalized Open, High, Low, Close relative to min-max window
            val normOpen = ((candle.open - minP) / priceRange).toFloat().coerceIn(0f, 1f)
            val normHigh = ((candle.high - minP) / priceRange).toFloat().coerceIn(0f, 1f)
            val normLow = ((candle.low - minP) / priceRange).toFloat().coerceIn(0f, 1f)
            val normClose = ((candle.close - minP) / priceRange).toFloat().coerceIn(0f, 1f)

            // 2. Normalized Volume relative to moving average
            val normVolume = (candle.volume / (avgVol * 2.5)).toFloat().coerceIn(0f, 2f)

            // 3. Normalized RSI (bounded -1.0 to 1.0)
            val normRsi = (((indicators.rsi - 50.0) / 50.0)).toFloat().coerceIn(-1f, 1f)

            // 4. Normalized MACD histogram
            val normMacd = if (indicators.sma20 > 0) {
                ((indicators.macdHistogram / indicators.sma20) * 50.0).toFloat().coerceIn(-1f, 1f)
            } else 0f

            // 5. Normalized EMA deviation
            val normEmaDev = if (indicators.ema21 > 0) {
                ((candle.close - indicators.ema21) / indicators.ema21 * 20.0).toFloat().coerceIn(-1f, 1f)
            } else 0f

            floatMatrix[t][TFLiteModelConfig.FEAT_NORM_OPEN] = normOpen
            floatMatrix[t][TFLiteModelConfig.FEAT_NORM_HIGH] = normHigh
            floatMatrix[t][TFLiteModelConfig.FEAT_NORM_LOW] = normLow
            floatMatrix[t][TFLiteModelConfig.FEAT_NORM_CLOSE] = normClose
            floatMatrix[t][TFLiteModelConfig.FEAT_NORM_VOLUME] = normVolume
            floatMatrix[t][TFLiteModelConfig.FEAT_RSI] = normRsi
            floatMatrix[t][TFLiteModelConfig.FEAT_MACD_HIST] = normMacd
            floatMatrix[t][TFLiteModelConfig.FEAT_EMA_DEV] = normEmaDev

            // Write to ByteBuffer sequentially
            for (f in 0 until featureCount) {
                byteBuffer.putFloat(floatMatrix[t][f])
            }
        }

        byteBuffer.rewind()

        return ExtractedTensor(
            byteBuffer = byteBuffer,
            floatArray = floatMatrix,
            minPrice = minP,
            maxPrice = maxP,
            lastClose = sequenceCandles.last().close,
            avgVolume = avgVol
        )
    }

    /**
     * Extracts and packages the normalized tensor into a fully validated TFLiteLSTMInputTensor
     */
    fun extractInputTensor(
        symbol: String,
        currentPrice: Double,
        candles: List<CandleStick>
    ): TFLiteLSTMInputTensor {
        val extracted = extractFeatures(symbol, currentPrice, candles)
        val scalingParams = TensorScalingParameters(
            minPrice = extracted.minPrice,
            maxPrice = extracted.maxPrice,
            priceRange = extracted.maxPrice - extracted.minPrice,
            baselineClose = extracted.lastClose,
            averageVolume = extracted.avgVolume
        )
        return TFLiteLSTMInputTensor(
            batchSize = TFLiteModelConfig.BATCH_SIZE,
            sequenceLength = TFLiteModelConfig.LOOKBACK_SEQUENCE_LENGTH,
            featureCount = TFLiteModelConfig.FEATURE_COUNT,
            featureMatrix = extracted.floatArray,
            byteBuffer = extracted.byteBuffer,
            scalingParams = scalingParams
        )
    }

    /**
     * Overload for HistoricalMarketDataSeries
     */
    fun extractFromSeries(
        series: HistoricalMarketDataSeries,
        currentPrice: Double
    ): TFLiteLSTMInputTensor {
        val candles = series.candles.map { it.toCandleStick() }
        return extractInputTensor(series.symbol, currentPrice, candles)
    }

    /**
     * Inverses normalized model output activations back to real stock prices in INR
     */
    fun inverseTransform(
        rawOutputDeltas: FloatArray,
        lastPrice: Double,
        minPrice: Double,
        maxPrice: Double
    ): List<Double> {
        val priceRange = maxPrice - minPrice
        return rawOutputDeltas.map { deltaPercent ->
            // Model predicts percentage change relative to current price
            val projected = lastPrice * (1.0 + (deltaPercent.toDouble() / 100.0))
            projected.coerceAtLeast(lastPrice * 0.5)
        }
    }
}
