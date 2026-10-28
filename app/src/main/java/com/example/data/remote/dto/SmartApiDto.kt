package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Standard Angel One SmartAPI generic response wrapper
 */
@JsonClass(generateAdapter = true)
data class SmartApiResponse<T>(
    @Json(name = "status") val status: Boolean = false,
    @Json(name = "message") val message: String? = null,
    @Json(name = "errorcode") val errorcode: String? = null,
    @Json(name = "data") val data: T? = null
)

/**
 * LTP (Last Traded Price) Request & Response DTOs
 */
@JsonClass(generateAdapter = true)
data class SmartApiLtpRequest(
    @Json(name = "exchange") val exchange: String,
    @Json(name = "tradingsymbol") val tradingsymbol: String,
    @Json(name = "symboltoken") val symboltoken: String
)

@JsonClass(generateAdapter = true)
data class SmartApiLtpData(
    @Json(name = "exchange") val exchange: String? = null,
    @Json(name = "tradingsymbol") val tradingsymbol: String? = null,
    @Json(name = "symboltoken") val symboltoken: String? = null,
    @Json(name = "open") val open: Double? = null,
    @Json(name = "high") val high: Double? = null,
    @Json(name = "low") val low: Double? = null,
    @Json(name = "close") val close: Double? = null,
    @Json(name = "ltp") val ltp: Double? = null
)

/**
 * Full Market Quote Request & Response DTOs
 */
@JsonClass(generateAdapter = true)
data class SmartApiQuoteRequest(
    @Json(name = "mode") val mode: String = "FULL", // "FULL", "OHLC", "LTP"
    @Json(name = "exchangeTokens") val exchangeTokens: Map<String, List<String>>
)

@JsonClass(generateAdapter = true)
data class SmartApiQuoteData(
    @Json(name = "fetched") val fetched: List<SmartApiScripQuote>? = null,
    @Json(name = "unadjusted") val unadjusted: List<SmartApiScripQuote>? = null
)

@JsonClass(generateAdapter = true)
data class SmartApiScripQuote(
    @Json(name = "exchange") val exchange: String? = null,
    @Json(name = "tradingSymbol") val tradingSymbol: String? = null,
    @Json(name = "symbolToken") val symbolToken: String? = null,
    @Json(name = "ltp") val ltp: Double? = null,
    @Json(name = "open") val open: Double? = null,
    @Json(name = "high") val high: Double? = null,
    @Json(name = "low") val low: Double? = null,
    @Json(name = "close") val close: Double? = null,
    @Json(name = "change") val change: Double? = null,
    @Json(name = "percentChange") val percentChange: Double? = null,
    @Json(name = "tradeVolume") val tradeVolume: Long? = null,
    @Json(name = "high52") val high52: Double? = null,
    @Json(name = "low52") val low52: Double? = null,
    @Json(name = "depth") val depth: SmartApiMarketDepth? = null
)

@JsonClass(generateAdapter = true)
data class SmartApiMarketDepth(
    @Json(name = "buy") val buy: List<SmartApiDepthItem>? = null,
    @Json(name = "sell") val sell: List<SmartApiDepthItem>? = null
)

@JsonClass(generateAdapter = true)
data class SmartApiDepthItem(
    @Json(name = "price") val price: Double? = null,
    @Json(name = "quantity") val quantity: Long? = null,
    @Json(name = "orders") val orders: Int? = null
)

/**
 * Historical Candle Data Request
 */
@JsonClass(generateAdapter = true)
data class SmartApiCandleRequest(
    @Json(name = "exchange") val exchange: String,
    @Json(name = "symboltoken") val symboltoken: String,
    @Json(name = "interval") val interval: String, // ONE_MINUTE, FIVE_MINUTE, FIFTEEN_MINUTE, ONE_HOUR, ONE_DAY
    @Json(name = "fromdate") val fromdate: String, // "yyyy-MM-dd HH:mm"
    @Json(name = "todate") val todate: String     // "yyyy-MM-dd HH:mm"
)

/**
 * Search Instrument Request & Response
 */
@JsonClass(generateAdapter = true)
data class SmartApiSearchRequest(
    @Json(name = "exchange") val exchange: String,
    @Json(name = "searchscrip") val searchscrip: String
)

@JsonClass(generateAdapter = true)
data class SmartApiSearchScripData(
    @Json(name = "exchange") val exchange: String? = null,
    @Json(name = "tradingsymbol") val tradingsymbol: String? = null,
    @Json(name = "symboltoken") val symboltoken: String? = null,
    @Json(name = "name") val name: String? = null
)

/**
 * SmartAPI User Login Request & Response
 */
@JsonClass(generateAdapter = true)
data class SmartApiLoginRequest(
    @Json(name = "clientcode") val clientcode: String,
    @Json(name = "password") val password: String,
    @Json(name = "totp") val totp: String
)

@JsonClass(generateAdapter = true)
data class SmartApiLoginData(
    @Json(name = "jwtToken") val jwtToken: String? = null,
    @Json(name = "refreshToken") val refreshToken: String? = null,
    @Json(name = "feedToken") val feedToken: String? = null
)

/**
 * SmartAPI User Profile Response
 */
@JsonClass(generateAdapter = true)
data class SmartApiProfileData(
    @Json(name = "clientcode") val clientcode: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "mobileno") val mobileno: String? = null,
    @Json(name = "exchanges") val exchanges: List<String>? = null,
    @Json(name = "products") val products: List<String>? = null,
    @Json(name = "brokerageGroup") val brokerageGroup: String? = null,
    @Json(name = "pan") val pan: String? = null,
    @Json(name = "dematBoId") val dematBoId: String? = null,
    @Json(name = "bankName") val bankName: String? = null,
    @Json(name = "bankAccountNo") val bankAccountNo: String? = null,
    @Json(name = "dpId") val dpId: String? = null
)

/**
 * SmartAPI Token Refresh Request & Response
 */
@JsonClass(generateAdapter = true)
data class SmartApiTokenRequest(
    @Json(name = "refreshToken") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class SmartApiTokenData(
    @Json(name = "jwtToken") val jwtToken: String? = null,
    @Json(name = "refreshToken") val refreshToken: String? = null,
    @Json(name = "feedToken") val feedToken: String? = null
)

/**
 * SmartAPI Logout Request
 */
@JsonClass(generateAdapter = true)
data class SmartApiLogoutRequest(
    @Json(name = "clientcode") val clientcode: String
)

/**
 * SmartAPI RMS Limits / Margin / Available Cash Response
 */
@JsonClass(generateAdapter = true)
data class SmartApiRmsData(
    @Json(name = "net") val net: String? = null,
    @Json(name = "availablecash") val availablecash: String? = null,
    @Json(name = "availableintradaypayin") val availableintradaypayin: String? = null,
    @Json(name = "availablelimitmargin") val availablelimitmargin: String? = null,
    @Json(name = "collateral") val collateral: String? = null,
    @Json(name = "m2munrealized") val m2munrealized: String? = null,
    @Json(name = "m2mrealized") val m2mrealized: String? = null,
    @Json(name = "utiliseddebits") val utiliseddebits: String? = null,
    @Json(name = "utilisedspan") val utilisedspan: String? = null,
    @Json(name = "utilisedoptionpremium") val utilisedoptionpremium: String? = null,
    @Json(name = "utilisedholdingpledgecharge") val utilisedholdingpledgecharge: String? = null,
    @Json(name = "utilisedturnover") val utilisedturnover: String? = null,
    @Json(name = "utilisedexposure") val utilisedexposure: String? = null,
    @Json(name = "utilisedpayout") val utilisedpayout: String? = null
)

/**
 * SmartAPI User Equity Holdings Response DTOs
 */
@JsonClass(generateAdapter = true)
data class SmartApiHoldingsData(
    @Json(name = "totalholdingvalue") val totalholdingvalue: Double? = null,
    @Json(name = "totalinvvalue") val totalinvvalue: Double? = null,
    @Json(name = "totalprofitandloss") val totalprofitandloss: Double? = null,
    @Json(name = "totalpnlpercentage") val totalpnlpercentage: Double? = null,
    @Json(name = "holdings") val holdings: List<SmartApiHoldingItem>? = null
)

@JsonClass(generateAdapter = true)
data class SmartApiHoldingItem(
    @Json(name = "tradingsymbol") val tradingsymbol: String? = null,
    @Json(name = "exchange") val exchange: String? = null,
    @Json(name = "isin") val isin: String? = null,
    @Json(name = "t1quantity") val t1quantity: Long? = null,
    @Json(name = "realisedquantity") val realisedquantity: Long? = null,
    @Json(name = "quantity") val quantity: Long? = null,
    @Json(name = "authorisedquantity") val authorisedquantity: Long? = null,
    @Json(name = "product") val product: String? = null,
    @Json(name = "collateralquantity") val collateralquantity: Long? = null,
    @Json(name = "collateraltype") val collateraltype: String? = null,
    @Json(name = "haircut") val haircut: Double? = null,
    @Json(name = "averageprice") val averageprice: Double? = null,
    @Json(name = "ltp") val ltp: Double? = null,
    @Json(name = "symboltoken") val symboltoken: String? = null,
    @Json(name = "close") val close: Double? = null,
    @Json(name = "profitandloss") val profitandloss: Double? = null,
    @Json(name = "pnlpercentage") val pnlpercentage: Double? = null
)

/**
 * SmartAPI Open and Closed Positions Response DTO
 */
@JsonClass(generateAdapter = true)
data class SmartApiPositionItem(
    @Json(name = "exchange") val exchange: String? = null,
    @Json(name = "symboltoken") val symboltoken: String? = null,
    @Json(name = "symbolname") val symbolname: String? = null,
    @Json(name = "tradingsymbol") val tradingsymbol: String? = null,
    @Json(name = "producttype") val producttype: String? = null,
    @Json(name = "buyqty") val buyqty: String? = null,
    @Json(name = "sellqty") val sellqty: String? = null,
    @Json(name = "netqty") val netqty: String? = null,
    @Json(name = "buyavgprice") val buyavgprice: String? = null,
    @Json(name = "sellavgprice") val sellavgprice: String? = null,
    @Json(name = "netprice") val netprice: String? = null,
    @Json(name = "totalbuyvalue") val totalbuyvalue: String? = null,
    @Json(name = "totalsellvalue") val totalsellvalue: String? = null,
    @Json(name = "cfbuyqty") val cfbuyqty: String? = null,
    @Json(name = "cfsellqty") val cfsellqty: String? = null,
    @Json(name = "openpositionvalue") val openpositionvalue: String? = null,
    @Json(name = "pnl") val pnl: String? = null,
    @Json(name = "realised") val realised: String? = null,
    @Json(name = "unrealised") val unrealised: String? = null,
    @Json(name = "ltp") val ltp: String? = null,
    @Json(name = "close") val close: String? = null
)

/**
 * Broker Connection State
 */
enum class BrokerSessionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    EXPIRED,
    ERROR
}

enum class BrokerType(val displayName: String, val isOfficialApiSupported: Boolean) {
    ANGEL_ONE("Angel One (SmartAPI V2)", true),
    ZERODHA("Zerodha Kite Connect", true),
    GROWW("Groww Open API", false),
    UPSTOX("Upstox API", false),
    SIMULATED_PAPER("Paper Sandbox (Offline)", true)
}

data class BrokerAccountInfo(
    val clientCode: String,
    val brokerName: String,
    val brokerType: BrokerType,
    val holderName: String,
    val email: String,
    val mobile: String,
    val exchanges: List<String> = listOf("NSE", "BSE", "NFO", "MCX"),
    val products: List<String> = listOf("CNC", "MIS", "NRML", "BO", "CO"),
    val brokerageGroup: String = "ACTIVE_TRADING",
    val pan: String = "",
    val dematBoId: String = "",
    val bankName: String = "",
    val bankAccountNo: String = "",
    val dpId: String = "",
    val connectedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L), // 24hr validity
    val feedTokenMasked: String = "••••••••",
    val jwtTokenMasked: String = "Bearer eyJhbG...••••",
    val isLiveSession: Boolean = true,
    val availableCash: Double = 0.0,
    val netMargin: Double = 0.0,
    val totalHoldingValue: Double = 0.0,
    val totalHoldingPnL: Double = 0.0,
    val holdingsCount: Int = 0,
    val positionsCount: Int = 0,
    val liveHoldings: List<SmartApiHoldingItem> = emptyList(),
    val livePositions: List<SmartApiPositionItem> = emptyList(),
    val isDemoSandbox: Boolean = false,
    val lastSyncTime: Long = System.currentTimeMillis()
)

sealed class BrokerAuthResult {
    data class Success(val accountInfo: BrokerAccountInfo) : BrokerAuthResult()
    data class RequiresOtp(val sessionRef: String, val message: String) : BrokerAuthResult()
    data class Error(val errorCode: String?, val errorMessage: String) : BrokerAuthResult()
}
