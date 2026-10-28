package com.example

import com.example.data.model.CandleStick
import com.example.domain.indicators.TechnicalIndicatorCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TechnicalIndicatorCalculatorTest {

    private fun generateCandleSeries(count: Int = 50, startPrice: Double = 1000.0): List<CandleStick> {
        return (1..count).map { i ->
            val price = startPrice + (i * 5.0) + (if (i % 2 == 0) 3.0 else -2.0)
            CandleStick(
                timestamp = 1700000000000L + (i * 60000L),
                open = price - 2.0,
                high = price + 6.0,
                low = price - 4.0,
                close = price,
                volume = 100000L + (i * 500L)
            )
        }
    }

    @Test
    fun testMovingAverages() {
        val candles = generateCandleSeries(40)
        val closes = candles.map { it.close }

        val sma20 = TechnicalIndicatorCalculator.calculateSMA(closes, 20)
        val ema21 = TechnicalIndicatorCalculator.calculateEMA(closes, 21)
        val wma10 = TechnicalIndicatorCalculator.calculateWMA(closes, 10)
        val vwap = TechnicalIndicatorCalculator.calculateVWAP(candles)

        assertTrue(sma20 > 0.0)
        assertTrue(ema21 > 0.0)
        assertTrue(wma10 > 0.0)
        assertTrue(vwap > 0.0)

        val smaSeries = TechnicalIndicatorCalculator.calculateSMASeries(closes, 20)
        val emaSeries = TechnicalIndicatorCalculator.calculateEMASeries(closes, 21)
        assertEquals(closes.size, smaSeries.size)
        assertEquals(closes.size, emaSeries.size)
    }

    @Test
    fun testRSI() {
        val candles = generateCandleSeries(40)
        val closes = candles.map { it.close }

        val rsi = TechnicalIndicatorCalculator.calculateRSI(closes, 14)
        assertTrue("RSI should be bounded between 0 and 100", rsi in 0.0..100.0)

        val rsiSeries = TechnicalIndicatorCalculator.calculateRSISeries(closes, 14)
        assertEquals(closes.size, rsiSeries.size)
        assertTrue(rsiSeries.last() in 0.0..100.0)
    }

    @Test
    fun testMACD() {
        val candles = generateCandleSeries(50)
        val closes = candles.map { it.close }

        val macd = TechnicalIndicatorCalculator.calculateMACD(closes, 12, 26, 9)
        assertNotNull(macd)
        assertEquals(macd.macdLine - macd.signalLine, macd.histogram, 0.0001)

        val macdSeries = TechnicalIndicatorCalculator.calculateMACDSeries(closes, 12, 26, 9)
        assertEquals(closes.size, macdSeries.macdLines.size)
        assertEquals(closes.size, macdSeries.signalLines.size)
        assertEquals(closes.size, macdSeries.histograms.size)
    }

    @Test
    fun testBollingerBands() {
        val candles = generateCandleSeries(30)
        val closes = candles.map { it.close }

        val bb = TechnicalIndicatorCalculator.calculateBollingerBands(closes, 20, 2.0)
        assertTrue(bb.upperBand >= bb.middleBand)
        assertTrue(bb.middleBand >= bb.lowerBand)
        assertTrue(bb.bandwidth >= 0.0)
    }

    @Test
    fun testSuperTrendAndATR() {
        val candles = generateCandleSeries(30)
        val atr = TechnicalIndicatorCalculator.calculateATR(candles, 14)
        val st = TechnicalIndicatorCalculator.calculateSuperTrend(candles, 10, 3.0)

        assertTrue(atr > 0.0)
        assertTrue(st.superTrend > 0.0)
        assertTrue(st.upperBand >= st.lowerBand)
    }

    @Test
    fun testComprehensiveCalculator() {
        val candles = generateCandleSeries(50)
        val result = TechnicalIndicatorCalculator.calculateIndicators("TCS", candles)

        assertEquals("TCS", result.symbol)
        assertTrue(result.rsi in 0.0..100.0)
        assertTrue(result.sma20 > 0.0)
        assertTrue(result.sma50 > 0.0)
        assertTrue(result.ema9 > 0.0)
        assertTrue(result.ema21 > 0.0)
        assertTrue(result.vwap > 0.0)
        assertTrue(result.upperBollinger >= result.lowerBollinger)
        assertNotNull(result.overallSentiment)
    }
}
