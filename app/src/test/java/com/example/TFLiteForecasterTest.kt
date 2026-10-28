package com.example

import com.example.data.model.CandleStick
import com.example.domain.ml.tflite.MarketDataFeatureExtractor
import com.example.domain.ml.tflite.TFLiteModelConfig
import com.example.domain.ml.tflite.TFLiteTimeSeriesForecaster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TFLiteForecasterTest {

    @Test
    fun testFeatureExtractionDimensions() {
        val extractor = MarketDataFeatureExtractor()
        val dummyCandles = (1..40).map { i ->
            val price = 2400.0 + i * 2.0
            CandleStick(
                timestamp = System.currentTimeMillis() - (40 - i) * 60000L,
                open = price - 1.0,
                high = price + 3.0,
                low = price - 2.0,
                close = price,
                volume = 250000L + (i * 1000L)
            )
        }

        val tensor = extractor.extractFeatures("RELIANCE", 2480.0, dummyCandles)
        assertNotNull(tensor.byteBuffer)
        assertEquals(30, tensor.floatArray.size)
        assertEquals(8, tensor.floatArray[0].size)
        assertTrue(tensor.minPrice > 0.0)
        assertTrue(tensor.maxPrice >= tensor.minPrice)
    }

    @Test
    fun testTFLiteTimeSeriesForecasterExecution() {
        val forecaster = TFLiteTimeSeriesForecaster(null)
        val dummyCandles = (1..35).map { i ->
            val price = 1450.0 + (i % 5) * 4.0
            CandleStick(
                timestamp = System.currentTimeMillis() - (35 - i) * 86400000L,
                open = price - 2.0,
                high = price + 5.0,
                low = price - 3.0,
                close = price,
                volume = 1500000L
            )
        }

        val prediction = forecaster.forecast("INFY", 1470.0, dummyCandles, "3 Days")
        assertNotNull(prediction)
        assertEquals("INFY", prediction.symbol)
        assertTrue(prediction.confidence in 50..100)
        assertTrue(prediction.multiStepForecast.size == 5)
        assertTrue(prediction.multiStepForecast[0].predictedPrice > 0.0)
        assertTrue(prediction.inputTensorShape == TFLiteModelConfig.INPUT_SHAPE)
        forecaster.close()
    }

    @Test
    fun testHistoricalMarketDataStructuresAndTensorDataClasses() {
        val historicalPoints = (1..35).map { i ->
            val p = 100.0 + i
            com.example.domain.ml.tflite.HistoricalMarketDataPoint(
                timestamp = System.currentTimeMillis() - (35 - i) * 60000L,
                open = p,
                high = p + 2.0,
                low = p - 1.0,
                close = p + 1.0,
                volume = 10000L + i * 500L,
                vwap = p + 0.5,
                openInterest = 50000L
            )
        }

        val series = com.example.domain.ml.tflite.HistoricalMarketDataSeries(
            symbol = "TATAMOTORS",
            candles = historicalPoints
        )

        assertEquals(35, series.size)
        assertTrue(series.maxPrice > series.minPrice)

        val extractor = MarketDataFeatureExtractor()
        val inputTensor = extractor.extractFromSeries(series, 136.0)

        assertTrue(inputTensor.validate())
        assertEquals(1, inputTensor.batchSize)
        assertEquals(30, inputTensor.sequenceLength)
        assertEquals(8, inputTensor.featureCount)
        assertEquals("[1, 30, 8]", inputTensor.tensorShape)
        assertEquals(30 * 8 * 4, inputTensor.bufferSizeBytes)

        val interpreter = com.example.domain.ml.tflite.TFLiteLSTMInterpreter(null)
        val outputTensor = interpreter.runTensorInference(inputTensor, 136.0)

        assertEquals(1, outputTensor.batchSize)
        assertEquals(5, outputTensor.forecastSteps)
        assertEquals("[1, 5]", outputTensor.tensorShape)
        assertEquals(5, outputTensor.horizons.size)
        assertTrue(outputTensor.horizons[0].denormalizedPrice > 0.0)
        assertTrue(outputTensor.horizons[0].upperRiskBound >= outputTensor.horizons[0].lowerRiskBound)

        interpreter.close()
    }
}
