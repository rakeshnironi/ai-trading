package com.example.domain.ml

import android.content.Context
import com.example.data.model.CandleStick
import com.example.data.model.MLPredictionResult
import com.example.domain.ml.tflite.TFLiteTimeSeriesForecaster

object LSTMPredictionEngine {

    private var forecaster: TFLiteTimeSeriesForecaster = TFLiteTimeSeriesForecaster()

    fun initContext(context: Context) {
        forecaster.close()
        forecaster = TFLiteTimeSeriesForecaster(context)
    }

    /**
     * Executes TensorFlow Lite LSTM inference pipeline for financial time-series forecasting
     * using real-time market data received from Angel One SmartAPI.
     */
    fun predictStockPrice(
        symbol: String,
        currentPrice: Double,
        candles: List<CandleStick>,
        horizon: String = "3 Days"
    ): MLPredictionResult {
        return forecaster.forecast(
            symbol = symbol,
            currentPrice = currentPrice,
            candles = candles,
            selectedHorizon = horizon
        )
    }
}

