package com.example.domain.ml.tflite

import com.example.data.model.CandleStick
import com.example.data.model.Exchange
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * =========================================================================
 * 1. HISTORICAL MARKET DATA STRUCTURES
 * =========================================================================
 */

/**
 * Represents a single time-series OHLCV market data bar with optional volume-weighted
 * and open-interest metrics from Angel One SmartAPI / Market Feeds.
 */
data class HistoricalMarketDataPoint(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val vwap: Double? = null,
    val openInterest: Long? = null
) {
    val isBullish: Boolean get() = close >= open
    val isBearish: Boolean get() = close < open
    val priceSpread: Double get() = high - low
    val bodySpread: Double get() = kotlin.math.abs(close - open)

    /**
     * Converts to legacy CandleStick representation for compatibility
     */
    fun toCandleStick(): CandleStick = CandleStick(
        timestamp = timestamp,
        open = open,
        high = high,
        low = low,
        close = close,
        volume = volume
    )

    companion object {
        fun fromCandleStick(candle: CandleStick): HistoricalMarketDataPoint = HistoricalMarketDataPoint(
            timestamp = candle.timestamp,
            open = candle.open,
            high = candle.high,
            low = candle.low,
            close = candle.close,
            volume = candle.volume
        )
    }
}

/**
 * Encapsulates a complete historical series for an asset with metadata and summary aggregations.
 */
data class HistoricalMarketDataSeries(
    val symbol: String,
    val exchange: Exchange = Exchange.NSE,
    val token: String = "",
    val interval: String = "1d", // "1m", "5m", "15m", "1d"
    val candles: List<HistoricalMarketDataPoint>,
    val fetchedTimestamp: Long = System.currentTimeMillis()
) {
    val size: Int get() = candles.size
    val minPrice: Double get() = candles.minOfOrNull { it.low } ?: 0.0
    val maxPrice: Double get() = candles.maxOfOrNull { it.high } ?: 0.0
    val totalVolume: Long get() = candles.sumOf { it.volume }
    val lastClose: Double get() = candles.lastOrNull()?.close ?: 0.0
    val firstOpen: Double get() = candles.firstOrNull()?.open ?: 0.0
    val periodReturnPercent: Double
        get() = if (firstOpen > 0) ((lastClose - firstOpen) / firstOpen) * 100.0 else 0.0
}

/**
 * Feature scaling parameters utilized for Min-Max normalization and inverse transformation.
 */
data class TensorScalingParameters(
    val minPrice: Double,
    val maxPrice: Double,
    val priceRange: Double,
    val baselineClose: Double,
    val averageVolume: Double,
    val rsiRangeMin: Double = 0.0,
    val rsiRangeMax: Double = 100.0
)

/**
 * =========================================================================
 * 2. TENSORFLOW LITE LSTM INPUT TENSOR DATA CLASS
 * =========================================================================
 */

/**
 * Data class representing the 3D Input Tensor required by the TensorFlow Lite LSTM model.
 * Shape: [batchSize, sequenceLength, featureCount] = [1, 30, 8]
 * Data Type: Float32 (32-bit floating point, 4 bytes per element)
 */
data class TFLiteLSTMInputTensor(
    val batchSize: Int = TFLiteModelConfig.BATCH_SIZE,                 // 1
    val sequenceLength: Int = TFLiteModelConfig.LOOKBACK_SEQUENCE_LENGTH, // 30
    val featureCount: Int = TFLiteModelConfig.FEATURE_COUNT,           // 8
    val featureMatrix: Array<FloatArray>,                              // Matrix [30][8]
    val byteBuffer: ByteBuffer,                                        // Direct ByteBuffer for TFLite C++ engine
    val scalingParams: TensorScalingParameters,
    val featureNames: List<String> = listOf(
        "Normalized Open",
        "Normalized High",
        "Normalized Low",
        "Normalized Close",
        "Normalized Volume",
        "RSI (14)",
        "MACD Histogram",
        "EMA (21) Deviation"
    ),
    val createdTimestamp: Long = System.currentTimeMillis()
) {
    val tensorShape: String get() = "[$batchSize, $sequenceLength, $featureCount]"
    val expectedElementCount: Int get() = batchSize * sequenceLength * featureCount
    val bufferSizeBytes: Int get() = expectedElementCount * 4 // Float32 is 4 bytes

    /**
     * Validates that the dimensions and ByteBuffer match the TensorFlow Lite model specifications
     */
    fun validate(): Boolean {
        if (featureMatrix.size != sequenceLength) return false
        if (featureMatrix.any { it.size != featureCount }) return false
        if (byteBuffer.capacity() < bufferSizeBytes) return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TFLiteLSTMInputTensor
        if (batchSize != other.batchSize) return false
        if (sequenceLength != other.sequenceLength) return false
        if (featureCount != other.featureCount) return false
        if (!featureMatrix.contentDeepEquals(other.featureMatrix)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = batchSize
        result = 31 * result + sequenceLength
        result = 31 * result + featureCount
        result = 31 * result + featureMatrix.contentDeepHashCode()
        return result
    }
}

/**
 * =========================================================================
 * 3. TENSORFLOW LITE LSTM OUTPUT TENSOR DATA CLASS
 * =========================================================================
 */

/**
 * Forecast output for an individual multi-step horizon (e.g. T+1 Day, T+3 Days, etc.)
 */
data class TFLiteLSTMOutputHorizon(
    val stepIndex: Int,
    val horizonLabel: String,         // e.g. "T+1 Day", "T+2 Days", "T+3 Days", "T+5 Days", "T+7 Days"
    val predictedDeltaPercent: Float, // Model output activation (delta percentage)
    val denormalizedPrice: Double,    // Denormalized projected price in currency units
    val lowerRiskBound: Double,       // Volatility channel lower bound
    val upperRiskBound: Double        // Volatility channel upper bound
)

/**
 * Data class representing the Output Tensor produced by the TensorFlow Lite LSTM model.
 * Shape: [batchSize, forecastSteps] = [1, 5]
 * Data Type: Float32
 */
data class TFLiteLSTMOutputTensor(
    val batchSize: Int = TFLiteModelConfig.BATCH_SIZE,                 // 1
    val forecastSteps: Int = TFLiteModelConfig.FORECAST_STEPS,         // 5
    val rawOutputDeltas: FloatArray,                                   // Raw output array [5]
    val horizons: List<TFLiteLSTMOutputHorizon>,                       // Denormalized horizons
    val inferenceLatencyMs: Long,                                      // Execution duration in ms
    val executionDelegate: String,                                     // "TFLite XNNPACK", "CPU Kernel", etc.
    val isHardwareAccelerated: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    val tensorShape: String get() = "[$batchSize, $forecastSteps]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TFLiteLSTMOutputTensor
        if (batchSize != other.batchSize) return false
        if (forecastSteps != other.forecastSteps) return false
        if (!rawOutputDeltas.contentEquals(other.rawOutputDeltas)) return false
        if (horizons != other.horizons) return false
        return true
    }

    override fun hashCode(): Int {
        var result = batchSize
        result = 31 * result + forecastSteps
        result = 31 * result + rawOutputDeltas.contentHashCode()
        result = 31 * result + horizons.hashCode()
        return result
    }
}
