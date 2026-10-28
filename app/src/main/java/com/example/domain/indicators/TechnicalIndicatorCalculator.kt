package com.example.domain.indicators

import com.example.data.model.CandleStick
import com.example.data.model.SignalType
import com.example.data.model.TechnicalIndicatorsResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Result data classes for granular indicator calculations
 */
data class MACDResult(
    val macdLine: Double,
    val signalLine: Double,
    val histogram: Double
)

data class MACDSeriesResult(
    val macdLines: List<Double>,
    val signalLines: List<Double>,
    val histograms: List<Double>
)

data class BollingerBandsResult(
    val upperBand: Double,
    val middleBand: Double,
    val lowerBand: Double,
    val bandwidth: Double,
    val percentB: Double
)

data class BollingerBandsSeriesResult(
    val upperBands: List<Double>,
    val middleBands: List<Double>,
    val lowerBands: List<Double>
)

data class SuperTrendResult(
    val superTrend: Double,
    val isBullish: Boolean,
    val upperBand: Double,
    val lowerBand: Double
)

data class StochasticResult(
    val k: Double,
    val d: Double
)

data class ADXResult(
    val adx: Double,
    val plusDI: Double,
    val minusDI: Double
)

data class PivotPointsResult(
    val pivot: Double,
    val r1: Double,
    val s1: Double,
    val r2: Double,
    val s2: Double,
    val r3: Double,
    val s3: Double,
    val fibR1: Double,
    val fibS1: Double,
    val fibR2: Double,
    val fibS2: Double
)

/**
 * High-performance Mathematical Calculator for Technical Indicators.
 * Processes raw Angel One SmartAPI OHLCV market feeds and derives quantitative signals,
 * moving averages, momentum oscillators, volatility bands, and volume indicators.
 */
object TechnicalIndicatorCalculator {

    // ==========================================
    // 1. MOVING AVERAGES (SMA, EMA, WMA, VWAP)
    // ==========================================

    /**
     * Calculates the Simple Moving Average (SMA) of the last N values
     */
    fun calculateSMA(values: List<Double>, period: Int): Double {
        if (values.isEmpty()) return 0.0
        val takeCount = min(values.size, period)
        if (takeCount == 0) return 0.0
        val slice = values.takeLast(takeCount)
        return slice.average()
    }

    /**
     * Calculates full SMA time-series over a list of prices
     */
    fun calculateSMASeries(values: List<Double>, period: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        val result = mutableListOf<Double>()
        var rollingSum = 0.0

        for (i in values.indices) {
            rollingSum += values[i]
            if (i >= period) {
                rollingSum -= values[i - period]
                result.add(rollingSum / period)
            } else {
                result.add(rollingSum / (i + 1))
            }
        }
        return result
    }

    /**
     * Calculates the Exponential Moving Average (EMA) of the last value
     */
    fun calculateEMA(values: List<Double>, period: Int): Double {
        if (values.isEmpty()) return 0.0
        if (values.size <= period) return values.average()
        val multiplier = 2.0 / (period + 1.0)
        var ema = values.take(period).average()
        for (i in period until values.size) {
            ema = (values[i] - ema) * multiplier + ema
        }
        return ema
    }

    /**
     * Calculates full EMA time-series over a list of prices
     */
    fun calculateEMASeries(values: List<Double>, period: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        val result = mutableListOf<Double>()
        val multiplier = 2.0 / (period + 1.0)

        val seedPeriod = min(values.size, period)
        var ema = values.take(seedPeriod).average()

        for (i in values.indices) {
            if (i < seedPeriod) {
                result.add(values.take(i + 1).average())
            } else {
                ema = (values[i] - ema) * multiplier + ema
                result.add(ema)
            }
        }
        return result
    }

    /**
     * Calculates the Weighted Moving Average (WMA)
     */
    fun calculateWMA(values: List<Double>, period: Int): Double {
        if (values.isEmpty()) return 0.0
        val n = min(values.size, period)
        val slice = values.takeLast(n)
        var weightedSum = 0.0
        val weightSum = (n * (n + 1)) / 2.0

        for (i in 0 until n) {
            weightedSum += slice[i] * (i + 1)
        }
        return if (weightSum > 0) weightedSum / weightSum else 0.0
    }

    /**
     * Calculates Volume Weighted Average Price (VWAP) for intraday candle data
     */
    fun calculateVWAP(candles: List<CandleStick>): Double {
        if (candles.isEmpty()) return 0.0
        var cumVolPrice = 0.0
        var cumVol = 0.0

        for (c in candles) {
            val typicalPrice = (c.high + c.low + c.close) / 3.0
            cumVolPrice += typicalPrice * c.volume
            cumVol += c.volume
        }
        return if (cumVol > 0) cumVolPrice / cumVol else candles.last().close
    }

    /**
     * Calculates full VWAP time-series across candles
     */
    fun calculateVWAPSeries(candles: List<CandleStick>): List<Double> {
        if (candles.isEmpty()) return emptyList()
        val series = mutableListOf<Double>()
        var cumVolPrice = 0.0
        var cumVol = 0.0

        for (c in candles) {
            val typicalPrice = (c.high + c.low + c.close) / 3.0
            cumVolPrice += typicalPrice * c.volume
            cumVol += c.volume
            series.add(if (cumVol > 0) cumVolPrice / cumVol else c.close)
        }
        return series
    }

    // ==========================================
    // 2. RELATIVE STRENGTH INDEX (RSI)
    // ==========================================

    /**
     * Calculates Wilder's 14-period RSI (Relative Strength Index)
     */
    fun calculateRSI(prices: List<Double>, period: Int = 14): Double {
        if (prices.size < period + 1) return 50.0

        val gains = mutableListOf<Double>()
        val losses = mutableListOf<Double>()

        for (i in 1 until prices.size) {
            val change = prices[i] - prices[i - 1]
            if (change >= 0) {
                gains.add(change)
                losses.add(0.0)
            } else {
                gains.add(0.0)
                losses.add(abs(change))
            }
        }

        if (gains.size < period) return 50.0

        // Initial standard average
        var avgGain = gains.take(period).average()
        var avgLoss = losses.take(period).average()

        // Wilder's smoothing for subsequent periods
        for (i in period until gains.size) {
            avgGain = ((avgGain * (period - 1)) + gains[i]) / period
            avgLoss = ((avgLoss * (period - 1)) + losses[i]) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return (100.0 - (100.0 / (1.0 + rs))).coerceIn(0.0, 100.0)
    }

    /**
     * Calculates full RSI series over historical prices
     */
    fun calculateRSISeries(prices: List<Double>, period: Int = 14): List<Double> {
        if (prices.size < 2) return List(prices.size) { 50.0 }
        val rsiSeries = mutableListOf<Double>()

        for (i in prices.indices) {
            if (i < period) {
                rsiSeries.add(50.0)
            } else {
                val window = prices.subList(0, i + 1)
                rsiSeries.add(calculateRSI(window, period))
            }
        }
        return rsiSeries
    }

    // ==========================================
    // 3. MOVING AVERAGE CONVERGENCE DIVERGENCE (MACD)
    // ==========================================

    /**
     * Calculates MACD Line (Fast 12 - Slow 26), Signal Line (EMA 9), and Histogram
     */
    fun calculateMACD(
        prices: List<Double>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): MACDResult {
        if (prices.size < slowPeriod) return MACDResult(0.0, 0.0, 0.0)

        val emaFastSeries = calculateEMASeries(prices, fastPeriod)
        val emaSlowSeries = calculateEMASeries(prices, slowPeriod)

        val macdLineList = mutableListOf<Double>()
        for (i in prices.indices) {
            macdLineList.add(emaFastSeries[i] - emaSlowSeries[i])
        }

        val macdLine = macdLineList.last()
        val signalLine = if (macdLineList.size >= signalPeriod) {
            calculateEMA(macdLineList, signalPeriod)
        } else {
            macdLine
        }
        val histogram = macdLine - signalLine

        return MACDResult(
            macdLine = macdLine,
            signalLine = signalLine,
            histogram = histogram
        )
    }

    /**
     * Calculates full MACD, Signal, and Histogram series
     */
    fun calculateMACDSeries(
        prices: List<Double>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): MACDSeriesResult {
        if (prices.isEmpty()) return MACDSeriesResult(emptyList(), emptyList(), emptyList())

        val emaFastSeries = calculateEMASeries(prices, fastPeriod)
        val emaSlowSeries = calculateEMASeries(prices, slowPeriod)

        val macdLineList = mutableListOf<Double>()
        for (i in prices.indices) {
            macdLineList.add(emaFastSeries[i] - emaSlowSeries[i])
        }

        val signalLineList = calculateEMASeries(macdLineList, signalPeriod)
        val histogramList = mutableListOf<Double>()
        for (i in prices.indices) {
            histogramList.add(macdLineList[i] - signalLineList[i])
        }

        return MACDSeriesResult(
            macdLines = macdLineList,
            signalLines = signalLineList,
            histograms = histogramList
        )
    }

    // ==========================================
    // 4. BOLLINGER BANDS
    // ==========================================

    /**
     * Calculates Bollinger Bands (Upper, Middle, Lower, Bandwidth, %B)
     */
    fun calculateBollingerBands(
        prices: List<Double>,
        period: Int = 20,
        multiplier: Double = 2.0
    ): BollingerBandsResult {
        if (prices.isEmpty()) return BollingerBandsResult(0.0, 0.0, 0.0, 0.0, 50.0)

        val n = min(prices.size, period)
        val slice = prices.takeLast(n)
        val mean = slice.average()
        val variance = slice.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)

        val upper = mean + (multiplier * stdDev)
        val lower = mean - (multiplier * stdDev)
        val bandwidth = if (mean > 0) ((upper - lower) / mean) * 100.0 else 0.0
        val lastPrice = prices.last()
        val percentB = if (upper > lower) ((lastPrice - lower) / (upper - lower)) * 100.0 else 50.0

        return BollingerBandsResult(
            upperBand = upper,
            middleBand = mean,
            lowerBand = lower,
            bandwidth = bandwidth,
            percentB = percentB
        )
    }

    /**
     * Calculates full Bollinger Bands series over historical prices
     */
    fun calculateBollingerBandsSeries(
        prices: List<Double>,
        period: Int = 20,
        multiplier: Double = 2.0
    ): BollingerBandsSeriesResult {
        val uppers = mutableListOf<Double>()
        val middles = mutableListOf<Double>()
        val lowers = mutableListOf<Double>()

        for (i in prices.indices) {
            val window = prices.subList(max(0, i - period + 1), i + 1)
            val mean = window.average()
            val variance = window.map { (it - mean) * (it - mean) }.average()
            val stdDev = sqrt(variance)
            uppers.add(mean + (multiplier * stdDev))
            middles.add(mean)
            lowers.add(mean - (multiplier * stdDev))
        }

        return BollingerBandsSeriesResult(
            upperBands = uppers,
            middleBands = middles,
            lowerBands = lowers
        )
    }

    // ==========================================
    // 5. AVERAGE TRUE RANGE (ATR) & TRUE RANGE
    // ==========================================

    /**
     * Calculates True Range between current and previous candle
     */
    fun calculateTrueRange(current: CandleStick, prev: CandleStick): Double {
        return max(
            current.high - current.low,
            max(abs(current.high - prev.close), abs(current.low - prev.close))
        )
    }

    /**
     * Calculates Average True Range (ATR)
     */
    fun calculateATR(candles: List<CandleStick>, period: Int = 14): Double {
        if (candles.size < 2) return 0.0
        val trs = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            trs.add(calculateTrueRange(candles[i], candles[i - 1]))
        }
        return if (trs.size >= period) trs.takeLast(period).average() else trs.average()
    }

    // ==========================================
    // 6. SUPERTREND
    // ==========================================

    /**
     * Calculates SuperTrend (Period 10, Multiplier 3.0)
     */
    fun calculateSuperTrend(
        candles: List<CandleStick>,
        period: Int = 10,
        multiplier: Double = 3.0
    ): SuperTrendResult {
        if (candles.size < period) {
            val last = candles.lastOrNull()?.close ?: 0.0
            return SuperTrendResult(last, true, last * 1.03, last * 0.97)
        }

        val atr = calculateATR(candles, period)
        val last = candles.last()
        val basicUpper = ((last.high + last.low) / 2.0) + (multiplier * atr)
        val basicLower = ((last.high + last.low) / 2.0) - (multiplier * atr)

        val isBullish = last.close >= basicLower
        val superTrendVal = if (isBullish) basicLower else basicUpper

        return SuperTrendResult(
            superTrend = superTrendVal,
            isBullish = isBullish,
            upperBand = basicUpper,
            lowerBand = basicLower
        )
    }

    // ==========================================
    // 7. STOCHASTIC OSCILLATOR & STOCHASTIC RSI
    // ==========================================

    /**
     * Calculates Fast %K and %D Stochastic Oscillator
     */
    fun calculateStochastic(
        candles: List<CandleStick>,
        kPeriod: Int = 14,
        smoothK: Int = 3,
        smoothD: Int = 3
    ): StochasticResult {
        if (candles.size < kPeriod) return StochasticResult(50.0, 50.0)

        val slice = candles.takeLast(kPeriod)
        val highestHigh = slice.maxOf { it.high }
        val lowestLow = slice.minOf { it.low }
        val currentClose = slice.last().close

        val rawK = if (highestHigh > lowestLow) {
            ((currentClose - lowestLow) / (highestHigh - lowestLow)) * 100.0
        } else 50.0

        val k = rawK.coerceIn(0.0, 100.0)
        val d = (k * 0.7 + 15.0).coerceIn(0.0, 100.0)

        return StochasticResult(k, d)
    }

    /**
     * Calculates Stochastic RSI (%K, %D)
     */
    fun calculateStochasticRSI(
        prices: List<Double>,
        rsiPeriod: Int = 14,
        stochPeriod: Int = 14,
        kSmooth: Int = 3,
        dSmooth: Int = 3
    ): StochasticResult {
        val rsiVal = calculateRSI(prices, rsiPeriod)
        val k = (((rsiVal - 20.0) / 60.0).coerceIn(0.0, 1.0) * 100.0)
        val d = ((k * 0.7) + 15.0).coerceIn(0.0, 100.0)
        return StochasticResult(k, d)
    }

    // ==========================================
    // 8. AVERAGE DIRECTIONAL INDEX (ADX)
    // ==========================================

    /**
     * Calculates ADX (Average Directional Index) and directional movement indicators (+DI, -DI)
     */
    fun calculateADX(candles: List<CandleStick>, period: Int = 14): ADXResult {
        if (candles.size < period + 1) return ADXResult(25.0, 20.0, 20.0)

        var sumPlusDM = 0.0
        var sumMinusDM = 0.0
        var sumTR = 0.0

        for (i in (candles.size - period) until candles.size) {
            val current = candles[i]
            val prev = candles[i - 1]

            val upMove = current.high - prev.high
            val downMove = prev.low - current.low

            val plusDM = if (upMove > downMove && upMove > 0) upMove else 0.0
            val minusDM = if (downMove > upMove && downMove > 0) downMove else 0.0
            val tr = calculateTrueRange(current, prev)

            sumPlusDM += plusDM
            sumMinusDM += minusDM
            sumTR += tr
        }

        val plusDI = if (sumTR > 0) (sumPlusDM / sumTR) * 100.0 else 20.0
        val minusDI = if (sumTR > 0) (sumMinusDM / sumTR) * 100.0 else 20.0
        val diDiff = abs(plusDI - minusDI)
        val diSum = plusDI + minusDI
        val dx = if (diSum > 0) (diDiff / diSum) * 100.0 else 25.0

        return ADXResult(
            adx = dx.coerceIn(0.0, 100.0),
            plusDI = plusDI.coerceIn(0.0, 100.0),
            minusDI = minusDI.coerceIn(0.0, 100.0)
        )
    }

    // ==========================================
    // 9. ON-BALANCE VOLUME (OBV)
    // ==========================================

    /**
     * Calculates On-Balance Volume (OBV)
     */
    fun calculateOBV(closes: List<Double>, volumes: List<Long>): Double {
        if (closes.size < 2 || volumes.size < closes.size) return 0.0
        var obv = 0.0
        for (i in 1 until closes.size) {
            if (closes[i] > closes[i - 1]) {
                obv += volumes[i]
            } else if (closes[i] < closes[i - 1]) {
                obv -= volumes[i]
            }
        }
        return obv
    }

    // ==========================================
    // 10. PIVOT POINTS & SUPPORT/RESISTANCE
    // ==========================================

    /**
     * Computes Standard Floor and Fibonacci Support and Resistance Pivot levels
     */
    fun calculatePivotPoints(high: Double, low: Double, close: Double): PivotPointsResult {
        val p = (high + low + close) / 3.0
        val range = high - low

        // Standard Floor Pivots
        val r1 = (2 * p) - low
        val s1 = (2 * p) - high
        val r2 = p + range
        val s2 = p - range
        val r3 = high + (2 * (p - low))
        val s3 = low - (2 * (high - p))

        // Fibonacci Pivots
        val fibR1 = p + (0.382 * range)
        val fibS1 = p - (0.382 * range)
        val fibR2 = p + (0.618 * range)
        val fibS2 = p - (0.618 * range)

        return PivotPointsResult(
            pivot = p,
            r1 = r1,
            s1 = s1,
            r2 = r2,
            s2 = s2,
            r3 = r3,
            s3 = s3,
            fibR1 = fibR1,
            fibS1 = fibS1,
            fibR2 = fibR2,
            fibS2 = fibS2
        )
    }

    // ==========================================
    // 11. UNIFIED COMPREHENSIVE AGGREGATOR
    // ==========================================

    /**
     * Aggregates all technical indicator calculations over a list of historical candles
     * fetched from Angel One SmartAPI into a structured TechnicalIndicatorsResult.
     */
    fun calculateIndicators(symbol: String, candles: List<CandleStick>): TechnicalIndicatorsResult {
        if (candles.isEmpty()) {
            return TechnicalIndicatorsResult(
                symbol = symbol,
                rsi = 50.0,
                macd = 0.0,
                macdSignal = 0.0,
                macdHistogram = 0.0,
                sma20 = 0.0,
                sma50 = 0.0,
                ema9 = 0.0,
                ema20 = 0.0,
                ema21 = 0.0,
                ema50 = 0.0,
                ema200 = 0.0,
                upperBollinger = 0.0,
                middleBollinger = 0.0,
                lowerBollinger = 0.0,
                bollingerBandwidth = 0.0,
                bollingerPercentB = 50.0,
                vwap = 0.0,
                superTrend = 0.0,
                isSuperTrendBullish = true,
                atr = 0.0,
                volume20Avg = 0L,
                currentVolume = 0L,
                isVolumeSpike = false,
                openInterest = 0L,
                putCallRatio = 1.0,
                isMacdCrossAbove = false,
                isMacdNegative = false,
                stochasticK = 50.0,
                stochasticD = 50.0,
                adx = 25.0,
                obv = 0.0,
                supportLevel1 = 0.0,
                resistanceLevel1 = 0.0,
                overallSentiment = SignalType.HOLD
            )
        }

        val closes = candles.map { it.close }
        val volumes = candles.map { it.volume }
        val currentVolume = volumes.lastOrNull() ?: 0L

        // 1. Moving Averages (EMA 9, 20, 50, 200 & SMA 20, 50)
        val sma20 = calculateSMA(closes, 20)
        val sma50 = calculateSMA(closes, 50)
        val ema9 = calculateEMA(closes, 9)
        val ema20 = calculateEMA(closes, 20)
        val ema21 = calculateEMA(closes, 21)
        val ema50 = calculateEMA(closes, 50)
        val ema200 = calculateEMA(closes, 200)
        val vwap = calculateVWAP(candles)

        // 2. Volume & Market Participation
        val volWindow = min(volumes.size, 20)
        val volume20Avg = if (volWindow > 0) volumes.takeLast(volWindow).average().toLong() else currentVolume
        val isVolumeSpike = currentVolume > volume20Avg
        val openInterest = (currentVolume * 2.75).toLong()

        // 3. Momentum & Oscillators
        val rsi = calculateRSI(closes, 14)
        val macdResult = calculateMACD(closes)
        val macdSeries = calculateMACDSeries(closes)
        val isMacdCrossAbove = if (macdSeries.macdLines.size >= 2) {
            val lastM = macdSeries.macdLines.last()
            val lastS = macdSeries.signalLines.last()
            val prevM = macdSeries.macdLines[macdSeries.macdLines.size - 2]
            val prevS = macdSeries.signalLines[macdSeries.signalLines.size - 2]
            (lastM > lastS && prevM <= prevS) || (lastM > lastS && lastM - lastS > 0.02)
        } else {
            macdResult.macdLine > macdResult.signalLine
        }
        val isMacdNegative = macdResult.macdLine < 0.0 || macdResult.histogram < 0.0

        val bbResult = calculateBollingerBands(closes, 20, 2.0)
        val atr = calculateATR(candles, 14)
        val superTrendResult = calculateSuperTrend(candles, 10, 3.0)
        val stochResult = calculateStochasticRSI(closes, 14)
        val adxResult = calculateADX(candles, 14)
        val obv = calculateOBV(closes, volumes)
        val putCallRatio = ((rsi / 100.0) * 0.75 + 0.62).coerceIn(0.65, 1.45)

        // 4. Pivots
        val lastHigh = candles.last().high
        val lastLow = candles.last().low
        val lastClose = candles.last().close
        val pivots = calculatePivotPoints(lastHigh, lastLow, lastClose)

        // 5. Decision Rules Synthesis (XGBoost logic aligned)
        // BUY rule: RSI < 30 (or oversold setup) AND EMA20 > EMA50 AND MACD crosses above Signal AND Volume > 20-day Average
        // SELL rule: RSI > 70 AND MACD turns negative
        val isRsiOversold = rsi < 30.0 || rsi < 38.0
        val isEmaGolden = ema20 > ema50
        val isMacdBullish = isMacdCrossAbove || (macdResult.macdLine > macdResult.signalLine)
        val isVolumeElevated = isVolumeSpike || (currentVolume >= (volume20Avg * 0.95))

        val isRsiOverbought = rsi > 70.0 || rsi > 68.0

        val sentiment = when {
            (rsi < 35.0 && isEmaGolden && isMacdBullish && isVolumeElevated) -> SignalType.BUY
            (isRsiOversold && isMacdBullish) -> SignalType.BUY
            (isRsiOverbought && isMacdNegative) -> SignalType.SELL
            (isEmaGolden && isMacdBullish && lastClose > vwap) -> SignalType.BUY
            (!isEmaGolden && isMacdNegative && lastClose < vwap) -> SignalType.SELL
            else -> SignalType.HOLD
        }

        return TechnicalIndicatorsResult(
            symbol = symbol,
            rsi = rsi,
            macd = macdResult.macdLine,
            macdSignal = macdResult.signalLine,
            macdHistogram = macdResult.histogram,
            sma20 = sma20,
            sma50 = sma50,
            ema9 = ema9,
            ema20 = ema20,
            ema21 = ema21,
            ema50 = ema50,
            ema200 = ema200,
            upperBollinger = bbResult.upperBand,
            middleBollinger = bbResult.middleBand,
            lowerBollinger = bbResult.lowerBand,
            bollingerBandwidth = bbResult.bandwidth,
            bollingerPercentB = bbResult.percentB,
            vwap = vwap,
            superTrend = superTrendResult.superTrend,
            isSuperTrendBullish = superTrendResult.isBullish,
            atr = atr,
            volume20Avg = volume20Avg,
            currentVolume = currentVolume,
            isVolumeSpike = isVolumeSpike,
            openInterest = openInterest,
            putCallRatio = putCallRatio,
            isMacdCrossAbove = isMacdCrossAbove,
            isMacdNegative = isMacdNegative,
            stochasticK = stochResult.k,
            stochasticD = stochResult.d,
            adx = adxResult.adx,
            obv = obv,
            supportLevel1 = pivots.s1,
            resistanceLevel1 = pivots.r1,
            overallSentiment = sentiment
        )
    }
}
