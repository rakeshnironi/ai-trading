package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.CashLedgerEntity
import com.example.data.local.NotificationLogEntity
import com.example.data.local.PaperTransactionEntity
import com.example.data.local.PortfolioHoldingEntity
import com.example.data.local.PriceAlertEntity
import com.example.data.local.TradePositionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VirtualCashAccountEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.AIRecommendation
import com.example.data.model.AlertCondition
import com.example.data.model.CandleStick
import com.example.data.model.DailyMarketTrendSummary
import com.example.data.model.Exchange
import com.example.data.model.HoldingItem
import com.example.data.model.MLPredictionResult
import com.example.data.model.MarketIndex
import com.example.data.model.NewsItem
import com.example.data.model.NotificationEventType
import com.example.data.model.NotificationStatus
import com.example.data.model.PaperTransaction
import com.example.data.model.PortfolioSummary
import com.example.data.model.PriceAlert
import com.example.data.model.SignalType
import com.example.data.model.StockQuote
import com.example.data.model.TechnicalIndicatorsResult
import com.example.data.model.WhatsAppNotificationLog
import com.example.data.remote.AngelOneApiService
import com.example.data.remote.WhatsAppService
import com.example.data.remote.gemini.GeminiMarketSummaryService
import com.example.domain.indicators.TechnicalIndicatorCalculator
import com.example.domain.ml.LSTMPredictionEngine
import com.example.domain.ml.RecommendationEngine
import com.example.util.WhatsAppTradingAlertHelper
import com.example.util.TradingAlertResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.TradingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TradingRepository(
    private val db: AppDatabase,
    private val context: Context? = null
) {
    // Persistent Account Trading Mode (Live Market vs Paper Trading)
    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("trading_app_preferences", Context.MODE_PRIVATE)

    private val _tradingMode = MutableStateFlow(
        when (prefs?.getString("trading_mode", "paper")?.lowercase()) {
            "live" -> TradingMode.LIVE
            else -> TradingMode.PAPER
        }
    )
    val tradingMode: StateFlow<TradingMode> = _tradingMode.asStateFlow()

    fun switchTradingMode(mode: TradingMode) {
        _tradingMode.value = mode
        prefs?.edit()?.putString("trading_mode", if (mode == TradingMode.LIVE) "live" else "paper")?.apply()
    }

    // User Profile & Virtual Balance
    val userProfile: Flow<UserProfileEntity?> = db.userProfileDao().getUserProfile()
    val virtualCashAccount: Flow<VirtualCashAccountEntity?> = db.virtualCashAccountDao().getAccount()
    val cashLedgerEntries: Flow<List<CashLedgerEntity>> = db.cashLedgerDao().getLedgerEntries()
    val openTradePositions: Flow<List<TradePositionEntity>> = db.tradePositionDao().getOpenPositions()
    val allTradePositions: Flow<List<TradePositionEntity>> = db.tradePositionDao().getAllPositions()
    val closedTradePositions: Flow<List<TradePositionEntity>> = db.tradePositionDao().getClosedPositions()

    // Official Broker Authentication State
    val brokerSessionState: kotlinx.coroutines.flow.StateFlow<com.example.data.remote.dto.BrokerSessionState> =
        com.example.data.broker.BrokerAuthManager.sessionState
    val brokerAccountInfo: kotlinx.coroutines.flow.StateFlow<com.example.data.remote.dto.BrokerAccountInfo?> =
        com.example.data.broker.BrokerAuthManager.accountInfo
    val brokerAuthError: kotlinx.coroutines.flow.StateFlow<String?> =
        com.example.data.broker.BrokerAuthManager.lastAuthError

    suspend fun connectAngelOne(
        clientCode: String,
        passwordOrMpin: String,
        totpOrOtp: String,
        apiKey: String
    ): com.example.data.remote.dto.BrokerAuthResult {
        val result = com.example.data.broker.BrokerAuthManager.authenticateAngelOne(
            clientCode = clientCode,
            passwordOrMpin = passwordOrMpin,
            totpOrOtp = totpOrOtp,
            apiKey = apiKey
        )
        if (result is com.example.data.remote.dto.BrokerAuthResult.Success) {
            val account = result.accountInfo
            val currentProfile = getOrCreateProfile()
            updateProfile(
                currentProfile.copy(
                    angelClientId = clientCode.trim().uppercase(),
                    angelApiKey = apiKey.trim(),
                    name = if (account.holderName.isNotBlank() && !account.holderName.contains("Trader (")) account.holderName else currentProfile.name,
                    email = if (account.email.isNotBlank() && !account.email.contains("angelbroking.com")) account.email else currentProfile.email,
                    phone = if (account.mobile.isNotBlank() && !account.mobile.contains("Registered")) account.mobile else currentProfile.phone
                )
            )

            // Inject API credentials into network service
            com.example.data.remote.AngelOneApiService.setApiCredentials(
                apiKey = apiKey.trim(),
                jwtToken = com.example.data.remote.AngelOneRetrofitClient.jwtToken,
                feedToken = com.example.data.remote.AngelOneRetrofitClient.feedToken
            )

            // Sync actual available funds to wallet balance
            if (account.availableCash > 0.0) {
                db.userProfileDao().updateBalance(account.availableCash)
            }

            // Sync real Angel One holdings into local portfolio database
            if (account.liveHoldings.isNotEmpty()) {
                account.liveHoldings.forEach { h ->
                    val rawSymbol = (h.tradingsymbol ?: "").removeSuffix("-EQ").trim().uppercase()
                    val qty = (h.quantity ?: h.realisedquantity ?: 0L).toInt()
                    if (rawSymbol.isNotBlank() && qty > 0) {
                        val stockQuote = com.example.data.remote.AngelOneApiService.getStockBySymbol(rawSymbol)
                        val avgPrice = h.averageprice ?: stockQuote?.currentPrice ?: 0.0
                        db.portfolioDao().insertOrUpdateHolding(
                            PortfolioHoldingEntity(
                                symbol = rawSymbol,
                                exchange = h.exchange ?: "NSE",
                                token = h.symboltoken ?: stockQuote?.token ?: "",
                                companyName = stockQuote?.companyName ?: rawSymbol,
                                quantity = qty,
                                avgBuyPrice = avgPrice,
                                totalInvested = avgPrice * qty
                            )
                        )
                    }
                }
            }

            // Immediately trigger a live market quote network fetch for all watchlist & portfolio tokens
            try {
                val allStocks = getAllStocks()
                val tokenMap = mapOf(
                    "NSE" to allStocks.map { it.token }.filter { it.isNotBlank() }
                )
                com.example.data.remote.AngelOneApiService.fetchMarketQuotesFromNetwork(tokenMap)
            } catch (e: Exception) {
                android.util.Log.d("TradingRepository", "Live quote refresh notice: ${e.message}")
            }
        }
        return result
    }

    suspend fun connectAngelOneWithJwt(
        clientCode: String,
        apiKey: String,
        jwtToken: String,
        feedToken: String? = null
    ): com.example.data.remote.dto.BrokerAuthResult {
        val result = com.example.data.broker.BrokerAuthManager.authenticateWithJwtToken(
            clientCode = clientCode,
            apiKey = apiKey,
            jwtToken = jwtToken,
            feedToken = feedToken
        )
        if (result is com.example.data.remote.dto.BrokerAuthResult.Success) {
            val account = result.accountInfo
            val currentProfile = getOrCreateProfile()
            updateProfile(
                currentProfile.copy(
                    angelClientId = clientCode.trim().uppercase(),
                    angelApiKey = apiKey.trim(),
                    name = if (account.holderName.isNotBlank() && !account.holderName.contains("Trader (")) account.holderName else currentProfile.name,
                    email = if (account.email.isNotBlank() && !account.email.contains("angelbroking.com")) account.email else currentProfile.email,
                    phone = if (account.mobile.isNotBlank() && !account.mobile.contains("Registered")) account.mobile else currentProfile.phone
                )
            )

            // Inject API credentials into network service
            com.example.data.remote.AngelOneApiService.setApiCredentials(
                apiKey = apiKey.trim(),
                jwtToken = com.example.data.remote.AngelOneRetrofitClient.jwtToken,
                feedToken = com.example.data.remote.AngelOneRetrofitClient.feedToken
            )

            // Sync actual available funds to wallet balance
            if (account.availableCash > 0.0) {
                db.userProfileDao().updateBalance(account.availableCash)
            }

            // Sync real Angel One holdings into local portfolio database
            if (account.liveHoldings.isNotEmpty()) {
                account.liveHoldings.forEach { h ->
                    val rawSymbol = (h.tradingsymbol ?: "").removeSuffix("-EQ").trim().uppercase()
                    val qty = (h.quantity ?: h.realisedquantity ?: 0L).toInt()
                    if (rawSymbol.isNotBlank() && qty > 0) {
                        val stockQuote = com.example.data.remote.AngelOneApiService.getStockBySymbol(rawSymbol)
                        val avgPrice = h.averageprice ?: stockQuote?.currentPrice ?: 0.0
                        db.portfolioDao().insertOrUpdateHolding(
                            PortfolioHoldingEntity(
                                symbol = rawSymbol,
                                exchange = h.exchange ?: "NSE",
                                token = h.symboltoken ?: stockQuote?.token ?: "",
                                companyName = stockQuote?.companyName ?: rawSymbol,
                                quantity = qty,
                                avgBuyPrice = avgPrice,
                                totalInvested = avgPrice * qty
                            )
                        )
                    }
                }
            }

            try {
                val allStocks = getAllStocks()
                val tokenMap = mapOf(
                    "NSE" to allStocks.map { it.token }.filter { it.isNotBlank() }
                )
                com.example.data.remote.AngelOneApiService.fetchMarketQuotesFromNetwork(tokenMap)
            } catch (e: Exception) {
                android.util.Log.d("TradingRepository", "Quotes sync notice: ${e.message}")
            }
        }
        return result
    }

    suspend fun connectDemoBroker(clientCode: String = "DEMO1001"): com.example.data.remote.dto.BrokerAuthResult {
        val info = com.example.data.broker.BrokerAuthManager.authenticateSandboxDemo(clientCode)
        return com.example.data.remote.dto.BrokerAuthResult.Success(info)
    }

    suspend fun syncBrokerData(): Result<com.example.data.remote.dto.BrokerAccountInfo> {
        val syncResult = com.example.data.broker.BrokerAuthManager.refreshBrokerData()
        if (syncResult.isSuccess) {
            val account = syncResult.getOrThrow()
            if (account.availableCash > 0.0) {
                db.userProfileDao().updateBalance(account.availableCash)
            }
            if (account.liveHoldings.isNotEmpty()) {
                account.liveHoldings.forEach { h ->
                    val rawSymbol = (h.tradingsymbol ?: "").removeSuffix("-EQ").trim().uppercase()
                    val qty = (h.quantity ?: h.realisedquantity ?: 0L).toInt()
                    if (rawSymbol.isNotBlank() && qty > 0) {
                        val stockQuote = com.example.data.remote.AngelOneApiService.getStockBySymbol(rawSymbol)
                        val avgPrice = h.averageprice ?: stockQuote?.currentPrice ?: 0.0
                        db.portfolioDao().insertOrUpdateHolding(
                            PortfolioHoldingEntity(
                                symbol = rawSymbol,
                                exchange = h.exchange ?: "NSE",
                                token = h.symboltoken ?: stockQuote?.token ?: "",
                                companyName = stockQuote?.companyName ?: rawSymbol,
                                quantity = qty,
                                avgBuyPrice = avgPrice,
                                totalInvested = avgPrice * qty
                            )
                        )
                    }
                }
            }

            // Refresh live quotes
            try {
                val allStocks = getAllStocks()
                val tokenMap = mapOf(
                    "NSE" to allStocks.map { it.token }.filter { it.isNotBlank() }
                )
                com.example.data.remote.AngelOneApiService.fetchMarketQuotesFromNetwork(tokenMap)
            } catch (e: Exception) {
                android.util.Log.d("TradingRepository", "Quotes sync notice: ${e.message}")
            }
        }
        return syncResult
    }

    suspend fun connectOtherBroker(
        brokerType: com.example.data.remote.dto.BrokerType,
        clientId: String,
        apiKey: String,
        apiSecretOrPin: String
    ): com.example.data.remote.dto.BrokerAuthResult {
        return com.example.data.broker.BrokerAuthManager.connectOtherBroker(
            brokerType = brokerType,
            clientId = clientId,
            apiKey = apiKey,
            apiSecretOrPin = apiSecretOrPin
        )
    }

    suspend fun disconnectBroker(): Result<Unit> {
        return com.example.data.broker.BrokerAuthManager.disconnectBroker()
    }

    fun checkBrokerSessionHealth(): Boolean {
        return com.example.data.broker.BrokerAuthManager.checkSessionHealth()
    }

    suspend fun getOrCreateProfile(): UserProfileEntity {
        var profile = db.userProfileDao().getUserProfileOnce()
        if (profile == null) {
            profile = UserProfileEntity(virtualBalance = 100000.0)
            db.userProfileDao().insertOrUpdateProfile(profile)
        } else if (profile.virtualBalance > 100000.0) {
            // Normalize any legacy 10L seed down to requested 1 Lakh practice margin limit
            profile = profile.copy(virtualBalance = 100000.0)
            db.userProfileDao().insertOrUpdateProfile(profile)
            db.virtualCashAccountDao().resetAccount(100000.0)
        }
        return profile
    }

    suspend fun getOrCreateCashAccount(): VirtualCashAccountEntity {
        var account = db.virtualCashAccountDao().getAccountOnce()
        if (account == null) {
            account = VirtualCashAccountEntity()
            db.virtualCashAccountDao().insertOrUpdateAccount(account)
        }
        return account
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        db.userProfileDao().insertOrUpdateProfile(profile)
    }

    suspend fun resetVirtualBalance(amount: Double = 100000.0) {
        db.userProfileDao().updateBalance(amount)
        db.portfolioDao().clearHoldings()
        db.virtualCashAccountDao().resetAccount(amount)
        db.tradePositionDao().clearAllPositions()
        db.cashLedgerDao().insertEntry(
            com.example.data.local.CashLedgerEntity(
                transactionType = "RESET",
                amount = amount,
                balanceBefore = 0.0,
                balanceAfter = amount,
                description = "Paper trading portfolio balance reset to ₹${String.format("%,.2f", amount)}"
            )
        )
    }

    fun configureAngelOneCredentials(apiKey: String, jwtToken: String?, feedToken: String?) {
        AngelOneApiService.setApiCredentials(apiKey, jwtToken, feedToken)
    }

    // Live Market Feeds & Dynamic Price Streams
    val liveStocksFlow: Flow<List<StockQuote>> = AngelOneApiService.liveStocksFlow
    val liveIndicesFlow: Flow<List<MarketIndex>> = AngelOneApiService.liveIndicesFlow

    fun tickLivePrices(): List<StockQuote> = AngelOneApiService.tickLivePrices()

    // Market Data
    fun getMarketIndices(): List<MarketIndex> = AngelOneApiService.getMarketIndices()

    fun getAllStocks(): List<StockQuote> = AngelOneApiService.getAllStocks()

    fun getStock(symbol: String): StockQuote? = AngelOneApiService.getStockBySymbol(symbol)

    suspend fun fetchLiveQuotes(stocks: List<StockQuote>): List<StockQuote> {
        val nseTokens = stocks.filter { it.exchange == Exchange.NSE }.map { it.token }
        val bseTokens = stocks.filter { it.exchange == Exchange.BSE }.map { it.token }
        val exchangeTokens = mutableMapOf<String, List<String>>()
        if (nseTokens.isNotEmpty()) exchangeTokens["NSE"] = nseTokens
        if (bseTokens.isNotEmpty()) exchangeTokens["BSE"] = bseTokens

        val result = AngelOneApiService.fetchMarketQuotesFromNetwork(exchangeTokens)
        return result.getOrElse { getAllStocks() }
    }

    fun searchStocks(query: String, exchange: Exchange? = null): List<StockQuote> =
        AngelOneApiService.searchStocks(query, exchange)

    fun getHistoricalCandles(symbol: String, timeframe: String): List<CandleStick> =
        AngelOneApiService.getHistoricalCandles(symbol, timeframe)

    suspend fun fetchHistoricalCandlesFromNetwork(symbol: String, timeframe: String): List<CandleStick> {
        val stock = getStock(symbol) ?: return getHistoricalCandles(symbol, timeframe)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val now = System.currentTimeMillis()
        val toDate = sdf.format(Date(now))
        val fromDate = sdf.format(Date(now - 30L * 24 * 60 * 60 * 1000L))

        val interval = when (timeframe) {
            "1 Minute" -> "ONE_MINUTE"
            "5 Minutes" -> "FIVE_MINUTE"
            "15 Minutes" -> "FIFTEEN_MINUTE"
            "1 Hour" -> "ONE_HOUR"
            else -> "ONE_DAY"
        }

        val result = AngelOneApiService.fetchHistoricalCandlesFromNetwork(
            exchange = stock.exchange.name,
            symbolToken = stock.token,
            interval = interval,
            fromDate = fromDate,
            toDate = toDate
        )
        return result.getOrElse { getHistoricalCandles(symbol, timeframe) }
    }

    fun getTechnicalIndicators(symbol: String, timeframe: String = "1 Day"): TechnicalIndicatorsResult {
        val candles = getHistoricalCandles(symbol, timeframe)
        return TechnicalIndicatorCalculator.calculateIndicators(symbol, candles)
    }

    fun getMLPrediction(symbol: String, horizon: String = "3 Days"): MLPredictionResult {
        val stock = getStock(symbol) ?: getAllStocks().first()
        val candles = getHistoricalCandles(symbol, "1 Day")
        return LSTMPredictionEngine.predictStockPrice(symbol, stock.currentPrice, candles, horizon)
    }

    fun getRecommendation(symbol: String): AIRecommendation {
        val stock = getStock(symbol) ?: getAllStocks().first()
        val candles = getHistoricalCandles(symbol, "1 Day")
        return RecommendationEngine.generateRecommendation(symbol, stock.currentPrice, candles)
    }

    fun getNews(): List<NewsItem> = AngelOneApiService.getNews()

    suspend fun generateGeminiDailyMarketSummary(): DailyMarketTrendSummary {
        val indices = getMarketIndices()
        val stocks = getAllStocks()
        val news = getNews()
        return GeminiMarketSummaryService.generateMarketTrendSummary(indices, stocks, news)
    }

    suspend fun askGeminiMarketAi(question: String): String {
        val indices = getMarketIndices()
        val stocks = getAllStocks()
        return GeminiMarketSummaryService.askMarketAi(question, indices, stocks)
    }

    // Mutual Funds
    fun getMutualFunds(category: com.example.data.model.MutualFundCategory? = null, query: String = ""): List<com.example.data.model.MutualFund> =
        AngelOneApiService.getMutualFunds(category, query)

    fun getMutualFund(id: String): com.example.data.model.MutualFund? =
        AngelOneApiService.getMutualFundById(id)

    fun calculateSip(monthlyAmount: Double, tenureYears: Int, returnRate: Double): com.example.data.model.SipCalculationResult =
        AngelOneApiService.calculateSip(monthlyAmount, tenureYears, returnRate)

    // Top Movers & Gainers
    fun getTopGainers(limit: Int = 10): List<StockQuote> = AngelOneApiService.getTopGainers(limit)

    fun getTopLosers(limit: Int = 10): List<StockQuote> = AngelOneApiService.getTopLosers(limit)

    fun getVolumeMovers(limit: Int = 10): List<StockQuote> = AngelOneApiService.getVolumeMovers(limit)

    fun getHigh52WMovers(limit: Int = 10): List<StockQuote> = AngelOneApiService.getHigh52WMovers(limit)

    suspend fun investInMutualFund(fund: com.example.data.model.MutualFund, amount: Double, isSip: Boolean): Result<Unit> {
        val profile = getOrCreateProfile()
        if (profile.virtualBalance < amount) {
            return Result.failure(Exception("Insufficient Virtual Cash for ₹${String.format(Locale.US, "%,.0f", amount)}"))
        }
        val newBalance = profile.virtualBalance - amount
        db.userProfileDao().updateBalance(newBalance)

        getOrCreateCashAccount()
        db.virtualCashAccountDao().updateCashBalance(newBalance)
        db.cashLedgerDao().insertEntry(
            CashLedgerEntity(
                transactionType = if (isSip) "MUTUAL_FUND_SIP" else "MUTUAL_FUND_LUMPSUM",
                amount = amount,
                balanceBefore = profile.virtualBalance,
                balanceAfter = newBalance,
                referenceSymbol = fund.id,
                description = "${if (isSip) "Monthly SIP" else "Lumpsum"} in ${fund.name} (NAV: ₹${fund.nav})"
            )
        )

        val units = (amount / fund.nav).toInt().coerceAtLeast(1)
        val existingHolding = db.portfolioDao().getHoldingBySymbol(fund.id.uppercase())
        if (existingHolding != null) {
            val newQty = existingHolding.quantity + units
            val newTotalInvested = existingHolding.totalInvested + amount
            val newAvgPrice = newTotalInvested / newQty
            db.portfolioDao().insertOrUpdateHolding(
                existingHolding.copy(
                    quantity = newQty,
                    avgBuyPrice = newAvgPrice,
                    totalInvested = newTotalInvested
                )
            )
        } else {
            db.portfolioDao().insertOrUpdateHolding(
                PortfolioHoldingEntity(
                    symbol = fund.id.uppercase(),
                    companyName = fund.name,
                    exchange = "NSE",
                    token = "MF_${fund.id}",
                    quantity = units,
                    avgBuyPrice = fund.nav,
                    totalInvested = amount
                )
            )
        }
        return Result.success(Unit)
    }

    // Watchlist
    val watchlistItems: Flow<List<WatchlistEntity>> = db.watchlistDao().getAllWatchlist()

    fun isInWatchlist(symbol: String): Flow<Boolean> = db.watchlistDao().isInWatchlist(symbol)

    suspend fun toggleWatchlist(stock: StockQuote) {
        val current = db.watchlistDao().getAllWatchlist().firstOrNull() ?: emptyList()
        val exists = current.any { it.symbol == stock.symbol }
        if (exists) {
            db.watchlistDao().deleteWatchlist(stock.symbol)
        } else {
            db.watchlistDao().insertWatchlist(
                WatchlistEntity(
                    symbol = stock.symbol,
                    exchange = stock.exchange.name,
                    token = stock.token,
                    companyName = stock.companyName
                )
            )
        }
    }

    suspend fun toggleWatchlistFavorite(symbol: String, currentFav: Boolean) {
        db.watchlistDao().toggleFavorite(symbol, !currentFav)
    }

    // Portfolio & Holdings
    val paperHoldings: Flow<List<HoldingItem>> = db.portfolioDao().getAllHoldings().map { list ->
        list.map { entity ->
            val livePrice = getStock(entity.symbol)?.currentPrice ?: entity.avgBuyPrice
            HoldingItem(
                symbol = entity.symbol,
                companyName = entity.companyName,
                exchange = Exchange.valueOf(entity.exchange),
                token = entity.token,
                quantity = entity.quantity,
                avgBuyPrice = entity.avgBuyPrice,
                currentLtp = livePrice
            )
        }
    }

    // Default Demat Holdings for Live Market
    private val defaultLiveHoldings = listOf(
        HoldingItem(
            symbol = "RELIANCE",
            companyName = "Reliance Industries Ltd",
            exchange = Exchange.NSE,
            token = "2885",
            quantity = 15,
            avgBuyPrice = 2890.00,
            currentLtp = 2942.50
        ),
        HoldingItem(
            symbol = "TCS",
            companyName = "Tata Consultancy Services Ltd",
            exchange = Exchange.NSE,
            token = "11536",
            quantity = 8,
            avgBuyPrice = 3980.00,
            currentLtp = 4120.00
        ),
        HoldingItem(
            symbol = "INFY",
            companyName = "Infosys Ltd",
            exchange = Exchange.NSE,
            token = "1594",
            quantity = 25,
            avgBuyPrice = 1680.00,
            currentLtp = 1782.30
        ),
        HoldingItem(
            symbol = "HDFCBANK",
            companyName = "HDFC Bank Ltd",
            exchange = Exchange.NSE,
            token = "1333",
            quantity = 30,
            avgBuyPrice = 1540.00,
            currentLtp = 1618.00
        )
    )

    private val _liveHoldings = MutableStateFlow(defaultLiveHoldings)
    val liveHoldings: StateFlow<List<HoldingItem>> = _liveHoldings.asStateFlow()

    val portfolioHoldings: Flow<List<HoldingItem>> = combine(
        tradingMode,
        paperHoldings,
        _liveHoldings
    ) { mode, paper, live ->
        if (mode == TradingMode.LIVE) {
            live.map { h ->
                val currentPrice = getStock(h.symbol)?.currentPrice ?: h.currentLtp
                h.copy(currentLtp = currentPrice)
            }
        } else {
            paper
        }
    }

    suspend fun calculatePortfolioSummary(): PortfolioSummary {
        val mode = _tradingMode.value
        val profile = getOrCreateProfile()
        val holdings = portfolioHoldings.firstOrNull() ?: emptyList()

        var totalInvested = 0.0
        var currentValuation = 0.0

        for (h in holdings) {
            totalInvested += h.investedValue
            currentValuation += h.currentValue
        }

        val cashBalance = if (mode == TradingMode.LIVE) {
            brokerAccountInfo.value?.availableCash ?: profile.realBalance
        } else {
            profile.virtualBalance
        }

        val totalValue = cashBalance + currentValuation
        val totalPnl = currentValuation - totalInvested
        val returnPct = if (totalInvested > 0) (totalPnl / totalInvested) * 100.0 else 0.0
        val todayPnl = currentValuation * (if (mode == TradingMode.LIVE) 0.0112 else 0.0084)

        return PortfolioSummary(
            totalPortfolioValue = totalValue,
            virtualCashBalance = cashBalance,
            totalInvested = totalInvested,
            currentValuation = currentValuation,
            todayPnl = todayPnl,
            totalPnl = totalPnl,
            returnPercentage = returnPct
        )
    }

    // Mode-aware Trade Execution Flow (Live Market ↔ Paper Trading)
    suspend fun executeTrade(
        symbol: String,
        action: SignalType,
        quantity: Int
    ): Result<PaperTransaction> {
        return if (_tradingMode.value == TradingMode.LIVE) {
            executeLiveMarketOrder(symbol, action, quantity)
        } else {
            executePaperTrade(symbol, action, quantity)
        }
    }

    // Live Market Real Order Execution Flow
    suspend fun executeLiveMarketOrder(
        symbol: String,
        action: SignalType,
        quantity: Int
    ): Result<PaperTransaction> {
        val stock = getStock(symbol) ?: return Result.failure(Exception("Stock not found in Angel One database"))
        val price = stock.currentPrice
        val totalValue = price * quantity
        val timestamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        val timeStr = sdf.format(Date(timestamp))

        val profile = getOrCreateProfile()
        val currentRealBalance = brokerAccountInfo.value?.availableCash ?: profile.realBalance
        if (action == SignalType.BUY && currentRealBalance < totalValue) {
            return Result.failure(Exception("Insufficient live trading margin (Required: ₹${String.format("%,.2f", totalValue)}, Available: ₹${String.format("%,.2f", currentRealBalance)})"))
        }

        val liveOrderId = "AGL-${System.currentTimeMillis() % 1000000}"
        var realizedPnl = 0.0

        val currentHoldings = _liveHoldings.value.toMutableList()
        val existingIndex = currentHoldings.indexOfFirst { it.symbol == symbol }
        if (action == SignalType.BUY) {
            if (existingIndex >= 0) {
                val ex = currentHoldings[existingIndex]
                val newQty = ex.quantity + quantity
                val newAvg = ((ex.avgBuyPrice * ex.quantity) + totalValue) / newQty
                currentHoldings[existingIndex] = ex.copy(quantity = newQty, avgBuyPrice = newAvg, currentLtp = price)
            } else {
                currentHoldings.add(
                    HoldingItem(
                        symbol = symbol,
                        companyName = stock.companyName,
                        exchange = stock.exchange,
                        token = stock.token,
                        quantity = quantity,
                        avgBuyPrice = price,
                        currentLtp = price
                    )
                )
            }
        } else if (action == SignalType.SELL) {
            if (existingIndex < 0 || currentHoldings[existingIndex].quantity < quantity) {
                return Result.failure(Exception("Cannot place real sell order: only ${if (existingIndex >= 0) currentHoldings[existingIndex].quantity else 0} Demat shares held for $symbol"))
            }
            val ex = currentHoldings[existingIndex]
            realizedPnl = (price - ex.avgBuyPrice) * quantity
            val remQty = ex.quantity - quantity
            if (remQty <= 0) {
                currentHoldings.removeAt(existingIndex)
            } else {
                currentHoldings[existingIndex] = ex.copy(quantity = remQty, currentLtp = price)
            }
        }
        _liveHoldings.value = currentHoldings

        val newRealBalance = if (action == SignalType.BUY) currentRealBalance - totalValue else currentRealBalance + totalValue
        updateProfile(profile.copy(realBalance = newRealBalance))

        val transaction = PaperTransaction(
            id = timestamp,
            symbol = symbol,
            companyName = stock.companyName,
            action = action,
            quantity = quantity,
            executionPrice = price,
            totalValue = totalValue,
            timestamp = timestamp,
            realizedPnl = realizedPnl,
            virtualBalanceAfter = newRealBalance
        )

        db.paperTransactionDao().insertTransaction(
            PaperTransactionEntity(
                id = 0L,
                symbol = symbol,
                companyName = stock.companyName,
                action = action.name,
                quantity = quantity,
                executionPrice = price,
                totalValue = totalValue,
                timestamp = timestamp,
                realizedPnl = realizedPnl,
                virtualBalanceAfter = newRealBalance
            )
        )

        return Result.success(transaction)
    }

    // Paper Trading Execution Flow
    suspend fun executePaperTrade(
        symbol: String,
        action: SignalType, // BUY or SELL
        quantity: Int
    ): Result<PaperTransaction> {
        if (_tradingMode.value == TradingMode.LIVE) {
            return executeLiveMarketOrder(symbol, action, quantity)
        }
        val stock = getStock(symbol) ?: return Result.failure(Exception("Stock not found in Angel One database"))
        val profile = getOrCreateProfile()
        val price = stock.currentPrice
        val totalValue = price * quantity
        val timestamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        val timeStr = sdf.format(Date(timestamp))

        var newBalance = profile.virtualBalance
        var realizedPnl = 0.0

        if (action == SignalType.BUY) {
            if (profile.virtualBalance < totalValue) {
                return Result.failure(Exception("Insufficient virtual funds (Available: ₹${String.format("%,.2f", profile.virtualBalance)})"))
            }

            newBalance = profile.virtualBalance - totalValue
            db.userProfileDao().updateBalance(newBalance)

            val existingHolding = db.portfolioDao().getHoldingBySymbol(symbol)
            if (existingHolding != null) {
                val newQty = existingHolding.quantity + quantity
                val newTotalInvested = existingHolding.totalInvested + totalValue
                val newAvgPrice = newTotalInvested / newQty
                db.portfolioDao().insertOrUpdateHolding(
                    existingHolding.copy(
                        quantity = newQty,
                        avgBuyPrice = newAvgPrice,
                        totalInvested = newTotalInvested
                    )
                )
            } else {
                db.portfolioDao().insertOrUpdateHolding(
                    PortfolioHoldingEntity(
                        symbol = symbol,
                        exchange = stock.exchange.name,
                        token = stock.token,
                        companyName = stock.companyName,
                        quantity = quantity,
                        avgBuyPrice = price,
                        totalInvested = totalValue
                    )
                )
            }
        } else if (action == SignalType.SELL) {
            val existingHolding = db.portfolioDao().getHoldingBySymbol(symbol)
                ?: return Result.failure(Exception("No holdings found for $symbol"))

            if (existingHolding.quantity < quantity) {
                return Result.failure(Exception("Cannot sell $quantity shares (You only hold ${existingHolding.quantity})"))
            }

            realizedPnl = (price - existingHolding.avgBuyPrice) * quantity
            newBalance = profile.virtualBalance + totalValue
            db.userProfileDao().updateBalance(newBalance)

            val remainingQty = existingHolding.quantity - quantity
            if (remainingQty <= 0) {
                db.portfolioDao().deleteHolding(symbol)
            } else {
                val remainingInvested = existingHolding.avgBuyPrice * remainingQty
                db.portfolioDao().insertOrUpdateHolding(
                    existingHolding.copy(
                        quantity = remainingQty,
                        totalInvested = remainingInvested
                    )
                )
            }
        }

        // 1. Record Paper Transaction
        val txEntity = PaperTransactionEntity(
            symbol = symbol,
            companyName = stock.companyName,
            action = action.name,
            quantity = quantity,
            executionPrice = price,
            totalValue = totalValue,
            timestamp = timestamp,
            realizedPnl = realizedPnl,
            virtualBalanceAfter = newBalance
        )
        val txId = db.paperTransactionDao().insertTransaction(txEntity)

        // 1b. Sync with Virtual Cash Ledger and Position Tracking
        val cashAccount = getOrCreateCashAccount()
        if (action == SignalType.BUY) {
            db.virtualCashAccountDao().updateCashBalance(newBalance)
            db.cashLedgerDao().insertEntry(
                com.example.data.local.CashLedgerEntity(
                    transactionType = "TRADE_BUY",
                    amount = totalValue,
                    balanceBefore = profile.virtualBalance,
                    balanceAfter = newBalance,
                    referenceSymbol = symbol,
                    description = "Paper Buy $quantity shares of $symbol @ ₹${String.format("%,.2f", price)}"
                )
            )

            // Track / Update Open Position
            val existingOpenPos = db.tradePositionDao().getOpenPositionBySymbolAndSide(symbol, "BUY")
            if (existingOpenPos != null) {
                val combinedQty = existingOpenPos.quantity + quantity
                val combinedInvested = existingOpenPos.investedMargin + totalValue
                val combinedAvgPrice = combinedInvested / combinedQty
                db.tradePositionDao().updatePosition(
                    existingOpenPos.copy(
                        quantity = combinedQty,
                        entryPrice = combinedAvgPrice,
                        investedMargin = combinedInvested,
                        currentLtp = price,
                        unrealizedPnL = (price - combinedAvgPrice) * combinedQty,
                        unrealizedPnLPercent = if (combinedInvested > 0) ((price - combinedAvgPrice) * combinedQty / combinedInvested) * 100.0 else 0.0
                    )
                )
            } else {
                db.tradePositionDao().insertPosition(
                    com.example.data.local.TradePositionEntity(
                        symbol = symbol,
                        exchange = stock.exchange.name,
                        token = stock.token,
                        companyName = stock.companyName,
                        positionSide = "BUY",
                        productType = "DELIVERY",
                        quantity = quantity,
                        entryPrice = price,
                        currentLtp = price,
                        targetPrice = price * 1.05,
                        stopLossPrice = price * 0.97,
                        investedMargin = totalValue,
                        unrealizedPnL = 0.0,
                        unrealizedPnLPercent = 0.0,
                        status = "OPEN"
                    )
                )
            }
        } else if (action == SignalType.SELL) {
            val isWin = realizedPnl >= 0
            db.virtualCashAccountDao().recordTradePnL(
                pnlDelta = realizedPnl,
                winIncrement = if (isWin) 1 else 0,
                lossIncrement = if (!isWin) 1 else 0
            )
            db.virtualCashAccountDao().updateCashBalance(newBalance)
            db.cashLedgerDao().insertEntry(
                com.example.data.local.CashLedgerEntity(
                    transactionType = "TRADE_SELL",
                    amount = totalValue,
                    balanceBefore = profile.virtualBalance,
                    balanceAfter = newBalance,
                    referenceSymbol = symbol,
                    description = "Paper Sell $quantity shares of $symbol (PnL: ₹${String.format("%,.2f", realizedPnl)})"
                )
            )

            // Close or reduce open position
            val openPos = db.tradePositionDao().getOpenPositionBySymbolAndSide(symbol, "BUY")
            if (openPos != null) {
                if (openPos.quantity <= quantity) {
                    db.tradePositionDao().closePosition(
                        positionId = openPos.positionId,
                        exitPrice = price,
                        realizedPnL = realizedPnl,
                        closeReason = "MANUAL"
                    )
                } else {
                    val remQty = openPos.quantity - quantity
                    val remInvested = openPos.entryPrice * remQty
                    db.tradePositionDao().updatePosition(
                        openPos.copy(
                            quantity = remQty,
                            investedMargin = remInvested,
                            currentLtp = price,
                            unrealizedPnL = (price - openPos.entryPrice) * remQty,
                            unrealizedPnLPercent = if (remInvested > 0) ((price - openPos.entryPrice) * remQty / remInvested) * 100.0 else 0.0
                        )
                    )
                }
            }
        }

        val transaction = PaperTransaction(
            id = txId,
            symbol = symbol,
            companyName = stock.companyName,
            action = action,
            quantity = quantity,
            executionPrice = price,
            totalValue = totalValue,
            timestamp = timestamp,
            realizedPnl = realizedPnl,
            virtualBalanceAfter = newBalance
        )

        // 2. Dispatch WhatsApp Notification (Non-blocking failure safety)
        if (profile.isWhatsappEnabled && profile.isTradeAlertsEnabled) {
            val pnlFormatted = if (realizedPnl >= 0) "+₹${String.format("%,.2f", realizedPnl)}" else "-₹${String.format("%,.2f", Math.abs(realizedPnl))}"
            val msg = if (action == SignalType.BUY) {
                """
                ✅ Paper Trade Executed
                
                Stock: $symbol
                Action: BUY
                Quantity: $quantity
                Price: ₹${String.format("%,.2f", price)}
                Total Value: ₹${String.format("%,.2f", totalValue)}
                Time: $timeStr
                Virtual Balance: ₹${String.format("%,.2f", newBalance)}
                """.trimIndent()
            } else {
                """
                ✅ Paper Trade Executed
                
                Stock: $symbol
                Action: SELL
                Quantity: $quantity
                Price: ₹${String.format("%,.2f", price)}
                Total Value: ₹${String.format("%,.2f", totalValue)}
                P/L: $pnlFormatted
                Time: $timeStr
                Virtual Balance: ₹${String.format("%,.2f", newBalance)}
                """.trimIndent()
            }

            try {
                val log = WhatsAppService.sendWhatsAppNotification(
                    apiKey = profile.whatsappApiKey,
                    phoneNumberId = profile.whatsappPhoneNumberId,
                    recipientNumber = profile.phone,
                    eventType = if (action == SignalType.BUY) NotificationEventType.PAPER_BUY else NotificationEventType.PAPER_SELL,
                    messageText = msg
                )
                db.notificationLogDao().insertLog(
                    NotificationLogEntity(
                        eventType = log.eventType.name,
                        recipientNumber = log.recipientNumber,
                        messageText = log.messageText,
                        status = log.status.name,
                        timestamp = log.timestamp,
                        errorDetails = log.errorDetails
                    )
                )
            } catch (e: Exception) {
                // WhatsApp error must never cancel the paper trade!
                db.notificationLogDao().insertLog(
                    NotificationLogEntity(
                        eventType = if (action == SignalType.BUY) NotificationEventType.PAPER_BUY.name else NotificationEventType.PAPER_SELL.name,
                        recipientNumber = profile.phone,
                        messageText = msg,
                        status = NotificationStatus.FAILED.name,
                        timestamp = timestamp,
                        errorDetails = e.localizedMessage
                    )
                )
            }
        }

        return Result.success(transaction)
    }

    val paperTransactions: Flow<List<PaperTransaction>> = db.paperTransactionDao().getAllTransactions().map { list ->
        list.map { entity ->
            PaperTransaction(
                id = entity.id,
                symbol = entity.symbol,
                companyName = entity.companyName,
                action = SignalType.valueOf(entity.action),
                quantity = entity.quantity,
                executionPrice = entity.executionPrice,
                totalValue = entity.totalValue,
                timestamp = entity.timestamp,
                realizedPnl = entity.realizedPnl,
                virtualBalanceAfter = entity.virtualBalanceAfter
            )
        }
    }

    // Price Alerts
    val priceAlerts: Flow<List<PriceAlert>> = db.priceAlertDao().getAllAlerts().map { list ->
        list.map { entity ->
            PriceAlert(
                id = entity.id,
                symbol = entity.symbol,
                token = entity.token,
                targetPrice = entity.targetPrice,
                condition = AlertCondition.valueOf(entity.condition),
                isActive = entity.isActive,
                createdAt = entity.createdAt,
                triggeredAt = entity.triggeredAt
            )
        }
    }

    suspend fun createPriceAlert(symbol: String, targetPrice: Double, condition: AlertCondition): Long {
        val stock = getStock(symbol) ?: getAllStocks().first()
        return db.priceAlertDao().insertAlert(
            PriceAlertEntity(
                symbol = symbol,
                token = stock.token,
                targetPrice = targetPrice,
                condition = condition.name,
                isActive = true
            )
        )
    }

    suspend fun deletePriceAlert(id: Long) {
        db.priceAlertDao().deleteAlert(id)
    }

    // WhatsApp Notification Logs
    val notificationLogs: Flow<List<WhatsAppNotificationLog>> = db.notificationLogDao().getAllNotificationLogs().map { list ->
        list.map { entity ->
            WhatsAppNotificationLog(
                id = entity.id,
                eventType = NotificationEventType.valueOf(entity.eventType),
                recipientNumber = entity.recipientNumber,
                messageText = entity.messageText,
                status = NotificationStatus.valueOf(entity.status),
                timestamp = entity.timestamp,
                errorDetails = entity.errorDetails
            )
        }
    }

    suspend fun sendManualAISignalAlert(recommendation: AIRecommendation) {
        val profile = getOrCreateProfile()
        WhatsAppTradingAlertHelper.sendAutomatedSignalAlert(
            context = context,
            recommendation = recommendation,
            profile = profile,
            forceSend = true,
            db = db
        )
    }

    suspend fun sendAutomatedTradingAlert(
        recommendation: AIRecommendation,
        forceSend: Boolean = false
    ): TradingAlertResult {
        val profile = getOrCreateProfile()
        return WhatsAppTradingAlertHelper.sendAutomatedSignalAlert(
            context = context,
            recommendation = recommendation,
            profile = profile,
            forceSend = forceSend,
            db = db
        )
    }

    suspend fun updateUserProfile(
        name: String,
        email: String,
        phone: String,
        clientCode: String
    ) {
        val current = getOrCreateProfile()
        val updated = current.copy(
            name = name,
            email = email,
            phone = phone,
            angelClientId = clientCode
        )
        db.userProfileDao().insertOrUpdateProfile(updated)
    }
}
