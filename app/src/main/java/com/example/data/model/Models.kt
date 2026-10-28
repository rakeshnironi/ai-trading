package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

enum class Exchange {
    NSE, BSE
}

enum class TradingMode {
    LIVE,
    PAPER;

    val isLive: Boolean get() = this == LIVE
    val isPaper: Boolean get() = this == PAPER
    val badgeLabel: String get() = if (this == LIVE) "LIVE" else "PAPER"
    val statusSubtitle: String get() = if (this == LIVE) "Connected to Market" else "Simulation Mode"
}

enum class SignalType {
    BUY, SELL, HOLD
}

enum class TrendDirection {
    UP, DOWN, SIDEWAYS
}

enum class RiskLevel {
    LOW, MODERATE, HIGH
}

enum class AlertCondition {
    ABOVE, BELOW
}

enum class NotificationStatus {
    SENT, FAILED, PENDING
}

enum class NotificationEventType {
    PAPER_BUY, PAPER_SELL, AI_SIGNAL, PRICE_ALERT, PORTFOLIO_SUMMARY, SECURITY_ALERT
}

enum class PriceTickDirection {
    UP, DOWN, UNCHANGED
}

data class MarketDepthItem(
    val buyQty: Long,
    val buyPrice: Double,
    val sellPrice: Double,
    val sellQty: Long
)

data class StockQuote(
    val symbol: String,
    val companyName: String,
    val exchange: Exchange,
    val token: String,
    val currentPrice: Double,
    val change: Double,
    val percentChange: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val previousClose: Double,
    val volume: Long,
    val high52Week: Double,
    val low52Week: Double,
    val sector: String = "Equities",
    val marketCap: String = "Large Cap",
    val isMarketOpen: Boolean = true,
    val lastTickDirection: PriceTickDirection = PriceTickDirection.UNCHANGED,
    val lastUpdatedTime: Long = System.currentTimeMillis(),
    val marketDepth: List<MarketDepthItem> = emptyList()
) {
    val isPositive: Boolean get() = change >= 0
    val formattedPrice: String get() = "₹" + String.format(Locale.US, "%,.2f", currentPrice)
    val formattedChange: String get() = (if (change >= 0) "+₹" else "-₹") + String.format(Locale.US, "%,.2f", Math.abs(change))
    val formattedPercent: String get() = (if (percentChange >= 0) "+" else "") + String.format(Locale.US, "%.2f%%", percentChange)
}

data class MarketIndex(
    val name: String,
    val value: Double,
    val change: Double,
    val percentChange: Double,
    val lastUpdated: String,
    val lastTickDirection: PriceTickDirection = PriceTickDirection.UNCHANGED
) {
    val isPositive: Boolean get() = change >= 0
    val formattedValue: String get() = String.format(Locale.US, "%,.2f", value)
    val formattedChange: String get() = (if (change >= 0) "+" else "") + String.format(Locale.US, "%,.2f (%.2f%%)", change, percentChange)
}

data class CandleStick(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
) {
    val isBullish: Boolean get() = close >= open
}

data class TechnicalIndicatorsResult(
    val symbol: String,
    val rsi: Double, // Relative Strength Index (14)
    val macd: Double,
    val macdSignal: Double,
    val macdHistogram: Double,
    val sma20: Double,
    val sma50: Double,
    val ema9: Double,
    val ema20: Double = 0.0,
    val ema21: Double = 0.0,
    val ema50: Double = 0.0,
    val ema200: Double = 0.0,
    val upperBollinger: Double,
    val middleBollinger: Double,
    val lowerBollinger: Double,
    val bollingerBandwidth: Double = 0.0,
    val bollingerPercentB: Double = 0.0,
    val vwap: Double,
    val superTrend: Double,
    val isSuperTrendBullish: Boolean,
    val atr: Double,
    val volume20Avg: Long = 0L,
    val currentVolume: Long = 0L,
    val isVolumeSpike: Boolean = false,
    val openInterest: Long = 0L,
    val putCallRatio: Double = 1.0,
    val isMacdCrossAbove: Boolean = false,
    val isMacdNegative: Boolean = false,
    val stochasticK: Double,
    val stochasticD: Double,
    val adx: Double,
    val obv: Double,
    val supportLevel1: Double,
    val resistanceLevel1: Double,
    val overallSentiment: SignalType
)

data class ForecastStep(
    val stepLabel: String,
    val predictedPrice: Double,
    val returnPercent: Double,
    val lowerBound: Double,
    val upperBound: Double
)

data class MLPredictionResult(
    val symbol: String,
    val currentPrice: Double,
    val predictedPrice: Double,
    val horizon: String, // e.g. "1 Day", "3 Days", "1 Week"
    val confidence: Int, // e.g. 84%
    val trend: TrendDirection,
    val expectedReturnPercent: Double,
    val modelType: String = "TensorFlow Lite Stacked LSTM (128-64 units)",
    val featureMetrics: List<Pair<String, Double>> = emptyList(),
    val trainedTimestamp: String,
    val status: String = "TFLite Active",
    val multiStepForecast: List<ForecastStep> = emptyList(),
    val inferenceTimeMs: Long = 4L,
    val inputTensorShape: String = "[1, 30, 8]",
    val outputTensorShape: String = "[1, 5]",
    val executionDelegate: String = "TFLite XNNPACK / CPU"
) {
    val isIncrease: Boolean get() = trend == TrendDirection.UP || expectedReturnPercent > 0.05
    val isDecrease: Boolean get() = trend == TrendDirection.DOWN || expectedReturnPercent < -0.05
    val isSideways: Boolean get() = !isIncrease && !isDecrease

    val directionHeadline: String get() = when {
        isIncrease -> "PREDICTED TO INCREASE ↗"
        isDecrease -> "PREDICTED TO DECREASE ↘"
        else -> "PREDICTED SIDEWAYS / CONSOLIDATION ↔"
    }

    val increaseProbability: Int get() = when {
        isIncrease -> confidence.coerceIn(58, 96)
        isDecrease -> (100 - confidence).coerceIn(4, 42)
        else -> 50
    }

    val decreaseProbability: Int get() = 100 - increaseProbability

    val tradeOutlookSummary: String get() = when {
        isIncrease -> "Bullish Momentum: Price is forecasted to INCREASE by +${String.format(Locale.US, "%.2f", expectedReturnPercent)}% towards target ₹${String.format(Locale.US, "%,.2f", predictedPrice)}. Favorable for manual BUY trades."
        isDecrease -> "Bearish Resistance: Price is forecasted to DECREASE by ${String.format(Locale.US, "%.2f", expectedReturnPercent)}% towards target ₹${String.format(Locale.US, "%,.2f", predictedPrice)}. Elevated downside risk for manual long positions."
        else -> "Range-bound consolidation: Price expected to fluctuate within narrow band without clear directional bias."
    }

    val manualExecutionSafetyNotice: String = "🛡️ Manual Order Execution Only: The app predicts whether the price will increase or decrease, but will NEVER place trades automatically. You retain complete manual control."
}

data class XGBoostRuleCheck(
    val ruleLabel: String,
    val conditionDescription: String,
    val observedValue: String,
    val isSatisfied: Boolean,
    val weight: String
)

data class FeatureRankingItem(
    val featureName: String,
    val importancePercent: Int,
    val source: String,
    val observedValue: String
)

data class EngineeredFeatureMatrix(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val rsi: Double,
    val ema20: Double,
    val ema50: Double,
    val macdHistogram: Double,
    val vwap: Double,
    val atr: Double,
    val putCallRatio: Double,
    val openInterest: Long
)

data class AIRecommendation(
    val symbol: String,
    val signal: SignalType,
    val confidence: Int,
    val riskLevel: RiskLevel,
    val currentPrice: Double,
    val targetPrice: Double,
    val stopLoss: Double,
    val primaryReasons: List<String>,
    val indicatorConsensus: String,
    val disclaimer: String = "Educational machine learning signal only. Not guaranteed financial advice.",
    val riskPercent: Double = 2.5,
    val isRiskAlert: Boolean = false,
    val riskAlertMessage: String = "",
    val stopLossAtrMultiplier: Double = 1.5,
    val targetAtrMultiplier: Double = 2.5,
    val atrValue: Double = 0.0,
    val decisionRuleChecks: List<XGBoostRuleCheck> = emptyList(),
    val featureRankings: List<FeatureRankingItem> = emptyList(),
    val engineeredFeatures: EngineeredFeatureMatrix? = null,
    val modelPipeline: String = "SmartAPI Market Feed ➔ Technical Engine (EMA, RSI, MACD, VWAP, Bollinger, ATR, Supertrend) ➔ XGBoost Classifier + Random Forest Confidence ➔ Action"
) {
    val isIncrease: Boolean get() = signal == SignalType.BUY
    val isDecrease: Boolean get() = signal == SignalType.SELL
    val predictedDirectionText: String get() = when (signal) {
        SignalType.BUY -> "PREDICTED TO INCREASE ↗"
        SignalType.SELL -> "PREDICTED TO DECREASE ↘"
        SignalType.HOLD -> "PREDICTED TO MOVE SIDEWAYS ↔"
    }
    val noAutoExecutionDisclaimer: String = "⚡ Strictly Manual Execution: This prediction is for your review. No order is or will be executed automatically."
}

data class PortfolioSummary(
    val totalPortfolioValue: Double,
    val virtualCashBalance: Double,
    val totalInvested: Double,
    val currentValuation: Double,
    val todayPnl: Double,
    val totalPnl: Double,
    val returnPercentage: Double
) {
    val formattedTotal: String get() = "₹" + String.format(Locale.US, "%,.2f", totalPortfolioValue)
    val formattedCash: String get() = "₹" + String.format(Locale.US, "%,.2f", virtualCashBalance)
    val formattedInvested: String get() = "₹" + String.format(Locale.US, "%,.2f", totalInvested)
    val formattedPnl: String get() = (if (totalPnl >= 0) "+₹" else "-₹") + String.format(Locale.US, "%,.2f", Math.abs(totalPnl))
    val formattedReturn: String get() = (if (returnPercentage >= 0) "+" else "") + String.format(Locale.US, "%.2f%%", returnPercentage)
}

data class HoldingItem(
    val symbol: String,
    val companyName: String,
    val exchange: Exchange,
    val token: String,
    val quantity: Int,
    val avgBuyPrice: Double,
    val currentLtp: Double
) {
    val investedValue: Double get() = quantity * avgBuyPrice
    val currentValue: Double get() = quantity * currentLtp
    val pnl: Double get() = currentValue - investedValue
    val pnlPercentage: Double get() = if (investedValue > 0) (pnl / investedValue) * 100.0 else 0.0
}

data class PaperTransaction(
    val id: Long = 0,
    val symbol: String,
    val companyName: String,
    val action: SignalType, // BUY or SELL
    val quantity: Int,
    val executionPrice: Double,
    val totalValue: Double,
    val timestamp: Long,
    val realizedPnl: Double = 0.0,
    val virtualBalanceAfter: Double
)

data class PriceAlert(
    val id: Long = 0,
    val symbol: String,
    val token: String,
    val targetPrice: Double,
    val condition: AlertCondition,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null
)

data class WhatsAppNotificationLog(
    val id: Long = 0,
    val eventType: NotificationEventType,
    val recipientNumber: String,
    val messageText: String,
    val status: NotificationStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val errorDetails: String? = null
)

data class DailyMarketTrendSummary(
    val headline: String,
    val executiveBriefing: String,
    val overallSentiment: SignalType,
    val sentimentScore: Int,
    val niftyOutlook: String,
    val bankNiftyOutlook: String,
    val keyDrivers: List<MacroDriverItem>,
    val sectorTrends: List<SectorTrendItem>,
    val topTakeaways: List<String>,
    val keyLevels: MarketKeyLevels,
    val generatedTimestamp: Long = System.currentTimeMillis(),
    val isLiveGeminiResponse: Boolean = true
)

data class MacroDriverItem(
    val title: String,
    val description: String,
    val impact: String,
    val iconType: String
)

data class SectorTrendItem(
    val sectorName: String,
    val trend: SignalType,
    val changePercent: Double,
    val keyDriver: String
)

data class MarketKeyLevels(
    val niftySupport: Double,
    val niftyResistance: Double,
    val bankNiftySupport: Double,
    val bankNiftyResistance: Double,
    val vixLevel: Double
)

data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val timeAgo: String,
    val sentiment: SignalType,
    val category: String,
    val aiTakeaway: String = "",
    val traderImpact: String = "",
    val relatedSymbol: String? = null,
    val url: String? = null,
    val isBookmarked: Boolean = false
)

enum class MutualFundCategory(val displayName: String) {
    ALL("All Funds"),
    FLEXI_CAP("Flexi Cap"),
    SMALL_CAP("Small Cap"),
    LARGE_CAP("Large Cap"),
    MID_CAP("Mid Cap"),
    INDEX_ETF("Index / ETFs"),
    HYBRID("Hybrid & Balanced"),
    DEBT("Debt & Liquid"),
    ELSS("ELSS Tax Saver")
}

data class MutualFund(
    val id: String,
    val name: String,
    val fundHouse: String,
    val category: MutualFundCategory,
    val nav: Double,
    val dayChange: Double,
    val dayChangePercent: Double,
    val oneYearReturn: Double,
    val threeYearReturn: Double,
    val fiveYearReturn: Double,
    val starRating: Int, // 1-5
    val expenseRatio: Double, // e.g. 0.65%
    val aum: String, // e.g. "₹45,230 Cr"
    val riskLevel: RiskLevel,
    val minSipAmount: Double = 500.0,
    val minLumpsumAmount: Double = 1000.0,
    val fundManager: String = "Fund Management Team",
    val benchmark: String = "Nifty 500 TRI",
    val description: String = "Actively managed equity scheme aimed at long-term capital appreciation."
) {
    val formattedNav: String get() = "₹" + String.format(Locale.US, "%,.2f", nav)
    val formattedDayChange: String get() = (if (dayChange >= 0) "+₹" else "-₹") + String.format(Locale.US, "%,.2f (%.2f%%)", Math.abs(dayChange), dayChangePercent)
    val isPositive: Boolean get() = dayChange >= 0
    val formatted1Y: String get() = (if (oneYearReturn >= 0) "+" else "") + String.format(Locale.US, "%.1f%%", oneYearReturn)
    val formatted3Y: String get() = (if (threeYearReturn >= 0) "+" else "") + String.format(Locale.US, "%.1f%%", threeYearReturn)
    val formatted5Y: String get() = (if (fiveYearReturn >= 0) "+" else "") + String.format(Locale.US, "%.1f%%", fiveYearReturn)
}

data class SipCalculationResult(
    val monthlyInvestment: Double,
    val tenureYears: Int,
    val expectedReturnRate: Double,
    val totalInvested: Double,
    val estimatedReturns: Double,
    val maturityValue: Double
) {
    val formattedInvested: String get() = "₹" + String.format(Locale.US, "%,.0f", totalInvested)
    val formattedReturns: String get() = "₹" + String.format(Locale.US, "%,.0f", estimatedReturns)
    val formattedMaturity: String get() = "₹" + String.format(Locale.US, "%,.0f", maturityValue)
}

enum class MarketMoverType(val title: String, val subtitle: String) {
    TOP_GAINERS("Top Gainers", "Leading market winners by % increase"),
    TOP_LOSERS("Top Losers", "Leading market dips by % decline"),
    VOLUME_SHOCKERS("Volume Movers", "Stocks with massive trading volume & liquidity"),
    HIGH_52W("52W High Movers", "Stocks trading near historic 52-week highs")
}

// ========================
// ANGEL ONE APP ARCHITECTURE MODELS
// ========================

enum class OrderType(val displayName: String, val description: String) {
    MARKET("Market", "Instant execution at prevailing market price"),
    LIMIT("Limit", "Execute at your desired trigger/target price"),
    STOP_LOSS("Stop Loss", "Risk protection SL-Limit / SL-Market order"),
    GTT("GTT", "Good Till Triggered valid until canceled"),
    BASKET("Basket", "Multiple equity/F&O orders executed together")
}

enum class OrderStatus(val displayName: String) {
    OPEN("Open"),
    EXECUTED("Executed"),
    CANCELLED("Cancelled"),
    TRIGGERED("Triggered"),
    REJECTED("Rejected")
}

enum class OrderProductType(val displayName: String, val code: String) {
    DELIVERY("Delivery (CNC)", "DELIVERY"),
    INTRADAY("Intraday (MIS)", "INTRADAY"),
    CARRYFORWARD("Carryforward (NRML)", "CARRYFORWARD")
}

data class AngelOneOrder(
    val orderId: String,
    val symbol: String,
    val companyName: String,
    val action: SignalType, // BUY or SELL
    val orderType: OrderType,
    val productType: OrderProductType = OrderProductType.DELIVERY,
    val quantity: Int,
    val price: Double,
    val triggerPrice: Double? = null,
    val status: OrderStatus = OrderStatus.OPEN,
    val timestamp: Long = System.currentTimeMillis(),
    val executedPrice: Double? = null,
    val rejectionReason: String? = null
) {
    val totalValue: Double get() = quantity * (executedPrice ?: price)
}

data class GttRule(
    val id: String,
    val symbol: String,
    val companyName: String,
    val action: SignalType,
    val triggerPrice: Double,
    val limitPrice: Double,
    val quantity: Int,
    val currentLtp: Double,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
) {
    val triggerCondition: String get() = if (triggerPrice >= currentLtp) ">= ₹${String.format(Locale.US, "%.2f", triggerPrice)}" else "<= ₹${String.format(Locale.US, "%.2f", triggerPrice)}"
}

data class BasketItem(
    val symbol: String,
    val companyName: String,
    val action: SignalType,
    val quantity: Int,
    val price: Double,
    val orderType: OrderType = OrderType.MARKET
) {
    val estimatedAmount: Double get() = quantity * price
}

data class BasketOrder(
    val id: String,
    val name: String,
    val description: String,
    val items: List<BasketItem>,
    val createdAt: Long = System.currentTimeMillis(),
    val isExecuted: Boolean = false
) {
    val totalAmount: Double get() = items.sumOf { it.estimatedAmount }
}

data class OptionStrike(
    val strikePrice: Double,
    val callLtp: Double,
    val callChange: Double,
    val callOi: Long,
    val callVolume: Long,
    val callIv: Double,
    val putLtp: Double,
    val putChange: Double,
    val putOi: Long,
    val putVolume: Long,
    val putIv: Double,
    val isAtm: Boolean = false,
    val isItmCall: Boolean = false,
    val isItmPut: Boolean = false
)

data class OptionChainData(
    val symbol: String,
    val underlyingLtp: Double,
    val expiryDate: String,
    val availableExpiries: List<String>,
    val pcrRatio: Double,
    val maxPain: Double,
    val totalCallOi: Long,
    val totalPutOi: Long,
    val strikes: List<OptionStrike>
)

data class WatchlistTab(
    val id: String,
    val name: String,
    val symbols: List<String>
)

data class ActiveSipItem(
    val id: String,
    val schemeName: String,
    val monthlyAmount: Double,
    val frequency: String = "Monthly",
    val nextDueDate: String,
    val totalInstallmentsPaid: Int,
    val totalInvested: Double,
    val currentValuation: Double,
    val status: String = "ACTIVE"
)


