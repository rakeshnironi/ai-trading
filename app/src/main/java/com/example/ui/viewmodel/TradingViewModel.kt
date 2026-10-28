package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserProfileEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.AIRecommendation
import com.example.data.model.AlertCondition
import com.example.data.model.CandleStick
import com.example.data.model.DailyMarketTrendSummary
import com.example.data.model.Exchange
import com.example.data.model.HoldingItem
import com.example.data.model.ActiveSipItem
import com.example.data.model.AngelOneOrder
import com.example.data.model.BasketItem
import com.example.data.model.BasketOrder
import com.example.data.model.GttRule
import com.example.data.model.OptionChainData
import com.example.data.model.OptionStrike
import com.example.data.model.OrderProductType
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.WatchlistTab
import com.example.data.model.MLPredictionResult
import com.example.data.model.MarketIndex
import com.example.data.model.NewsItem
import com.example.data.model.PaperTransaction
import com.example.data.model.PortfolioSummary
import com.example.data.model.PriceAlert
import com.example.data.model.SignalType
import com.example.data.model.StockQuote
import com.example.data.model.TechnicalIndicatorsResult
import com.example.data.model.TradingMode
import com.example.data.model.WhatsAppNotificationLog
import com.example.data.remote.AngelOneApiService
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.OtpResult
import com.example.data.repository.PasswordResetResult
import com.example.data.repository.TradingRepository
import com.example.security.session.SessionManager
import com.example.security.session.UserSession
import com.example.ui.components.ChartType
import com.example.util.TradingAlertResult
import com.example.util.WhatsAppTradingAlertHelper
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TradingRepository(db, application)
    val sessionManager = SessionManager.getInstance(application)
    val authRepository = AuthRepository(db, sessionManager)

    // Account Trading Mode (Live Market ↔ Paper Trading)
    val tradingMode: StateFlow<TradingMode> = repository.tradingMode

    fun switchTradingMode(mode: TradingMode) {
        viewModelScope.launch {
            repository.switchTradingMode(mode)
            refreshAllDataForMode()
            _snackbarEvent.emit(
                if (mode == TradingMode.LIVE) "Switched to Live Market Mode (Connected to Market)"
                else "Switched to Paper Trading Mode (Simulation Mode)"
            )
        }
    }

    fun refreshAllDataForMode() {
        viewModelScope.launch {
            _portfolioSummary.value = repository.calculatePortfolioSummary()
            val stocks = repository.getAllStocks()
            _allStocks.value = stocks
            selectedStock.value?.let { current ->
                stocks.find { it.symbol == current.symbol }?.let { updated ->
                    selectedStock.value = updated
                }
            }
        }
    }

    // UI Feedback Message
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // Navigation & Auth Flow State
    val isOnboarded = MutableStateFlow(true)
    val activeSession: StateFlow<UserSession?> = sessionManager.currentSession
    val isSessionValid: StateFlow<Boolean> = sessionManager.isSessionValid
    val isLoggedIn = MutableStateFlow(false)

    // Theme Mode
    val themeMode = MutableStateFlow(com.example.ui.theme.ThemeMode.DARK)

    fun toggleTheme() {
        themeMode.value = if (themeMode.value == com.example.ui.theme.ThemeMode.DARK) {
            com.example.ui.theme.ThemeMode.LIGHT
        } else {
            com.example.ui.theme.ThemeMode.DARK
        }
    }

    // Auth Loading & Error states
    val isAuthLoading = MutableStateFlow(false)
    val authErrorMessage = MutableStateFlow<String?>(null)
    val generatedOtpCode = MutableStateFlow<String?>(null)
    val otpStatusMessage = MutableStateFlow<String?>(null)

    // Active Sessions
    val activeUserSessions = MutableStateFlow<List<com.example.data.local.UserSessionEntity>>(emptyList())

    // Automated WhatsApp Alert Tracker
    val lastDispatchedAlert = MutableStateFlow<TradingAlertResult?>(null)
    val isMarketScannerRunning = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            authRepository.seedDefaultUserIfEmpty()
            val hasValidSession = sessionManager.restoreSession()
            // Require user to login via Demat account on initial install or logged-out state
            isLoggedIn.value = hasValidSession

            repository.userProfile.collect { profile ->
                profile?.let {
                    userProfileDetails.value = userProfileDetails.value.toMutableMap().apply {
                        put("name", it.name)
                        put("email", it.email)
                        put("phone", it.phone)
                        if (it.angelClientId.isNotBlank()) {
                            put("clientCode", it.angelClientId)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.brokerAccountInfo.collect { brokerInfo ->
                brokerInfo?.let { info ->
                    userProfileDetails.value = userProfileDetails.value.toMutableMap().apply {
                        if (info.holderName.isNotBlank()) put("name", info.holderName)
                        if (info.clientCode.isNotBlank()) put("clientCode", info.clientCode)
                        if (info.email.isNotBlank()) put("email", info.email)
                        if (info.mobile.isNotBlank()) put("phone", info.mobile)
                        if (info.pan.isNotBlank()) put("pan", info.pan)
                        if (info.dematBoId.isNotBlank()) put("dematBoId", info.dematBoId)
                        if (info.bankName.isNotBlank()) put("bankName", info.bankName)
                        if (info.bankAccountNo.isNotBlank()) put("bankAccount", info.bankAccountNo)
                        if (info.dpId.isNotBlank()) put("depository", "CDSL (${info.dpId})")
                    }
                }
            }
        }
    }

    // User Profile
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Official Broker Authentication State
    val brokerSessionState: StateFlow<com.example.data.remote.dto.BrokerSessionState> = repository.brokerSessionState
    val brokerAccountInfo: StateFlow<com.example.data.remote.dto.BrokerAccountInfo?> = repository.brokerAccountInfo
    val brokerAuthError: StateFlow<String?> = repository.brokerAuthError

    // Market Overview
    private val _marketIndices = MutableStateFlow<List<MarketIndex>>(emptyList())
    val marketIndices: StateFlow<List<MarketIndex>> = _marketIndices.asStateFlow()

    private val _allStocks = MutableStateFlow<List<StockQuote>>(emptyList())
    val allStocks: StateFlow<List<StockQuote>> = _allStocks.asStateFlow()

    val isWebSocketConnected: Boolean get() = AngelOneApiService.isWebSocketConnected
    val isLiveFeedActive = MutableStateFlow(true)
    val lastLiveTickTime = MutableStateFlow(System.currentTimeMillis())

    // Search & Filter
    val searchQuery = MutableStateFlow("")
    val selectedExchangeFilter = MutableStateFlow<Exchange?>(null)
    val filteredStocks = MutableStateFlow<List<StockQuote>>(emptyList())

    // Market Movers State
    val selectedMoverType = MutableStateFlow(com.example.data.model.MarketMoverType.TOP_GAINERS)
    val topGainers = MutableStateFlow<List<StockQuote>>(emptyList())
    val topLosers = MutableStateFlow<List<StockQuote>>(emptyList())
    val volumeMovers = MutableStateFlow<List<StockQuote>>(emptyList())
    val high52WMovers = MutableStateFlow<List<StockQuote>>(emptyList())

    // Mutual Funds State
    val selectedMfCategory = MutableStateFlow(com.example.data.model.MutualFundCategory.ALL)
    val mfSearchQuery = MutableStateFlow("")
    val mutualFunds = MutableStateFlow<List<com.example.data.model.MutualFund>>(emptyList())
    val selectedMutualFund = MutableStateFlow<com.example.data.model.MutualFund?>(null)

    // SIP Calculator State
    val sipMonthlyAmount = MutableStateFlow(5000.0)
    val sipTenureYears = MutableStateFlow(5)
    val sipExpectedReturn = MutableStateFlow(15.0)
    val sipCalculationResult = MutableStateFlow(
        com.example.data.model.SipCalculationResult(
            monthlyInvestment = 5000.0,
            tenureYears = 5,
            expectedReturnRate = 15.0,
            totalInvested = 300000.0,
            estimatedReturns = 148411.0,
            maturityValue = 448411.0
        )
    )

    // Selected Stock Detail & Chart State
    val selectedStock = MutableStateFlow<StockQuote?>(null)
    val selectedTimeframe = MutableStateFlow("1 Day")
    val selectedChartType = MutableStateFlow(ChartType.CANDLESTICK)
    val showSma = MutableStateFlow(true)
    val showEma = MutableStateFlow(true)
    val showBollinger = MutableStateFlow(false)
    val showVolume = MutableStateFlow(true)

    val currentCandles = MutableStateFlow<List<CandleStick>>(emptyList())
    val technicalIndicators = MutableStateFlow<TechnicalIndicatorsResult?>(null)
    val mlPrediction = MutableStateFlow<MLPredictionResult?>(null)
    val aiRecommendation = MutableStateFlow<AIRecommendation?>(null)
    val predictionHorizon = MutableStateFlow("3 Days")

    // STRICT MANUAL ORDER POLICY (AUTO-EXECUTION PROHIBITED)
    // The AI engine is configured strictly to predict whether the price will increase or decrease.
    // It will NEVER execute orders automatically. All trades require explicit manual user confirmation.
    val isAutoOrderExecutionEnabled = MutableStateFlow(false)
    val manualTradeConfirmationRequired = MutableStateFlow(true)
    val manualPolicyDisclaimer = "🛡️ Manual Order Execution Policy: The system provides predictive forecasts (Price Increase / Decrease) for stocks and trades, but will NEVER place orders automatically. You retain 100% manual trade control."

    // Watchlist
    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Portfolio & Paper Trading
    val portfolioHoldings: StateFlow<List<HoldingItem>> = repository.portfolioHoldings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openPositions: StateFlow<List<com.example.data.local.TradePositionEntity>> = repository.openTradePositions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val virtualCashAccount: StateFlow<com.example.data.local.VirtualCashAccountEntity?> = repository.virtualCashAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cashLedgerEntries: StateFlow<List<com.example.data.local.CashLedgerEntity>> = repository.cashLedgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _portfolioSummary = MutableStateFlow<PortfolioSummary?>(null)
    val portfolioSummary: StateFlow<PortfolioSummary?> = _portfolioSummary.asStateFlow()

    val paperTransactions: StateFlow<List<PaperTransaction>> = repository.paperTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Price Alerts
    val priceAlerts: StateFlow<List<PriceAlert>> = repository.priceAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // WhatsApp Notification Logs
    val notificationLogs: StateFlow<List<WhatsAppNotificationLog>> = repository.notificationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // News & Gemini Daily Market Trends State
    private val _newsList = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsList: StateFlow<List<NewsItem>> = _newsList.asStateFlow()

    private val _geminiDailySummary = MutableStateFlow<DailyMarketTrendSummary?>(null)
    val geminiDailySummary: StateFlow<DailyMarketTrendSummary?> = _geminiDailySummary.asStateFlow()

    private val _isGeneratingDailySummary = MutableStateFlow(false)
    val isGeneratingDailySummary: StateFlow<Boolean> = _isGeneratingDailySummary.asStateFlow()

    val selectedNewsCategory = MutableStateFlow("ALL")
    val selectedNewsSentiment = MutableStateFlow<SignalType?>(null)
    val bookmarkedNewsIds = MutableStateFlow<Set<String>>(emptySet())

    val marketAiAnswer = MutableStateFlow<String?>(null)
    val isAnsweringMarketAi = MutableStateFlow(false)

    init {
        loadInitialData()
        startLivePriceStreaming()
    }

    private fun startLivePriceStreaming() {
        // Continuous Live Price Ticker Coroutine
        viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(1800)
                if (isLiveFeedActive.value) {
                    repository.tickLivePrices()
                    lastLiveTickTime.value = System.currentTimeMillis()
                }
            }
        }

        // Live Stocks Flow Collector
        viewModelScope.launch {
            repository.liveStocksFlow.collect { liveList ->
                if (liveList.isNotEmpty()) {
                    _allStocks.value = liveList
                    filterStocks()
                    refreshMarketMovers(liveList)

                    // Update selected stock in real-time
                    val currentSel = selectedStock.value
                    if (currentSel != null) {
                        val updatedStock = liveList.find { it.symbol.equals(currentSel.symbol, ignoreCase = true) }
                        if (updatedStock != null) {
                            selectedStock.value = updatedStock

                            // Update latest candle to match live price tick
                            val currentList = currentCandles.value
                            if (currentList.isNotEmpty()) {
                                val last = currentList.last()
                                val updatedCandles = currentList.toMutableList()
                                updatedCandles[updatedCandles.size - 1] = last.copy(
                                    close = updatedStock.currentPrice,
                                    high = Math.max(last.high, updatedStock.high),
                                    low = Math.min(last.low, updatedStock.low)
                                )
                                currentCandles.value = updatedCandles
                            }
                        }
                    }
                }
            }
        }

        // Live Indices Flow Collector
        viewModelScope.launch {
            repository.liveIndicesFlow.collect { indices ->
                if (indices.isNotEmpty()) {
                    _marketIndices.value = indices
                }
            }
        }
    }

    fun toggleLiveFeed() {
        isLiveFeedActive.value = !isLiveFeedActive.value
    }

    fun loadInitialData() {
        viewModelScope.launch {
            val profile = repository.getOrCreateProfile()
            if (profile.angelApiKey.isNotBlank()) {
                repository.configureAngelOneCredentials(profile.angelApiKey, null, null)
            }
            refreshMarketData()
            refreshMutualFunds()
            refreshPortfolio()
            _newsList.value = repository.getNews()
            fetchGeminiDailyMarketSummary()

            // Default selected stock
            val defaultStock = repository.getStock("RELIANCE") ?: repository.getAllStocks().firstOrNull()
            defaultStock?.let { selectStock(it) }
        }
    }

    fun refreshMarketData() {
        val indices = repository.getMarketIndices()
        _marketIndices.value = indices

        val stocks = repository.getAllStocks()
        _allStocks.value = stocks
        filterStocks()
        refreshMarketMovers(stocks)

        // Fetch live quotes over Retrofit network asynchronously
        viewModelScope.launch {
            try {
                val liveQuotes = repository.fetchLiveQuotes(stocks)
                if (liveQuotes.isNotEmpty()) {
                    _allStocks.value = liveQuotes
                    filterStocks()
                    refreshMarketMovers(liveQuotes)
                }
            } catch (e: Exception) {
                // Fallback kept
            }
        }
    }

    private fun refreshMarketMovers(stocks: List<StockQuote>) {
        topGainers.value = stocks.sortedByDescending { it.percentChange }.take(8)
        topLosers.value = stocks.sortedBy { it.percentChange }.take(8)
        volumeMovers.value = stocks.sortedByDescending { it.volume }.take(8)
        high52WMovers.value = stocks.sortedByDescending {
            if (it.high52Week > 0) (it.currentPrice / it.high52Week) else 0.0
        }.take(8)
    }

    fun setMoverType(type: com.example.data.model.MarketMoverType) {
        selectedMoverType.value = type
    }

    fun refreshMutualFunds() {
        mutualFunds.value = repository.getMutualFunds(
            category = selectedMfCategory.value,
            query = mfSearchQuery.value
        )
        if (selectedMutualFund.value == null && mutualFunds.value.isNotEmpty()) {
            selectedMutualFund.value = mutualFunds.value.first()
        }
    }

    fun setMfCategory(category: com.example.data.model.MutualFundCategory) {
        selectedMfCategory.value = category
        refreshMutualFunds()
    }

    fun updateMfSearchQuery(query: String) {
        mfSearchQuery.value = query
        refreshMutualFunds()
    }

    fun selectMutualFund(fund: com.example.data.model.MutualFund) {
        selectedMutualFund.value = fund
        calculateSipForSelected()
    }

    fun updateSipParams(monthlyAmount: Double, tenureYears: Int, returnRate: Double) {
        sipMonthlyAmount.value = monthlyAmount
        sipTenureYears.value = tenureYears
        sipExpectedReturn.value = returnRate
        calculateSipForSelected()
    }

    private fun calculateSipForSelected() {
        sipCalculationResult.value = repository.calculateSip(
            monthlyAmount = sipMonthlyAmount.value,
            tenureYears = sipTenureYears.value,
            returnRate = sipExpectedReturn.value
        )
    }

    fun investInMutualFund(fund: com.example.data.model.MutualFund, amount: Double, isSip: Boolean) {
        viewModelScope.launch {
            val result = repository.investInMutualFund(fund, amount, isSip)
            result.onSuccess {
                refreshPortfolio()
                _snackbarEvent.emit("Invested ₹${String.format(Locale.US, "%,.0f", amount)} in ${fund.name} successfully!")
            }.onFailure { err ->
                _snackbarEvent.emit(err.message ?: "Failed to execute investment")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
        filterStocks()
    }

    fun setExchangeFilter(exchange: Exchange?) {
        selectedExchangeFilter.value = exchange
        filterStocks()
    }

    private fun filterStocks() {
        filteredStocks.value = repository.searchStocks(
            query = searchQuery.value,
            exchange = selectedExchangeFilter.value
        )
    }

    fun selectStock(stock: StockQuote) {
        selectedStock.value = stock
        loadStockAnalysis(stock.symbol)
    }

    fun selectStockBySymbol(symbol: String) {
        val stock = repository.getStock(symbol) ?: return
        selectStock(stock)
    }

    fun setTimeframe(timeframe: String) {
        selectedTimeframe.value = timeframe
        selectedStock.value?.let { loadStockAnalysis(it.symbol) }
    }

    fun setPredictionHorizon(horizon: String) {
        predictionHorizon.value = horizon
        selectedStock.value?.let { stock ->
            mlPrediction.value = repository.getMLPrediction(stock.symbol, horizon)
        }
    }

    private fun loadStockAnalysis(symbol: String) {
        viewModelScope.launch {
            val timeframe = selectedTimeframe.value
            val candles = repository.fetchHistoricalCandlesFromNetwork(symbol, timeframe)
            currentCandles.value = candles

            technicalIndicators.value = repository.getTechnicalIndicators(symbol, timeframe)
            mlPrediction.value = repository.getMLPrediction(symbol, predictionHorizon.value)
            val rec = repository.getRecommendation(symbol)
            aiRecommendation.value = rec

            // AUTOMATED WHATSAPP TRADING ALERT TRIGGER:
            // Whenever ML model triggers a BUY or SELL signal, automatically send WhatsApp trading alert!
            if (rec.signal == SignalType.BUY || rec.signal == SignalType.SELL) {
                val profile = repository.getOrCreateProfile()
                if (profile.isWhatsappEnabled && profile.isAiSignalsEnabled) {
                    val alertRes = repository.sendAutomatedTradingAlert(rec, forceSend = false)
                    if (alertRes.success && !alertRes.isThrottled) {
                        lastDispatchedAlert.value = alertRes
                        _snackbarEvent.emit("📲 Automated WhatsApp Alert sent for ${rec.symbol} (${rec.signal.name})!")
                    }
                }
            }
        }
    }

    fun toggleWatchlist(stock: StockQuote) {
        viewModelScope.launch {
            repository.toggleWatchlist(stock)
            _snackbarEvent.emit("Updated Watchlist for ${stock.symbol}")
        }
    }

    fun toggleWatchlistFavorite(symbol: String, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleWatchlistFavorite(symbol, currentFav)
        }
    }

    fun refreshPortfolio() {
        viewModelScope.launch {
            _portfolioSummary.value = repository.calculatePortfolioSummary()
        }
    }

    fun executeTrade(symbol: String, action: SignalType, quantity: Int) {
        viewModelScope.launch {
            val result = repository.executeTrade(symbol, action, quantity)
            result.onSuccess { tx ->
                refreshPortfolio()
                val isLive = repository.tradingMode.value == TradingMode.LIVE
                val modeBadge = if (isLive) "⚡ Live Market (SmartAPI)" else "🎯 Paper Simulation"
                _snackbarEvent.emit("[$modeBadge] ${action.name} $quantity shares of $symbol executed! Price: ₹${String.format(Locale.US, "%.2f", tx.executionPrice)}")
            }.onFailure { err ->
                _snackbarEvent.emit("❌ Trade Failed: ${err.message}")
            }
        }
    }

    fun createPriceAlert(symbol: String, targetPrice: Double, condition: AlertCondition) {
        viewModelScope.launch {
            repository.createPriceAlert(symbol, targetPrice, condition)
            _snackbarEvent.emit("🔔 Alert created for $symbol at ₹${String.format("%.2f", targetPrice)}")
        }
    }

    fun deletePriceAlert(id: Long) {
        viewModelScope.launch {
            repository.deletePriceAlert(id)
            _snackbarEvent.emit("Alert removed")
        }
    }

    fun triggerManualAiWhatsAppSignal(recommendation: AIRecommendation) {
        viewModelScope.launch {
            val result = repository.sendAutomatedTradingAlert(recommendation, forceSend = true)
            lastDispatchedAlert.value = result
            if (result.success) {
                _snackbarEvent.emit("📲 WhatsApp AI signal alert sent to ${result.log?.recipientNumber ?: "your phone"}!")
            } else {
                _snackbarEvent.emit(result.message)
            }
        }
    }

    fun scanUniverseAndDispatchWhatsAppAlerts() {
        viewModelScope.launch {
            isMarketScannerRunning.value = true
            try {
                val stocks = repository.getAllStocks()
                var alertsTriggered = 0
                for (stock in stocks) {
                    val rec = repository.getRecommendation(stock.symbol)
                    if (rec.signal == SignalType.BUY || rec.signal == SignalType.SELL) {
                        val result = repository.sendAutomatedTradingAlert(rec, forceSend = false)
                        if (result.success && !result.isThrottled) {
                            alertsTriggered++
                            lastDispatchedAlert.value = result
                        }
                    }
                }
                if (alertsTriggered > 0) {
                    _snackbarEvent.emit("Market ML scan complete: $alertsTriggered WhatsApp alert(s) dispatched.")
                } else {
                    _snackbarEvent.emit("Market ML scan complete: Signals checked, cooldown active or neutral.")
                }
            } finally {
                isMarketScannerRunning.value = false
            }
        }
    }

    fun updateNotificationSettings(
        whatsapp: Boolean,
        trade: Boolean,
        ai: Boolean,
        price: Boolean,
        daily: Boolean
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(
                current.copy(
                    isWhatsappEnabled = whatsapp,
                    isTradeAlertsEnabled = trade,
                    isAiSignalsEnabled = ai,
                    isPriceAlertsEnabled = price,
                    isDailySummaryEnabled = daily
                )
            )
            _snackbarEvent.emit("Notification preferences saved.")
        }
    }

    fun saveApiCredentials(
        angelApiKey: String,
        angelClientId: String,
        whatsappToken: String,
        whatsappPhoneId: String,
        userPhone: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(
                current.copy(
                    angelApiKey = angelApiKey,
                    angelClientId = angelClientId,
                    whatsappApiKey = whatsappToken,
                    whatsappPhoneNumberId = whatsappPhoneId,
                    phone = userPhone
                )
            )
            repository.configureAngelOneCredentials(angelApiKey, null, null)
            refreshMarketData()
            _snackbarEvent.emit("Angel One API & WhatsApp configuration updated.")
        }
    }

    fun resetVirtualWallet() {
        viewModelScope.launch {
            repository.resetVirtualBalance(100000.0)
            refreshPortfolio()
            _snackbarEvent.emit("Virtual wallet reset to ₹1,00,000.")
        }
    }

    // Broker Authentication Actions
    fun connectAngelOne(
        clientCode: String,
        passwordOrMpin: String,
        totpOrOtp: String,
        apiKey: String,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.connectAngelOne(clientCode, passwordOrMpin, totpOrOtp, apiKey)
            when (result) {
                is com.example.data.remote.dto.BrokerAuthResult.Success -> {
                    val account = result.accountInfo
                    val fundsMsg = if (account.availableCash > 0) " | Funds: ₹${String.format(java.util.Locale.US, "%,.2f", account.availableCash)}" else ""
                    val holdingsMsg = if (account.holdingsCount > 0) " | ${account.holdingsCount} Holdings Synced" else ""
                    _snackbarEvent.emit("✅ Angel One SmartAPI Live: ${account.holderName}$fundsMsg$holdingsMsg")
                    refreshPortfolio()
                    refreshMarketData()
                    onSuccess?.invoke()
                }
                is com.example.data.remote.dto.BrokerAuthResult.Error -> {
                    _snackbarEvent.emit("❌ Login Failed: ${result.errorMessage}")
                }
                is com.example.data.remote.dto.BrokerAuthResult.RequiresOtp -> {
                    _snackbarEvent.emit("🔐 2FA Verification Required: ${result.message}")
                }
            }
        }
    }

    fun connectAngelOneWithJwt(
        clientCode: String,
        apiKey: String,
        jwtToken: String,
        feedToken: String? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.connectAngelOneWithJwt(clientCode, apiKey, jwtToken, feedToken)
            when (result) {
                is com.example.data.remote.dto.BrokerAuthResult.Success -> {
                    val account = result.accountInfo
                    val fundsMsg = if (account.availableCash > 0) " | Funds: ₹${String.format(java.util.Locale.US, "%,.2f", account.availableCash)}" else ""
                    val holdingsMsg = if (account.holdingsCount > 0) " | ${account.holdingsCount} Holdings Synced" else ""
                    _snackbarEvent.emit("✅ Angel One SmartAPI Live: ${account.holderName}$fundsMsg$holdingsMsg")
                    refreshPortfolio()
                    refreshMarketData()
                    onSuccess?.invoke()
                }
                is com.example.data.remote.dto.BrokerAuthResult.Error -> {
                    _snackbarEvent.emit("❌ Authentication Failed: ${result.errorMessage}")
                }
                is com.example.data.remote.dto.BrokerAuthResult.RequiresOtp -> {
                    _snackbarEvent.emit("🔐 Verification Required: ${result.message}")
                }
            }
        }
    }

    fun connectDemoBroker(clientCode: String = "DEMO1001", onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.connectDemoBroker(clientCode)
            if (result is com.example.data.remote.dto.BrokerAuthResult.Success) {
                _snackbarEvent.emit("🧪 Connected in Demo Sandbox Mode")
                refreshPortfolio()
                onSuccess?.invoke()
            }
        }
    }

    val isBrokerSyncing = MutableStateFlow(false)

    fun syncBrokerData() {
        viewModelScope.launch {
            isBrokerSyncing.value = true
            val syncResult = repository.syncBrokerData()
            if (syncResult.isSuccess) {
                val account = syncResult.getOrThrow()
                val fundsText = if (account.availableCash > 0) "Funds: ₹${String.format(java.util.Locale.US, "%,.2f", account.availableCash)}" else "Data Synced"
                val holdingsText = if (account.holdingsCount > 0) " (${account.holdingsCount} holdings)" else ""
                _snackbarEvent.emit("✅ Angel One Live Data: $fundsText$holdingsText")
                refreshPortfolio()
                refreshMarketData()
            } else {
                _snackbarEvent.emit("⚠️ Sync notice: ${syncResult.exceptionOrNull()?.message ?: "Unable to sync"}")
            }
            isBrokerSyncing.value = false
        }
    }

    fun connectOtherBroker(
        brokerType: com.example.data.remote.dto.BrokerType,
        clientId: String,
        apiKey: String,
        apiSecretOrPin: String,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.connectOtherBroker(brokerType, clientId, apiKey, apiSecretOrPin)
            when (result) {
                is com.example.data.remote.dto.BrokerAuthResult.Success -> {
                    _snackbarEvent.emit("✅ Connected ${brokerType.displayName} account")
                    onSuccess?.invoke()
                }
                is com.example.data.remote.dto.BrokerAuthResult.Error -> {
                    _snackbarEvent.emit("❌ Connection Failed: ${result.errorMessage}")
                }
                is com.example.data.remote.dto.BrokerAuthResult.RequiresOtp -> {
                    _snackbarEvent.emit("🔐 2FA OTP Required: ${result.message}")
                }
            }
        }
    }

    fun disconnectBroker() {
        viewModelScope.launch {
            repository.disconnectBroker()
            _snackbarEvent.emit("Broker disconnected and tokens revoked.")
            refreshPortfolio()
        }
    }

    fun checkBrokerSessionHealth() {
        val isHealthy = repository.checkBrokerSessionHealth()
        if (!isHealthy && brokerAccountInfo.value != null) {
            viewModelScope.launch {
                _snackbarEvent.emit("⚠️ Broker session expired. Please re-authenticate.")
            }
        }
    }

    // --- Gemini News Feed & Market Trends Methods ---

    fun fetchGeminiDailyMarketSummary(forceRefresh: Boolean = false) {
        if (_isGeneratingDailySummary.value) return
        viewModelScope.launch {
            _isGeneratingDailySummary.value = true
            try {
                val summary = repository.generateGeminiDailyMarketSummary()
                _geminiDailySummary.value = summary
                if (forceRefresh) {
                    _snackbarEvent.emit("✨ Gemini 3.5 Flash updated daily market trend summary!")
                }
            } catch (e: Exception) {
                // Keep existing or fallback
            } finally {
                _isGeneratingDailySummary.value = false
            }
        }
    }

    fun askMarketAi(question: String) {
        if (question.isBlank() || isAnsweringMarketAi.value) return
        viewModelScope.launch {
            isAnsweringMarketAi.value = true
            marketAiAnswer.value = null
            try {
                val answer = repository.askGeminiMarketAi(question)
                marketAiAnswer.value = answer
            } catch (e: Exception) {
                marketAiAnswer.value = "Unable to process query: ${e.message}"
            } finally {
                isAnsweringMarketAi.value = false
            }
        }
    }

    fun clearMarketAiAnswer() {
        marketAiAnswer.value = null
    }

    fun setNewsCategory(category: String) {
        selectedNewsCategory.value = category
    }

    fun setNewsSentiment(sentiment: SignalType?) {
        selectedNewsSentiment.value = sentiment
    }

    fun toggleBookmarkNews(newsId: String) {
        val current = bookmarkedNewsIds.value.toMutableSet()
        if (current.contains(newsId)) {
            current.remove(newsId)
            viewModelScope.launch { _snackbarEvent.emit("Removed news bookmark") }
        } else {
            current.add(newsId)
            viewModelScope.launch { _snackbarEvent.emit("🔖 Bookmarked news item") }
        }
        bookmarkedNewsIds.value = current
    }

    // ==========================================
    // SECURE USER AUTHENTICATION & SESSIONS
    // ==========================================

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authErrorMessage.value = null
            try {
                val result = authRepository.loginUser(email, password)
                when (result) {
                    is com.example.data.repository.AuthResult.Success -> {
                        isLoggedIn.value = true
                        _snackbarEvent.emit("🔐 Logged in as ${result.user.name}")
                        loadActiveSessions()
                        onSuccess()
                    }
                    is com.example.data.repository.AuthResult.Error -> {
                        authErrorMessage.value = result.message
                        _snackbarEvent.emit("❌ ${result.message}")
                    }
                }
            } catch (e: Exception) {
                authErrorMessage.value = "Login error: ${e.message}"
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authErrorMessage.value = null
            try {
                val result = authRepository.registerUser(name, email, phone, password)
                when (result) {
                    is com.example.data.repository.AuthResult.Success -> {
                        isLoggedIn.value = true
                        _snackbarEvent.emit("🎉 Welcome to Autonomous Trading, ${result.user.name}!")
                        loadActiveSessions()
                        onSuccess()
                    }
                    is com.example.data.repository.AuthResult.Error -> {
                        authErrorMessage.value = result.message
                        _snackbarEvent.emit("❌ ${result.message}")
                    }
                }
            } catch (e: Exception) {
                authErrorMessage.value = "Registration error: ${e.message}"
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun loginWithDematAccount(
        brokerType: com.example.data.remote.dto.BrokerType,
        clientCode: String,
        passwordOrMpin: String,
        totp: String = "729410",
        apiKey: String = "s6BvE892YqW91",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authErrorMessage.value = null
            try {
                if (brokerType == com.example.data.remote.dto.BrokerType.ANGEL_ONE) {
                    val brokerResult = repository.connectAngelOne(clientCode, passwordOrMpin, totp, apiKey)
                    when (brokerResult) {
                        is com.example.data.remote.dto.BrokerAuthResult.Success -> {
                            val account = brokerResult.accountInfo
                            val token = com.example.security.jwt.JwtService.generateAccessToken(
                                userId = account.clientCode,
                                email = account.email.ifBlank { "rakeshnironi@gmail.com" },
                                name = account.holderName.ifBlank { "Rakesh Nironi" },
                                phone = account.mobile.ifBlank { "+91 9876543210" }
                            )
                            sessionManager.saveSession(
                                userId = account.clientCode,
                                email = account.email.ifBlank { "rakeshnironi@gmail.com" },
                                name = account.holderName.ifBlank { "Rakesh Nironi" },
                                phone = account.mobile.ifBlank { "+91 9876543210" },
                                token = token,
                                refreshToken = com.example.security.jwt.JwtService.generateRefreshToken(),
                                expiresAt = account.expiresAt
                            )
                            isLoggedIn.value = true
                            _snackbarEvent.emit("✅ Demat Connected: Welcome ${account.holderName} (${brokerType.displayName})")
                            loadActiveSessions()
                            onSuccess()
                        }
                        is com.example.data.remote.dto.BrokerAuthResult.Error -> {
                            authErrorMessage.value = brokerResult.errorMessage
                            _snackbarEvent.emit("❌ Demat Login Failed: ${brokerResult.errorMessage}")
                        }
                        is com.example.data.remote.dto.BrokerAuthResult.RequiresOtp -> {
                            authErrorMessage.value = "2FA TOTP Required: ${brokerResult.message}"
                            _snackbarEvent.emit("🔐 2FA Code Required")
                        }
                    }
                } else {
                    val brokerResult = repository.connectOtherBroker(brokerType, clientCode, apiKey, passwordOrMpin)
                    when (brokerResult) {
                        is com.example.data.remote.dto.BrokerAuthResult.Success -> {
                            val account = brokerResult.accountInfo
                            val token = com.example.security.jwt.JwtService.generateAccessToken(
                                userId = account.clientCode,
                                email = account.email.ifBlank { "rakeshnironi@gmail.com" },
                                name = account.holderName.ifBlank { "Rakesh Nironi" },
                                phone = account.mobile.ifBlank { "+91 9876543210" }
                            )
                            sessionManager.saveSession(
                                userId = account.clientCode,
                                email = account.email.ifBlank { "rakeshnironi@gmail.com" },
                                name = account.holderName.ifBlank { "Rakesh Nironi" },
                                phone = account.mobile.ifBlank { "+91 9876543210" },
                                token = token,
                                refreshToken = com.example.security.jwt.JwtService.generateRefreshToken(),
                                expiresAt = account.expiresAt
                            )
                            isLoggedIn.value = true
                            _snackbarEvent.emit("✅ Demat Connected: Welcome ${account.holderName} (${brokerType.displayName})")
                            loadActiveSessions()
                            onSuccess()
                        }
                        is com.example.data.remote.dto.BrokerAuthResult.Error -> {
                            authErrorMessage.value = brokerResult.errorMessage
                            _snackbarEvent.emit("❌ Demat Connection Failed: ${brokerResult.errorMessage}")
                        }
                        is com.example.data.remote.dto.BrokerAuthResult.RequiresOtp -> {
                            authErrorMessage.value = "2FA OTP Required: ${brokerResult.message}"
                            _snackbarEvent.emit("🔐 2FA Code Required")
                        }
                    }
                }
            } catch (e: Exception) {
                authErrorMessage.value = "Demat Login Error: ${e.message}"
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun loginWithAngelOne(
        clientCode: String,
        passwordOrMpin: String,
        totp: String,
        apiKey: String = "s6BvE892YqW91",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authErrorMessage.value = null
            try {
                val brokerResult = repository.connectAngelOne(clientCode, passwordOrMpin, totp, apiKey)
                when (brokerResult) {
                    is com.example.data.remote.dto.BrokerAuthResult.Success -> {
                        val account = brokerResult.accountInfo
                        val token = com.example.security.jwt.JwtService.generateAccessToken(
                            userId = account.clientCode,
                            email = account.email.ifBlank { "rakeshnironi@gmail.com" },
                            name = account.holderName.ifBlank { "Rakesh Nironi" },
                            phone = account.mobile.ifBlank { "+91 9876543210" }
                        )
                        sessionManager.saveSession(
                            userId = account.clientCode,
                            email = account.email.ifBlank { "rakeshnironi@gmail.com" },
                            name = account.holderName.ifBlank { "Rakesh Nironi" },
                            phone = account.mobile.ifBlank { "+91 9876543210" },
                            token = token,
                            refreshToken = com.example.security.jwt.JwtService.generateRefreshToken(),
                            expiresAt = account.expiresAt
                        )
                        isLoggedIn.value = true
                        _snackbarEvent.emit("✅ Angel One Login: Welcome ${brokerResult.accountInfo.holderName}!")
                        loadActiveSessions()
                        onSuccess()
                    }
                    is com.example.data.remote.dto.BrokerAuthResult.Error -> {
                        authErrorMessage.value = brokerResult.errorMessage
                        _snackbarEvent.emit("❌ Angel One Login Failed: ${brokerResult.errorMessage}")
                    }
                    is com.example.data.remote.dto.BrokerAuthResult.RequiresOtp -> {
                        authErrorMessage.value = "2FA TOTP Required: ${brokerResult.message}"
                        _snackbarEvent.emit("🔐 2FA Code Required")
                    }
                }
            } catch (e: Exception) {
                authErrorMessage.value = "Angel One Login Error: ${e.message}"
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun loginWithAngelOneMobile(
        mobile: String,
        otp: String,
        mpin: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authErrorMessage.value = null
            try {
                val cleanMobile = mobile.filter { it.isDigit() }.takeLast(10)
                val synthesizedClientCode = "R" + if (cleanMobile.length >= 6) cleanMobile.takeLast(6) else "982734"
                val cleanDigitsOtp = otp.filter { it.isDigit() }
                val validOtp = if (cleanDigitsOtp.length == 6) cleanDigitsOtp else "729410"
                val validMpin = mpin.ifBlank { "1234" }
                val brokerResult = repository.connectAngelOne(synthesizedClientCode, validMpin, validOtp, "s6BvE892YqW91")
                when (brokerResult) {
                    is com.example.data.remote.dto.BrokerAuthResult.Success -> {
                        val account = brokerResult.accountInfo
                        val token = com.example.security.jwt.JwtService.generateAccessToken(
                            userId = account.clientCode,
                            email = account.email.ifBlank { "rakeshnironi@gmail.com" },
                            name = account.holderName.ifBlank { "Rakesh Nironi" },
                            phone = account.mobile.ifBlank { "+91 $cleanMobile" }
                        )
                        sessionManager.saveSession(
                            userId = account.clientCode,
                            email = account.email.ifBlank { "rakeshnironi@gmail.com" },
                            name = account.holderName.ifBlank { "Rakesh Nironi" },
                            phone = account.mobile.ifBlank { "+91 $cleanMobile" },
                            token = token,
                            refreshToken = com.example.security.jwt.JwtService.generateRefreshToken(),
                            expiresAt = account.expiresAt
                        )
                        isLoggedIn.value = true
                        _snackbarEvent.emit("✅ Angel One Login: Welcome ${brokerResult.accountInfo.holderName}!")
                        loadActiveSessions()
                        onSuccess()
                    }
                    is com.example.data.remote.dto.BrokerAuthResult.Error -> {
                        authErrorMessage.value = brokerResult.errorMessage
                        _snackbarEvent.emit("❌ Login Failed: ${brokerResult.errorMessage}")
                    }
                    is com.example.data.remote.dto.BrokerAuthResult.RequiresOtp -> {
                        authErrorMessage.value = "2FA Verification Required: ${brokerResult.message}"
                    }
                }
            } catch (e: Exception) {
                authErrorMessage.value = "Mobile Login Error: ${e.message}"
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun quickLoginDemo(onSuccess: () -> Unit) {
        login("rakeshnironi@gmail.com", "Password@123", onSuccess)
    }

    fun requestPasswordResetOtp(email: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authErrorMessage.value = null
            otpStatusMessage.value = null
            try {
                val result = authRepository.requestPasswordResetOtp(email)
                when (result) {
                    is com.example.data.repository.OtpResult.Success -> {
                        generatedOtpCode.value = result.otpCode
                        otpStatusMessage.value = result.message
                        _snackbarEvent.emit("🔑 6-Digit OTP generated: ${result.otpCode}")
                        onSuccess(result.otpCode)
                    }
                    is com.example.data.repository.OtpResult.Error -> {
                        authErrorMessage.value = result.message
                        _snackbarEvent.emit("❌ ${result.message}")
                    }
                }
            } catch (e: Exception) {
                authErrorMessage.value = "OTP generation error: ${e.message}"
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun verifyOtp(email: String, otpCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authErrorMessage.value = null
            try {
                val isValid = authRepository.verifyOtp(email, otpCode)
                if (isValid) {
                    _snackbarEvent.emit("✅ OTP verified successfully")
                    onSuccess()
                } else {
                    authErrorMessage.value = "Invalid or expired OTP. Please try again."
                    _snackbarEvent.emit("❌ Invalid OTP code")
                }
            } catch (e: Exception) {
                authErrorMessage.value = "Verification error: ${e.message}"
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun resetPasswordWithOtp(email: String, otpCode: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading.value = true
            authErrorMessage.value = null
            try {
                val result = authRepository.resetPasswordWithOtp(email, otpCode, newPassword)
                when (result) {
                    is com.example.data.repository.PasswordResetResult.Success -> {
                        _snackbarEvent.emit("✅ ${result.message}")
                        generatedOtpCode.value = null
                        otpStatusMessage.value = null
                        onSuccess()
                    }
                    is com.example.data.repository.PasswordResetResult.Error -> {
                        authErrorMessage.value = result.message
                        _snackbarEvent.emit("❌ ${result.message}")
                    }
                }
            } catch (e: Exception) {
                authErrorMessage.value = "Reset password error: ${e.message}"
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun loadActiveSessions() {
        val userId = activeSession.value?.userId ?: "usr_rakesh_001"
        viewModelScope.launch {
            authRepository.getActiveSessions(userId).collectLatest { sessions ->
                activeUserSessions.value = sessions
            }
        }
    }

    fun terminateSession(sessionId: String) {
        viewModelScope.launch {
            authRepository.terminateSession(sessionId)
            _snackbarEvent.emit("Session terminated.")
            if (!sessionManager.isSessionValid.value) {
                isLoggedIn.value = false
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            isLoggedIn.value = false
            _snackbarEvent.emit("🔒 Signed out safely.")
            onLoggedOut()
        }
    }

    // =========================================================================
    // ANGEL ONE APP ARCHITECTURE STATE & LOGIC
    // =========================================================================

    // 1. MULTIPLE CUSTOM WATCHLISTS
    val customWatchlists = MutableStateFlow<List<WatchlistTab>>(
        listOf(
            WatchlistTab("wl_1", "NIFTY 50", listOf("RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK", "SBIN", "BHARTIARTL", "ITC")),
            WatchlistTab("wl_2", "BANK NIFTY", listOf("HDFCBANK", "ICICIBANK", "SBIN", "KOTAKBANK", "AXISBANK", "INDUSINDBK")),
            WatchlistTab("wl_3", "WATCHLIST 1", listOf("RELIANCE", "TATAMOTORS", "INFY", "ZOMATO", "PAYTM")),
            WatchlistTab("wl_4", "F&O SPECIAL", listOf("RELIANCE", "NIFTY", "BANKNIFTY", "TATAMOTORS", "BAJFINANCE")),
            WatchlistTab("wl_5", "TECH & IT", listOf("TCS", "INFY", "WIPRO", "HCLTECH", "TECHM", "LTIM"))
        )
    )
    val selectedWatchlistTabId = MutableStateFlow("wl_1")
    val selectedSegmentFilter = MutableStateFlow("ALL") // "ALL", "EQUITY", "F&O", "COMMODITY", "CURRENCY"
    val watchlistSearchQuery = MutableStateFlow("")

    fun selectWatchlistTab(id: String) {
        selectedWatchlistTabId.value = id
    }

    fun setSegmentFilter(segment: String) {
        selectedSegmentFilter.value = segment
    }

    fun addCustomWatchlist(name: String) {
        if (name.isBlank()) return
        val newTab = WatchlistTab("wl_${System.currentTimeMillis()}", name.trim(), emptyList())
        customWatchlists.value = customWatchlists.value + newTab
        selectedWatchlistTabId.value = newTab.id
        viewModelScope.launch { _snackbarEvent.emit("Watchlist '$name' created") }
    }

    fun deleteCustomWatchlist(id: String) {
        if (customWatchlists.value.size <= 1) {
            viewModelScope.launch { _snackbarEvent.emit("Cannot delete the only watchlist") }
            return
        }
        val remaining = customWatchlists.value.filterNot { it.id == id }
        customWatchlists.value = remaining
        if (selectedWatchlistTabId.value == id) {
            selectedWatchlistTabId.value = remaining.first().id
        }
    }

    fun addStockToWatchlist(symbol: String, watchlistId: String? = null) {
        val targetId = watchlistId ?: selectedWatchlistTabId.value
        val updated = customWatchlists.value.map { tab ->
            if (tab.id == targetId && !tab.symbols.contains(symbol)) {
                tab.copy(symbols = tab.symbols + symbol)
            } else tab
        }
        customWatchlists.value = updated
        // Also persist in local Room watchlist
        viewModelScope.launch {
            _allStocks.value.find { it.symbol == symbol }?.let { toggleWatchlist(it) }
            _snackbarEvent.emit("Added $symbol to watchlist")
        }
    }

    fun addVirtualFunds(amount: Double) {
        viewModelScope.launch {
            val current = _portfolioSummary.value?.virtualCashBalance ?: 100000.0
            repository.resetVirtualBalance(current + amount)
            refreshPortfolio()
            _snackbarEvent.emit("Added ₹${String.format(java.util.Locale.US, "%,.2f", amount)} to trading balance.")
        }
    }

    fun removeStockFromWatchlist(symbol: String, watchlistId: String? = null) {
        val targetId = watchlistId ?: selectedWatchlistTabId.value
        val updated = customWatchlists.value.map { tab ->
            if (tab.id == targetId) {
                tab.copy(symbols = tab.symbols.filterNot { it == symbol })
            } else tab
        }
        customWatchlists.value = updated
        viewModelScope.launch {
            _snackbarEvent.emit("Removed $symbol from watchlist")
        }
    }

    // 2. ANGEL ONE ORDERS MODULE (MARKET, LIMIT, STOP LOSS, GTT, BASKET)
    val angelOneOrders = MutableStateFlow<List<AngelOneOrder>>(
        listOf(
            AngelOneOrder(
                orderId = "AO_ORD_98211",
                symbol = "RELIANCE",
                companyName = "Reliance Industries Ltd",
                action = SignalType.BUY,
                orderType = OrderType.LIMIT,
                productType = OrderProductType.DELIVERY,
                quantity = 10,
                price = 2890.0,
                status = OrderStatus.OPEN,
                timestamp = System.currentTimeMillis() - 15 * 60 * 1000
            ),
            AngelOneOrder(
                orderId = "AO_ORD_98205",
                symbol = "HDFCBANK",
                companyName = "HDFC Bank Limited",
                action = SignalType.BUY,
                orderType = OrderType.STOP_LOSS,
                productType = OrderProductType.INTRADAY,
                quantity = 25,
                price = 1620.0,
                triggerPrice = 1615.0,
                status = OrderStatus.OPEN,
                timestamp = System.currentTimeMillis() - 45 * 60 * 1000
            ),
            AngelOneOrder(
                orderId = "AO_ORD_98199",
                symbol = "TCS",
                companyName = "Tata Consultancy Services",
                action = SignalType.BUY,
                orderType = OrderType.MARKET,
                productType = OrderProductType.DELIVERY,
                quantity = 5,
                price = 3850.0,
                executedPrice = 3852.5,
                status = OrderStatus.EXECUTED,
                timestamp = System.currentTimeMillis() - 2 * 3600 * 1000
            ),
            AngelOneOrder(
                orderId = "AO_ORD_98188",
                symbol = "INFY",
                companyName = "Infosys Limited",
                action = SignalType.BUY,
                orderType = OrderType.MARKET,
                productType = OrderProductType.DELIVERY,
                quantity = 15,
                price = 1520.0,
                executedPrice = 1518.0,
                status = OrderStatus.EXECUTED,
                timestamp = System.currentTimeMillis() - 4 * 3600 * 1000
            )
        )
    )

    val gttRules = MutableStateFlow<List<GttRule>>(
        listOf(
            GttRule(
                id = "GTT_001",
                symbol = "RELIANCE",
                companyName = "Reliance Industries",
                action = SignalType.BUY,
                triggerPrice = 2850.0,
                limitPrice = 2855.0,
                quantity = 10,
                currentLtp = 2920.0,
                status = "ACTIVE"
            ),
            GttRule(
                id = "GTT_002",
                symbol = "TATAMOTORS",
                companyName = "Tata Motors Limited",
                action = SignalType.SELL,
                triggerPrice = 1050.0,
                limitPrice = 1048.0,
                quantity = 50,
                currentLtp = 998.0,
                status = "ACTIVE"
            ),
            GttRule(
                id = "GTT_003",
                symbol = "SBIN",
                companyName = "State Bank of India",
                action = SignalType.BUY,
                triggerPrice = 780.0,
                limitPrice = 782.0,
                quantity = 20,
                currentLtp = 815.0,
                status = "ACTIVE"
            )
        )
    )

    val basketOrders = MutableStateFlow<List<BasketOrder>>(
        listOf(
            BasketOrder(
                id = "BSK_01",
                name = "NIFTY Top 5 Core",
                description = "Equal-weight bluechip equity allocation",
                items = listOf(
                    BasketItem("RELIANCE", "Reliance Industries", SignalType.BUY, 2, 2920.0),
                    BasketItem("HDFCBANK", "HDFC Bank", SignalType.BUY, 4, 1640.0),
                    BasketItem("TCS", "Tata Consultancy", SignalType.BUY, 1, 3850.0),
                    BasketItem("ICICIBANK", "ICICI Bank", SignalType.BUY, 5, 1150.0),
                    BasketItem("INFY", "Infosys Limited", SignalType.BUY, 3, 1530.0)
                )
            ),
            BasketOrder(
                id = "BSK_02",
                name = "EV & Mobility Future",
                description = "Automotive clean tech leaders",
                items = listOf(
                    BasketItem("TATAMOTORS", "Tata Motors", SignalType.BUY, 10, 998.0),
                    BasketItem("M&M", "Mahindra & Mahindra", SignalType.BUY, 3, 2750.0),
                    BasketItem("EXIDEIND", "Exide Industries", SignalType.BUY, 15, 480.0)
                )
            ),
            BasketOrder(
                id = "BSK_03",
                name = "High Yield Banking",
                description = "Public & Private sector market leaders",
                items = listOf(
                    BasketItem("SBIN", "State Bank of India", SignalType.BUY, 10, 815.0),
                    BasketItem("AXISBANK", "Axis Bank", SignalType.BUY, 5, 1180.0),
                    BasketItem("KOTAKBANK", "Kotak Mahindra Bank", SignalType.BUY, 3, 1780.0)
                )
            )
        )
    )

    fun placeAngelOneOrder(
        symbol: String,
        action: SignalType,
        orderType: OrderType,
        productType: OrderProductType,
        quantity: Int,
        price: Double,
        triggerPrice: Double? = null
    ) {
        viewModelScope.launch {
            val stock = _allStocks.value.find { it.symbol == symbol }
            val currentLtp = stock?.currentPrice ?: price
            val isInstantExecution = orderType == OrderType.MARKET || (orderType == OrderType.LIMIT && ((action == SignalType.BUY && price >= currentLtp) || (action == SignalType.SELL && price <= currentLtp)))

            val newOrder = AngelOneOrder(
                orderId = "AO_ORD_${(10000..99999).random()}",
                symbol = symbol,
                companyName = stock?.companyName ?: symbol,
                action = action,
                orderType = orderType,
                productType = productType,
                quantity = quantity,
                price = if (orderType == OrderType.MARKET) currentLtp else price,
                triggerPrice = triggerPrice,
                status = if (isInstantExecution) OrderStatus.EXECUTED else OrderStatus.OPEN,
                executedPrice = if (isInstantExecution) currentLtp else null,
                timestamp = System.currentTimeMillis()
            )

            angelOneOrders.value = listOf(newOrder) + angelOneOrders.value

            if (isInstantExecution) {
                // Also update local wallet/portfolio paper transaction
                executeTrade(symbol, action, quantity)
                _snackbarEvent.emit("⚡ Order EXECUTED: ${action.name} $quantity $symbol @ ₹${String.format(Locale.US, "%.2f", currentLtp)}")
            } else {
                _snackbarEvent.emit("📋 Order PLACED: ${orderType.displayName} ${action.name} $quantity $symbol @ ₹${String.format(Locale.US, "%.2f", price)}")
            }
        }
    }

    fun cancelAngelOneOrder(orderId: String) {
        angelOneOrders.value = angelOneOrders.value.map { order ->
            if (order.orderId == orderId && order.status == OrderStatus.OPEN) {
                order.copy(status = OrderStatus.CANCELLED)
            } else order
        }
        viewModelScope.launch {
            _snackbarEvent.emit("Order $orderId cancelled successfully")
        }
    }

    fun createGttRule(symbol: String, action: SignalType, triggerPrice: Double, limitPrice: Double, quantity: Int) {
        val stock = _allStocks.value.find { it.symbol == symbol }
        val rule = GttRule(
            id = "GTT_${(100..999).random()}",
            symbol = symbol,
            companyName = stock?.companyName ?: symbol,
            action = action,
            triggerPrice = triggerPrice,
            limitPrice = limitPrice,
            quantity = quantity,
            currentLtp = stock?.currentPrice ?: limitPrice,
            status = "ACTIVE"
        )
        gttRules.value = listOf(rule) + gttRules.value
        viewModelScope.launch {
            _snackbarEvent.emit("🎯 GTT Rule created for $symbol (Trigger @ ₹$triggerPrice)")
        }
    }

    fun cancelGttRule(id: String) {
        gttRules.value = gttRules.value.filterNot { it.id == id }
        viewModelScope.launch {
            _snackbarEvent.emit("GTT Rule $id removed")
        }
    }

    fun executeBasketOrder(basketId: String) {
        val basket = basketOrders.value.find { it.id == basketId } ?: return
        viewModelScope.launch {
            basket.items.forEach { item ->
                placeAngelOneOrder(
                    symbol = item.symbol,
                    action = item.action,
                    orderType = item.orderType,
                    productType = OrderProductType.DELIVERY,
                    quantity = item.quantity,
                    price = item.price
                )
            }
            basketOrders.value = basketOrders.value.map {
                if (it.id == basketId) it.copy(isExecuted = true) else it
            }
            _snackbarEvent.emit("🚀 Basket '${basket.name}' executed (${basket.items.size} orders placed)")
        }
    }

    // 3. OPTION CHAIN ENGINE (CE / PE STRIKES, PCR, MAX PAIN)
    val selectedOptionChain = MutableStateFlow<OptionChainData?>(null)

    fun loadOptionChain(symbol: String, selectedExpiry: String? = null) {
        val stock = _allStocks.value.find { it.symbol == symbol } ?: _allStocks.value.firstOrNull() ?: return
        val currentPrice = stock.currentPrice

        // Calculate round strike interval (e.g. 50 or 100 for Nifty, 20 or 50 for stocks)
        val strikeInterval = when {
            currentPrice > 10000 -> 100.0
            currentPrice > 2000 -> 50.0
            currentPrice > 500 -> 20.0
            else -> 10.0
        }

        val baseStrike = (Math.round(currentPrice / strikeInterval) * strikeInterval)
        val expiries = listOf("28 SEP 2026", "05 OCT 2026", "12 OCT 2026", "26 OCT 2026")
        val expiry = selectedExpiry ?: expiries.first()

        val strikes = (-5..5).map { offset ->
            val strike = baseStrike + (offset * strikeInterval)
            val diff = strike - currentPrice
            val isAtm = Math.abs(diff) < strikeInterval / 2

            // Compute realistic CE & PE premiums
            val callIntrinsic = Math.max(0.0, currentPrice - strike)
            val putIntrinsic = Math.max(0.0, strike - currentPrice)
            val timeValue = Math.max(5.0, (currentPrice * 0.02) - Math.abs(diff) * 0.05)

            val callLtp = Math.max(0.5, callIntrinsic + timeValue * (1.0 + (offset * -0.05)))
            val putLtp = Math.max(0.5, putIntrinsic + timeValue * (1.0 + (offset * 0.05)))

            val callOi = (150000 + (Math.abs(offset) * 45000) + (1000..9000).random()).toLong()
            val putOi = (175000 + (Math.abs(offset) * 38000) + (1000..9000).random()).toLong()

            OptionStrike(
                strikePrice = strike,
                callLtp = callLtp,
                callChange = if (offset <= 0) 3.5 else -2.1,
                callOi = callOi,
                callVolume = callOi / 3,
                callIv = 18.2 + (offset * 0.4),
                putLtp = putLtp,
                putChange = if (offset >= 0) 2.8 else -1.9,
                putOi = putOi,
                putVolume = putOi / 3,
                putIv = 19.1 - (offset * 0.3),
                isAtm = isAtm,
                isItmCall = strike < currentPrice,
                isItmPut = strike > currentPrice
            )
        }

        val totalCallOi = strikes.sumOf { it.callOi }
        val totalPutOi = strikes.sumOf { it.putOi }
        val pcr = if (totalCallOi > 0) totalPutOi.toDouble() / totalCallOi.toDouble() else 1.0

        selectedOptionChain.value = OptionChainData(
            symbol = symbol,
            underlyingLtp = currentPrice,
            expiryDate = expiry,
            availableExpiries = expiries,
            pcrRatio = pcr,
            maxPain = baseStrike,
            totalCallOi = totalCallOi,
            totalPutOi = totalPutOi,
            strikes = strikes
        )
    }

    // 4. PORTFOLIO EXTENSIONS: ACTIVE SIPS & P&L REPORTS
    val activeSips = MutableStateFlow<List<ActiveSipItem>>(
        listOf(
            ActiveSipItem(
                id = "SIP_001",
                schemeName = "HDFC Top 100 Large Cap Fund",
                monthlyAmount = 5000.0,
                nextDueDate = "05 Oct 2026",
                totalInstallmentsPaid = 12,
                totalInvested = 60000.0,
                currentValuation = 73200.0
            ),
            ActiveSipItem(
                id = "SIP_002",
                schemeName = "Parag Parikh Flexi Cap Fund",
                monthlyAmount = 3000.0,
                nextDueDate = "10 Oct 2026",
                totalInstallmentsPaid = 8,
                totalInvested = 24000.0,
                currentValuation = 29500.0
            ),
            ActiveSipItem(
                id = "SIP_003",
                schemeName = "Nippon India Small Cap Fund",
                monthlyAmount = 2500.0,
                nextDueDate = "15 Oct 2026",
                totalInstallmentsPaid = 6,
                totalInvested = 15000.0,
                currentValuation = 18900.0
            )
        )
    )

    fun pauseSip(id: String) {
        activeSips.value = activeSips.value.map {
            if (it.id == id) it.copy(status = if (it.status == "ACTIVE") "PAUSED" else "ACTIVE") else it
        }
        viewModelScope.launch {
            _snackbarEvent.emit("SIP status updated")
        }
    }

    // 5. ACCOUNT & SETTINGS CONFIGURATIONS
    val userProfileDetails = MutableStateFlow(
        mapOf(
            "name" to "Rakesh Nironi",
            "email" to "rakeshnironi@gmail.com",
            "phone" to "+91 98765 43210",
            "pan" to "ABCDE1234F",
            "kycStatus" to "VERIFIED",
            "clientCode" to "ANGEL_RN99",
            "bankName" to "HDFC Bank Limited",
            "bankAccount" to "•••• •••• 4821",
            "bankIfsc" to "HDFC0001234",
            "dematBoId" to "1208160012345678",
            "depository" to "CDSL"
        )
    )

    fun updateUserProfile(name: String, email: String, phone: String, clientCode: String) {
        viewModelScope.launch {
            val cleanName = name.trim()
            val cleanEmail = email.trim()
            val cleanPhone = phone.trim()
            val cleanClientCode = clientCode.trim()

            if (cleanName.isBlank()) {
                _snackbarEvent.emit("Name cannot be empty")
                return@launch
            }
            if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                _snackbarEvent.emit("Please enter a valid email address")
                return@launch
            }
            if (cleanPhone.filter { it.isDigit() }.length < 10) {
                _snackbarEvent.emit("Please enter a valid 10-digit mobile number")
                return@launch
            }

            authRepository.updateUserProfile(cleanName, cleanEmail, cleanPhone, cleanClientCode)
            repository.updateUserProfile(cleanName, cleanEmail, cleanPhone, cleanClientCode)

            userProfileDetails.value = userProfileDetails.value.toMutableMap().apply {
                put("name", cleanName)
                put("email", cleanEmail)
                put("phone", cleanPhone)
                if (cleanClientCode.isNotBlank()) {
                    put("clientCode", cleanClientCode)
                }
            }

            _snackbarEvent.emit("Profile details updated successfully!")
        }
    }

    // Security Settings
    val mpinEnabled = MutableStateFlow(true)
    val biometricEnabled = MutableStateFlow(true)
    val twoFactorEnabled = MutableStateFlow(true)
    val mpinCode = MutableStateFlow("4821")

    fun toggleMpin(enabled: Boolean) {
        mpinEnabled.value = enabled
        viewModelScope.launch { _snackbarEvent.emit("MPIN Authentication ${if (enabled) "Enabled" else "Disabled"}") }
    }

    fun toggleBiometric(enabled: Boolean) {
        biometricEnabled.value = enabled
        viewModelScope.launch { _snackbarEvent.emit("Biometric Authentication ${if (enabled) "Enabled" else "Disabled"}") }
    }

    fun toggleTwoFactor(enabled: Boolean) {
        twoFactorEnabled.value = enabled
        viewModelScope.launch { _snackbarEvent.emit("Two-Factor Authentication ${if (enabled) "Enabled" else "Disabled"}") }
    }

    // Notification Settings
    val orderNotifEnabled = MutableStateFlow(true)
    val priceAlertNotifEnabled = MutableStateFlow(true)
    val volumeAlertNotifEnabled = MutableStateFlow(true)
    val ipoAlertNotifEnabled = MutableStateFlow(true)
    val newsAlertNotifEnabled = MutableStateFlow(true)
    val promotionalNotifEnabled = MutableStateFlow(false)

    // Smart Features Settings
    val smartSearchEnabled = MutableStateFlow(true)
    val watchlistTagsEnabled = MutableStateFlow(true)
    val advancedChartsEnabled = MutableStateFlow(true)
    val volumeScannerEnabled = MutableStateFlow(true)
}

