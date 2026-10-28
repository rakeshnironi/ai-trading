package com.example.data.remote

import android.util.Log
import com.example.data.model.Exchange
import com.example.data.model.StockQuote
import com.example.data.remote.dto.BrokerAccountInfo
import com.example.data.remote.dto.BrokerType
import com.example.data.remote.dto.SmartApiHoldingItem
import com.example.data.remote.dto.SmartApiPositionItem
import com.example.data.remote.dto.SmartApiProfileData
import com.example.data.remote.dto.SmartApiScripQuote
import org.json.JSONArray
import org.json.JSONObject

/**
 * Bulletproof, Lenient JSON Parser for Angel One SmartAPI V2 Responses.
 * Prevents Moshi / Kotlin serialization crashes when Angel One returns:
 * - Numbers as Strings or vice-versa
 * - Arrays instead of Objects (e.g. in getAllHolding vs getHolding)
 * - Boolean vs String status flags ("true", "success", 1)
 */
object SmartApiLenientParser {

    private const val TAG = "SmartApiLenientParser"

    fun isSuccess(json: JSONObject): Boolean {
        val statusBool = json.optBoolean("status", false)
        if (statusBool) return true
        val statusStr = json.optString("status", "").lowercase()
        return statusStr == "true" || statusStr == "success" || statusStr == "1"
    }

    data class LoginResult(
        val isSuccess: Boolean,
        val jwtToken: String?,
        val refreshToken: String?,
        val feedToken: String?,
        val errorMessage: String?,
        val errorCode: String?
    )

    fun parseLoginResponse(body: String): LoginResult {
        return try {
            val json = JSONObject(body)
            val success = isSuccess(json)
            val message = json.optString("message", "")
            val errorCode = json.optString("errorcode", "")

            if (success) {
                val dataObj = json.optJSONObject("data")
                val jwt = dataObj?.optString("jwtToken", "")?.takeIf { it.isNotBlank() }
                val refresh = dataObj?.optString("refreshToken", "")?.takeIf { it.isNotBlank() }
                val feed = dataObj?.optString("feedToken", "")?.takeIf { it.isNotBlank() }
                LoginResult(true, jwt, refresh, feed, null, null)
            } else {
                LoginResult(false, null, null, null, message.ifBlank { "Authentication failed" }, errorCode)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing login response: ${e.message}")
            LoginResult(false, null, null, null, e.localizedMessage ?: "Invalid response from server", "PARSE_ERROR")
        }
    }

    fun parseProfileResponse(body: String): SmartApiProfileData? {
        return try {
            val json = JSONObject(body)
            if (!isSuccess(json)) return null
            val data = json.optJSONObject("data") ?: return null

            val clientCode = data.optString("clientcode", "")
            val name = data.optString("name", "")
            val email = data.optString("email", "")
            val mobile = data.optString("mobileno", "")
            val group = data.optString("brokerageGroup", "")

            val exchangesList = mutableListOf<String>()
            val exchArr = data.optJSONArray("exchanges")
            if (exchArr != null) {
                for (i in 0 until exchArr.length()) {
                    exchangesList.add(exchArr.optString(i))
                }
            } else {
                val exchStr = data.optString("exchanges", "")
                if (exchStr.isNotBlank()) {
                    exchangesList.addAll(exchStr.split(",", ";").map { it.trim() })
                }
            }

            val productsList = mutableListOf<String>()
            val prodArr = data.optJSONArray("products")
            if (prodArr != null) {
                for (i in 0 until prodArr.length()) {
                    productsList.add(prodArr.optString(i))
                }
            } else {
                val prodStr = data.optString("products", "")
                if (prodStr.isNotBlank()) {
                    productsList.addAll(prodStr.split(",", ";").map { it.trim() })
                }
            }

            val pan = data.optString("pan", "").ifBlank { data.optString("pan_no", "") }
            val dematBoId = data.optString("dematBoId", "").ifBlank { 
                data.optString("dp_account_no", "").ifBlank { data.optString("bo_id", "") } 
            }
            val bankName = data.optString("bankName", "").ifBlank { data.optString("bank_name", "") }
            val bankAccount = data.optString("bankAccountNo", "").ifBlank { data.optString("bank_acc_no", "") }
            val dpId = data.optString("dpId", "").ifBlank { data.optString("dp_id", "") }

            SmartApiProfileData(
                clientcode = clientCode,
                name = name,
                email = email,
                mobileno = mobile,
                exchanges = exchangesList.ifEmpty { listOf("NSE", "BSE", "NFO", "MCX") },
                products = productsList.ifEmpty { listOf("CNC", "MIS", "NRML") },
                brokerageGroup = group.ifBlank { "DEFAULT_BROKERAGE" },
                pan = pan.ifBlank { null },
                dematBoId = dematBoId.ifBlank { null },
                bankName = bankName.ifBlank { null },
                bankAccountNo = bankAccount.ifBlank { null },
                dpId = dpId.ifBlank { null }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing profile: ${e.message}")
            null
        }
    }

    data class RmsResult(
        val availableCash: Double,
        val netMargin: Double,
        val collateral: Double
    )

    fun parseRmsResponse(body: String): RmsResult? {
        return try {
            val json = JSONObject(body)
            if (!isSuccess(json)) return null
            val data = json.optJSONObject("data") ?: return null

            fun getSafeDouble(vararg keys: String): Double {
                for (key in keys) {
                    if (data.has(key)) {
                        val strVal = data.optString(key, "")
                        val dblVal = strVal.toDoubleOrNull() ?: data.optDouble(key, Double.NaN)
                        if (!dblVal.isNaN()) return dblVal
                    }
                }
                return 0.0
            }

            val cash = getSafeDouble("availablecash", "net", "availableintradaypayin")
            val margin = getSafeDouble("availablelimitmargin", "net", "availablecash")
            val collateral = getSafeDouble("collateral", "utilisedcollateral")

            RmsResult(
                availableCash = cash,
                netMargin = if (margin > 0) margin else cash,
                collateral = collateral
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing RMS: ${e.message}")
            null
        }
    }

    data class HoldingsResult(
        val holdings: List<SmartApiHoldingItem>,
        val totalHoldingValue: Double,
        val totalInvestmentValue: Double,
        val totalProfitAndLoss: Double,
        val totalPnlPercentage: Double
    )

    fun parseHoldingsResponse(body: String): HoldingsResult? {
        return try {
            val json = JSONObject(body)
            if (!isSuccess(json)) return null

            var totalVal = 0.0
            var totalInv = 0.0
            var totalPnL = 0.0
            var totalPct = 0.0

            var holdingsArray: JSONArray? = null

            // Check if data is directly an array (standard in getHolding)
            val dataArray = json.optJSONArray("data")
            if (dataArray != null) {
                holdingsArray = dataArray
            } else {
                // Check if data is an object (standard in getAllHolding)
                val dataObj = json.optJSONObject("data")
                if (dataObj != null) {
                    totalVal = dataObj.optString("totalholdingvalue", "").toDoubleOrNull()
                        ?: dataObj.optDouble("totalholdingvalue", 0.0)
                    totalInv = dataObj.optString("totalinvvalue", "").toDoubleOrNull()
                        ?: dataObj.optDouble("totalinvvalue", 0.0)
                    totalPnL = dataObj.optString("totalprofitandloss", "").toDoubleOrNull()
                        ?: dataObj.optDouble("totalprofitandloss", 0.0)
                    totalPct = dataObj.optString("totalpnlpercentage", "").toDoubleOrNull()
                        ?: dataObj.optDouble("totalpnlpercentage", 0.0)

                    holdingsArray = dataObj.optJSONArray("holdings")
                }
            }

            val list = mutableListOf<SmartApiHoldingItem>()
            if (holdingsArray != null) {
                for (i in 0 until holdingsArray.length()) {
                    val item = holdingsArray.optJSONObject(i) ?: continue

                    fun itemDouble(vararg keys: String): Double {
                        for (k in keys) {
                            if (item.has(k)) {
                                val s = item.optString(k, "")
                                val d = s.toDoubleOrNull() ?: item.optDouble(k, Double.NaN)
                                if (!d.isNaN()) return d
                            }
                        }
                        return 0.0
                    }

                    fun itemLong(vararg keys: String): Long {
                        for (k in keys) {
                            if (item.has(k)) {
                                val s = item.optString(k, "")
                                val l = s.toLongOrNull() ?: item.optLong(k, -1L)
                                if (l >= 0L) return l
                            }
                        }
                        return 0L
                    }

                    val symbol = item.optString("tradingsymbol", "")
                    val exchange = item.optString("exchange", "NSE")
                    val isin = item.optString("isin", "")
                    val token = item.optString("symboltoken", "")
                    val qty = itemLong("quantity", "realisedquantity", "t1quantity")
                    val avgPrice = itemDouble("averageprice", "avgprice")
                    val ltp = itemDouble("ltp", "close")
                    val close = itemDouble("close", "previousclose")
                    var pnl = itemDouble("profitandloss", "pnl")
                    if (pnl == 0.0 && qty > 0 && ltp > 0 && avgPrice > 0) {
                        pnl = (ltp - avgPrice) * qty
                    }
                    var pnlPct = itemDouble("pnlpercentage", "pnlpct")
                    if (pnlPct == 0.0 && avgPrice > 0 && ltp > 0) {
                        pnlPct = ((ltp - avgPrice) / avgPrice) * 100.0
                    }

                    list.add(
                        SmartApiHoldingItem(
                            tradingsymbol = symbol,
                            exchange = exchange,
                            isin = isin,
                            quantity = qty,
                            realisedquantity = qty,
                            averageprice = avgPrice,
                            ltp = ltp,
                            symboltoken = token,
                            close = close,
                            profitandloss = pnl,
                            pnlpercentage = pnlPct
                        )
                    )
                }
            }

            if (totalVal == 0.0 && list.isNotEmpty()) {
                totalVal = list.sumOf { (it.ltp ?: it.averageprice ?: 0.0) * (it.quantity ?: 0L) }
                totalInv = list.sumOf { (it.averageprice ?: 0.0) * (it.quantity ?: 0L) }
                totalPnL = totalVal - totalInv
                totalPct = if (totalInv > 0) (totalPnL / totalInv) * 100.0 else 0.0
            }

            HoldingsResult(
                holdings = list,
                totalHoldingValue = totalVal,
                totalInvestmentValue = totalInv,
                totalProfitAndLoss = totalPnL,
                totalPnlPercentage = totalPct
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing holdings: ${e.message}")
            null
        }
    }

    fun parsePositionsResponse(body: String): List<SmartApiPositionItem> {
        val result = mutableListOf<SmartApiPositionItem>()
        try {
            val json = JSONObject(body)
            if (!isSuccess(json)) return emptyList()

            val dataArray = json.optJSONArray("data") ?: return emptyList()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.optJSONObject(i) ?: continue

                result.add(
                    SmartApiPositionItem(
                        exchange = item.optString("exchange", "NSE"),
                        symboltoken = item.optString("symboltoken", ""),
                        symbolname = item.optString("symbolname", ""),
                        tradingsymbol = item.optString("tradingsymbol", ""),
                        producttype = item.optString("producttype", "INTRADAY"),
                        buyqty = item.optString("buyqty", "0"),
                        sellqty = item.optString("sellqty", "0"),
                        netqty = item.optString("netqty", "0"),
                        buyavgprice = item.optString("buyavgprice", "0.0"),
                        sellavgprice = item.optString("sellavgprice", "0.0"),
                        netprice = item.optString("netprice", "0.0"),
                        totalbuyvalue = item.optString("totalbuyvalue", "0.0"),
                        totalsellvalue = item.optString("totalsellvalue", "0.0"),
                        cfbuyqty = item.optString("cfbuyqty", "0"),
                        cfsellqty = item.optString("cfsellqty", "0")
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing positions: ${e.message}")
        }
        return result
    }

    fun parseQuotesResponse(body: String): List<SmartApiScripQuote> {
        val result = mutableListOf<SmartApiScripQuote>()
        try {
            val json = JSONObject(body)
            if (!isSuccess(json)) return emptyList()

            val dataObj = json.optJSONObject("data")
            val quotesArray = dataObj?.optJSONArray("fetched") ?: json.optJSONArray("data")
            if (quotesArray != null) {
                for (i in 0 until quotesArray.length()) {
                    val item = quotesArray.optJSONObject(i) ?: continue

                    fun itemDouble(key: String): Double? {
                        if (!item.has(key)) return null
                        val s = item.optString(key, "")
                        return s.toDoubleOrNull() ?: item.optDouble(key, Double.NaN).takeUnless { it.isNaN() }
                    }

                    result.add(
                        SmartApiScripQuote(
                            exchange = item.optString("exchange", "NSE"),
                            tradingSymbol = item.optString("tradingSymbol", item.optString("tradingsymbol", "")),
                            symbolToken = item.optString("symbolToken", item.optString("symboltoken", "")),
                            ltp = itemDouble("ltp"),
                            open = itemDouble("open"),
                            high = itemDouble("high"),
                            low = itemDouble("low"),
                            close = itemDouble("close"),
                            change = itemDouble("change"),
                            percentChange = itemDouble("percentChange"),
                            tradeVolume = item.optLong("tradeVolume", item.optLong("volume", 0L)),
                            high52 = itemDouble("high52"),
                            low52 = itemDouble("low52")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing quotes: ${e.message}")
        }
        return result
    }

    fun parseLtpResponse(body: String): Double? {
        return try {
            val json = JSONObject(body)
            if (!isSuccess(json)) return null
            val dataObj = json.optJSONObject("data") ?: return null
            val str = dataObj.optString("ltp", "")
            str.toDoubleOrNull() ?: dataObj.optDouble("ltp", Double.NaN).takeUnless { it.isNaN() }
        } catch (e: Exception) {
            null
        }
    }
}
