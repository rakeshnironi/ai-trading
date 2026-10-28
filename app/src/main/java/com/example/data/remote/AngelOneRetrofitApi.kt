package com.example.data.remote

import com.example.data.remote.dto.SmartApiCandleRequest
import com.example.data.remote.dto.SmartApiHoldingsData
import com.example.data.remote.dto.SmartApiLoginData
import com.example.data.remote.dto.SmartApiLoginRequest
import com.example.data.remote.dto.SmartApiLogoutRequest
import com.example.data.remote.dto.SmartApiLtpData
import com.example.data.remote.dto.SmartApiLtpRequest
import com.example.data.remote.dto.SmartApiPositionItem
import com.example.data.remote.dto.SmartApiProfileData
import com.example.data.remote.dto.SmartApiQuoteData
import com.example.data.remote.dto.SmartApiQuoteRequest
import com.example.data.remote.dto.SmartApiResponse
import com.example.data.remote.dto.SmartApiRmsData
import com.example.data.remote.dto.SmartApiSearchRequest
import com.example.data.remote.dto.SmartApiSearchScripData
import com.example.data.remote.dto.SmartApiTokenData
import com.example.data.remote.dto.SmartApiTokenRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit Interface for Angel One SmartAPI V2 endpoints
 */
interface AngelOneRetrofitApi {

    /**
     * Fetch real-time Last Traded Price (LTP) for a specific symbol token
     */
    @POST("rest/secure/angelbroking/order/v1/getLtpData")
    suspend fun getLtpData(
        @Body request: SmartApiLtpRequest,
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<SmartApiLtpData>>

    @POST("rest/secure/angelbroking/order/v1/getLtpData")
    suspend fun getLtpDataRaw(
        @Body request: SmartApiLtpRequest,
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<ResponseBody>

    /**
     * Fetch comprehensive market quotes (OHLC, LTP, Volume, 52W High/Low, 5-level Market Depth)
     */
    @POST("rest/secure/angelbroking/market/v1/quote")
    suspend fun getMarketQuote(
        @Body request: SmartApiQuoteRequest,
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<SmartApiQuoteData>>

    @POST("rest/secure/angelbroking/market/v1/quote")
    suspend fun getMarketQuoteRaw(
        @Body request: SmartApiQuoteRequest,
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<ResponseBody>

    /**
     * Fetch historical candlestick OHLCV data across timeframes (1M, 5M, 15M, 1D, etc.)
     */
    @POST("rest/secure/angelbroking/historical/v1/getCandleData")
    suspend fun getHistoricalCandleData(
        @Body request: SmartApiCandleRequest,
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<List<List<Any>>>>

    /**
     * Search instrument tokens and scrip symbols on NSE/BSE
     */
    @POST("rest/secure/angelbroking/order/v1/searchScrip")
    suspend fun searchScrip(
        @Body request: SmartApiSearchRequest,
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<List<SmartApiSearchScripData>>>

    /**
     * Authenticate and generate session JWT + feed token using TOTP
     */
    @POST("rest/auth/angelbroking/user/v1/loginByPassword")
    suspend fun loginByPassword(
        @Body request: SmartApiLoginRequest,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<SmartApiLoginData>>

    @POST("rest/auth/angelbroking/user/v1/loginByPassword")
    suspend fun loginByPasswordRaw(
        @Body request: SmartApiLoginRequest,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<ResponseBody>

    /**
     * Fetch authorized user trading profile and permissions
     */
    @GET("rest/secure/angelbroking/user/v1/getProfile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<SmartApiProfileData>>

    @GET("rest/secure/angelbroking/user/v1/getProfile")
    suspend fun getProfileRaw(
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<ResponseBody>

    /**
     * Fetch real-time RMS limits, available cash, and trading margin
     */
    @GET("rest/secure/angelbroking/user/v1/getRMS")
    suspend fun getRMS(
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<SmartApiRmsData>>

    @GET("rest/secure/angelbroking/user/v1/getRMS")
    suspend fun getRMSRaw(
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<ResponseBody>

    /**
     * Fetch user's actual portfolio stock holdings (getAllHolding)
     */
    @GET("rest/secure/angelbroking/portfolio/v1/getAllHolding")
    suspend fun getAllHolding(
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<SmartApiHoldingsData>>

    @GET("rest/secure/angelbroking/portfolio/v1/getAllHolding")
    suspend fun getAllHoldingRaw(
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<ResponseBody>

    /**
     * Standard holdings endpoint (getHolding)
     */
    @GET("rest/secure/angelbroking/portfolio/v1/getHolding")
    suspend fun getHoldingRaw(
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<ResponseBody>

    /**
     * Fetch user's open and closed intraday / delivery positions
     */
    @GET("rest/secure/angelbroking/order/v1/getPosition")
    suspend fun getPosition(
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<List<SmartApiPositionItem>>>

    @GET("rest/secure/angelbroking/order/v1/getPosition")
    suspend fun getPositionRaw(
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<ResponseBody>

    /**
     * Renew/Refresh active session tokens
     */
    @POST("rest/auth/angelbroking/jwt/v1/generateTokens")
    suspend fun generateTokens(
        @Body request: SmartApiTokenRequest,
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<SmartApiTokenData>>

    /**
     * Official Broker Session Terminate / Logout
     */
    @POST("rest/secure/angelbroking/user/v1/logout")
    suspend fun logoutUser(
        @Body request: SmartApiLogoutRequest,
        @Header("Authorization") authorization: String? = null,
        @Header("X-PrivateKey") apiKey: String? = null
    ): Response<SmartApiResponse<Map<String, Any>>>
}
