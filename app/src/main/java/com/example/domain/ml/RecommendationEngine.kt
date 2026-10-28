package com.example.domain.ml

import com.example.data.model.AIRecommendation
import com.example.data.model.CandleStick
import com.example.data.model.RiskLevel
import com.example.data.model.SignalType
import com.example.data.model.TrendDirection
import com.example.domain.indicators.TechnicalIndicatorCalculator
import java.util.Locale
import kotlin.math.abs

object RecommendationEngine {

    fun generateRecommendation(
        symbol: String,
        currentPrice: Double,
        candles: List<CandleStick>
    ): AIRecommendation {
        // 1. Compute Comprehensive Technical Indicator Engine (EMA 9/20/50/200, RSI, MACD, Bollinger, VWAP, ATR, Supertrend)
        val indicators = TechnicalIndicatorCalculator.calculateIndicators(symbol, candles)

        // 2. Compute TensorFlow Lite LSTM Price Forecaster
        val lstmPrediction = LSTMPredictionEngine.predictStockPrice(symbol, currentPrice, candles, "3 Days")

        // 3. Evaluate XGBoost Decision Classifier with the Angel One Algo Rules
        val xgboostResult = XGBoostDecisionEngine.evaluate(symbol, currentPrice, candles, indicators)

        // 4. Evaluate Random Forest Ensemble for Confidence Scoring & Feature Importance Ranking
        val rfResult = RandomForestConfidenceEngine.computeConfidence(
            signal = xgboostResult.signal,
            indicators = indicators,
            features = xgboostResult.engineeredFeatures
        )

        // 5. Calculate Dynamic Stop Loss & Target Price using Average True Range (ATR)
        val atr = if (indicators.atr > 0.0) indicators.atr else (currentPrice * 0.015)
        val stopLossMultiplier = 1.5
        val targetMultiplier = 2.5

        val stopLoss = when (xgboostResult.signal) {
            SignalType.BUY -> (currentPrice - (stopLossMultiplier * atr)).coerceAtLeast(currentPrice * 0.85)
            SignalType.SELL -> (currentPrice + (stopLossMultiplier * atr))
            SignalType.HOLD -> (currentPrice - (1.0 * atr))
        }

        val targetPrice = when (xgboostResult.signal) {
            SignalType.BUY -> {
                val lstmTarget = lstmPrediction.predictedPrice
                val atrTarget = currentPrice + (targetMultiplier * atr)
                if (lstmTarget > currentPrice) (lstmTarget * 0.4 + atrTarget * 0.6) else atrTarget
            }
            SignalType.SELL -> (currentPrice - (targetMultiplier * atr)).coerceAtLeast(currentPrice * 0.5)
            SignalType.HOLD -> currentPrice
        }

        val riskPercent = (abs(currentPrice - stopLoss) / currentPrice) * 100.0
        val isRiskAlert = riskPercent > 3.8 || (atr / currentPrice > 0.028)
        val riskAlertMessage = if (isRiskAlert) {
            "Elevated ATR Volatility (₹${String.format(Locale.US, "%.2f", atr)}) — Keep strict ${String.format(Locale.US, "%.1f", riskPercent)}% Stop-Loss buffer"
        } else {
            "Normalized volatility regime within acceptable algorithmic risk parameters"
        }

        val riskLevel = when {
            riskPercent > 4.0 || atr / currentPrice > 0.03 -> RiskLevel.HIGH
            riskPercent > 2.0 || atr / currentPrice > 0.015 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        // 6. Build Detailed Algorithmic Rationales
        val reasons = mutableListOf<String>()

        // Rationale 1: XGBoost Classification Outcome
        reasons.add("XGBoost Classifier: ${xgboostResult.summaryRationale}")

        // Rationale 2: RSI Evaluation
        if (indicators.rsi < 30.0) {
            reasons.add("RSI (14) at ${String.format(Locale.US, "%.1f", indicators.rsi)} indicates acute oversold condition primed for mean-reversion")
        } else if (indicators.rsi > 70.0) {
            reasons.add("RSI (14) at ${String.format(Locale.US, "%.1f", indicators.rsi)} flags overbought risk zone with momentum deceleration")
        } else {
            reasons.add("RSI (14) at ${String.format(Locale.US, "%.1f", indicators.rsi)} reflects balanced trend momentum")
        }

        // Rationale 3: EMA 20 vs EMA 50 Trend Alignment
        if (indicators.ema20 > indicators.ema50) {
            reasons.add("EMA Trend: 20-period EMA (₹${String.format(Locale.US, "%.1f", indicators.ema20)}) trading above 50-period EMA (₹${String.format(Locale.US, "%.1f", indicators.ema50)}) confirms golden trend")
        } else {
            reasons.add("EMA Trend: 20-period EMA (₹${String.format(Locale.US, "%.1f", indicators.ema20)}) below 50-period EMA (₹${String.format(Locale.US, "%.1f", indicators.ema50)}) indicates overhead resistance")
        }

        // Rationale 4: MACD Signal & Histogram
        if (indicators.isMacdCrossAbove || indicators.macd > indicators.macdSignal) {
            reasons.add("MACD (12, 26, 9) crossed above Signal line (Histogram: ${if (indicators.macdHistogram >= 0) "+" else ""}${String.format(Locale.US, "%.2f", indicators.macdHistogram)}) indicating bullish acceleration")
        } else {
            reasons.add("MACD momentum negative (Histogram: ${String.format(Locale.US, "%.2f", indicators.macdHistogram)}) signalling bearish pressure")
        }

        // Rationale 5: Volume vs 20-day Average
        if (indicators.isVolumeSpike) {
            reasons.add("Volume Surge: Current volume exceeds 20-day volume average confirming institutional participation")
        } else {
            reasons.add("Volume Participation: Volume tracking near 20-day baseline")
        }

        // Rationale 6: LSTM Deep Learning Forecast
        reasons.add("LSTM Deep Learning: Forecasts ${if (lstmPrediction.expectedReturnPercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", lstmPrediction.expectedReturnPercent)}% target price of ₹${String.format(Locale.US, "%,.2f", lstmPrediction.predictedPrice)}")

        // Rationale 7: ATR Stop Loss & Target Price
        reasons.add("ATR Risk Guard: Stop-Loss calibrated at 1.5× ATR (₹${String.format(Locale.US, "%,.2f", stopLoss)}) with Target at 2.5× ATR (₹${String.format(Locale.US, "%,.2f", targetPrice)})")

        val consensusSummary = when (xgboostResult.signal) {
            SignalType.BUY -> "XGBoost & Random Forest consensus confirms BUY setup with ${rfResult.confidenceScore}% confidence"
            SignalType.SELL -> "XGBoost & Random Forest consensus triggers SELL / Profit Taking with ${rfResult.confidenceScore}% confidence"
            SignalType.HOLD -> "Algorithmic consensus suggests HOLD awaiting technical breakout confirmation"
        }

        return AIRecommendation(
            symbol = symbol,
            signal = xgboostResult.signal,
            confidence = rfResult.confidenceScore,
            riskLevel = riskLevel,
            currentPrice = currentPrice,
            targetPrice = targetPrice,
            stopLoss = stopLoss,
            primaryReasons = reasons,
            indicatorConsensus = consensusSummary,
            riskPercent = riskPercent,
            isRiskAlert = isRiskAlert,
            riskAlertMessage = riskAlertMessage,
            stopLossAtrMultiplier = stopLossMultiplier,
            targetAtrMultiplier = targetMultiplier,
            atrValue = atr,
            decisionRuleChecks = xgboostResult.ruleChecks,
            featureRankings = rfResult.featureRankings,
            engineeredFeatures = xgboostResult.engineeredFeatures,
            modelPipeline = "Angel One SmartAPI Feed ➔ Technical Engine (EMA 9/20/50/200, RSI, MACD, VWAP, Bollinger, ATR, Supertrend) ➔ XGBoost Classifier + Random Forest ➔ ${xgboostResult.signal.name}"
        )
    }
}

