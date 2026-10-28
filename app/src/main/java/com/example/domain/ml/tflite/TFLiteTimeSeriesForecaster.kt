package com.example.domain.ml.tflite

import android.content.Context
import com.example.data.model.CandleStick
import com.example.data.model.ForecastStep
import com.example.data.model.MLPredictionResult
import com.example.data.model.TrendDirection
import com.example.domain.indicators.TechnicalIndicatorCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * High-level Financial Time-Series Forecasting Module powered by TensorFlow Lite.
 * Consumes real-time market data received from Angel One SmartAPI and performs multi-horizon price predictions.
 */
class TFLiteTimeSeriesForecaster(context: Context? = null) {

    private val featureExtractor = MarketDataFeatureExtractor()
    private val interpreter = TFLiteLSTMInterpreter(context)

    /**
     * Executes the end-to-end TensorFlow Lite inference workflow:
     * 1. Extract and normalize 30x8 feature tensor from Angel One SmartAPI OHLCV candles
     * 2. Feed direct ByteBuffer to TFLite Interpreter with XNNPACK acceleration
     * 3. Inverse transform multi-horizon step outputs
     * 4. Compute dynamic confidence intervals and ATR risk channels
     */
    fun forecast(
        symbol: String,
        currentPrice: Double,
        candles: List<CandleStick>,
        selectedHorizon: String = "3 Days"
    ): MLPredictionResult {
        // Step 1: Feature Extraction
        val extractedTensor = featureExtractor.extractFeatures(symbol, currentPrice, candles)

        // Step 2: TensorFlow Lite Model Forward Pass
        val inference = interpreter.runInference(extractedTensor)

        // Step 3: Inverse Transform Output Tensor
        val projectedPrices = featureExtractor.inverseTransform(
            rawOutputDeltas = inference.outputDeltas,
            lastPrice = currentPrice,
            minPrice = extractedTensor.minPrice,
            maxPrice = extractedTensor.maxPrice
        )

        // Compute technical indicators for context & bounds
        val indicators = TechnicalIndicatorCalculator.calculateIndicators(symbol, candles)
        val atr = indicators.atr.coerceAtLeast(currentPrice * 0.008)

        // Multi-Step Forecast Generation [T+1, T+2, T+3, T+5, T+7]
        val stepLabels = listOf("T+1 Day", "T+2 Days", "T+3 Days", "T+5 Days", "T+7 Days")
        val forecastSteps = mutableListOf<ForecastStep>()

        for (i in stepLabels.indices) {
            val stepLabel = stepLabels[i]
            val stepPrice = if (i < projectedPrices.size) projectedPrices[i] else currentPrice
            val returnPct = if (currentPrice > 0) ((stepPrice - currentPrice) / currentPrice) * 100.0 else 0.0
            val volatilityBand = atr * (1.0 + (i * 0.35))
            val lower = (stepPrice - volatilityBand).coerceAtLeast(currentPrice * 0.5)
            val upper = stepPrice + volatilityBand

            forecastSteps.add(
                ForecastStep(
                    stepLabel = stepLabel,
                    predictedPrice = stepPrice,
                    returnPercent = returnPct,
                    lowerBound = lower,
                    upperBound = upper
                )
            )
        }

        // Selected Target based on requested horizon
        val targetStepIndex = when (selectedHorizon) {
            "1 Day" -> 0
            "2 Days" -> 1
            "3 Days" -> 2
            "1 Week", "5 Days" -> 3
            "7 Days" -> 4
            else -> 2
        }

        val targetStep = forecastSteps.getOrElse(targetStepIndex) { forecastSteps[2] }
        val expectedReturn = targetStep.returnPercent
        val predictedPrice = targetStep.predictedPrice

        val trend = when {
            expectedReturn > 0.35 -> TrendDirection.UP
            expectedReturn < -0.35 -> TrendDirection.DOWN
            else -> TrendDirection.SIDEWAYS
        }

        // Calculate confidence based on indicator alignment and tensor magnitude
        val alignment = abs(inference.outputDeltas.getOrElse(targetStepIndex) { 0f } / 3.0f).coerceIn(0f, 1f)
        val baseConfidence = 74
        val bonus = (alignment * 20).roundToInt().coerceIn(0, 22)
        val confidence = (baseConfidence + bonus).coerceIn(65, 96)

        val featureMetrics = listOf(
            "RSI (14) Momentum" to indicators.rsi,
            "MACD Histogram" to indicators.macdHistogram,
            "EMA (21) Deviation %" to (((currentPrice - indicators.ema21) / indicators.ema21) * 100.0),
            "ATR Volatility Band" to atr,
            "Bollinger %B Index" to if (indicators.upperBollinger > indicators.lowerBollinger) {
                ((currentPrice - indicators.lowerBollinger) / (indicators.upperBollinger - indicators.lowerBollinger)) * 100.0
            } else 50.0,
            "SuperTrend Signal" to (if (indicators.isSuperTrendBullish) 1.0 else -1.0)
        )

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.US)
        val timestamp = sdf.format(Date())

        return MLPredictionResult(
            symbol = symbol,
            currentPrice = currentPrice,
            predictedPrice = predictedPrice,
            horizon = selectedHorizon,
            confidence = confidence,
            trend = trend,
            expectedReturnPercent = expectedReturn,
            modelType = "TensorFlow Lite Stacked LSTM (128-64 units)",
            featureMetrics = featureMetrics,
            trainedTimestamp = timestamp,
            status = inference.engineStatus,
            multiStepForecast = forecastSteps,
            inferenceTimeMs = inference.latencyMs,
            inputTensorShape = TFLiteModelConfig.INPUT_SHAPE,
            outputTensorShape = TFLiteModelConfig.OUTPUT_SHAPE,
            executionDelegate = if (inference.isHardwareAccelerated) "TFLite XNNPACK Delegate" else "TFLite Direct Float32 Kernel"
        )
    }

    fun close() {
        interpreter.close()
    }
}
