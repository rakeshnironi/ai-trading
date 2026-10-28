package com.example.domain.ml.tflite

/**
 * Configuration & Metadata specification for the TensorFlow Lite LSTM Model
 */
object TFLiteModelConfig {
    const val MODEL_ASSET_NAME = "lstm_financial_forecaster.tflite"

    // Time-series tensor dimensions
    const val BATCH_SIZE = 1
    const val LOOKBACK_SEQUENCE_LENGTH = 30 // 30 historical candle periods
    const val FEATURE_COUNT = 8              // 8 normalized engineered features per timestep
    const val FORECAST_STEPS = 5             // Multi-horizon output: T+1, T+2, T+3, T+5, T+7 periods

    // Feature indices in the tensor
    const val FEAT_NORM_OPEN = 0
    const val FEAT_NORM_HIGH = 1
    const val FEAT_NORM_LOW = 2
    const val FEAT_NORM_CLOSE = 3
    const val FEAT_NORM_VOLUME = 4
    const val FEAT_RSI = 5
    const val FEAT_MACD_HIST = 6
    const val FEAT_EMA_DEV = 7

    const val MODEL_ARCHITECTURE = "Stacked Bidirectional LSTM (128 units -> 64 units) + Dropout(0.2) + Dense(5)"
    const val INPUT_TENSOR_NAME = "serving_default_lstm_input:0"
    const val OUTPUT_TENSOR_NAME = "StatefulPartitionedCall:0"
    const val INPUT_SHAPE = "[1, 30, 8]"
    const val OUTPUT_SHAPE = "[1, 5]"
    const val PRECISION = "Float32 (32-bit floating point)"
}
