package com.example.security.jwt

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class JwtClaims(
    val userId: String,
    val email: String,
    val name: String,
    val phone: String = "",
    val role: String = "TRADER",
    val issuedAt: Long,
    val expiresAt: Long,
    val tokenId: String = UUID.randomUUID().toString()
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAt

    val remainingMinutes: Long
        get() = ((expiresAt - System.currentTimeMillis()) / (60 * 1000L)).coerceAtLeast(0L)
}

object JwtService {

    // 256-bit cryptographically robust signing key for HMAC-SHA256
    private const val DEFAULT_SECRET = "AutonomousTradingSecureJWTHMAC256Key2026@AngelOneProductionKey98234"
    private const val ALGORITHM = "HmacSHA256"

    // Default Token Durations
    const val ACCESS_TOKEN_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 Hours
    const val REFRESH_TOKEN_EXPIRY_MS = 30 * 24 * 60 * 60 * 1000L // 30 Days

    /**
     * Generate an RFC 7519 compliant JSON Web Token (JWT) with HS256 algorithm
     */
    fun generateAccessToken(
        userId: String,
        email: String,
        name: String,
        phone: String = "",
        role: String = "TRADER",
        expiresInMillis: Long = ACCESS_TOKEN_EXPIRY_MS,
        secret: String = DEFAULT_SECRET
    ): String {
        val now = System.currentTimeMillis()
        val exp = now + expiresInMillis
        val tokenId = UUID.randomUUID().toString()

        // 1. Header
        val headerJson = JSONObject().apply {
            put("alg", "HS256")
            put("typ", "JWT")
        }
        val encodedHeader = base64UrlEncode(headerJson.toString().toByteArray(StandardCharsets.UTF_8))

        // 2. Payload
        val payloadJson = JSONObject().apply {
            put("sub", userId)
            put("email", email)
            put("name", name)
            put("phone", phone)
            put("role", role)
            put("iat", now / 1000)
            put("exp", exp / 1000)
            put("jti", tokenId)
            put("iss", "AutonomousTradingApp")
        }
        val encodedPayload = base64UrlEncode(payloadJson.toString().toByteArray(StandardCharsets.UTF_8))

        // 3. Signature
        val dataToSign = "$encodedHeader.$encodedPayload"
        val signatureBytes = signHmacSha256(dataToSign, secret)
        val encodedSignature = base64UrlEncode(signatureBytes)

        return "$dataToSign.$encodedSignature"
    }

    /**
     * Generate a cryptographically secure Refresh Token
     */
    fun generateRefreshToken(): String {
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(randomBytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    /**
     * Verify and parse JWT token. Returns JwtClaims if valid and not expired, null otherwise.
     */
    fun verifyAndParseToken(token: String, secret: String = DEFAULT_SECRET): JwtClaims? {
        val parts = token.split(".")
        if (parts.size != 3) return null

        val encodedHeader = parts[0]
        val encodedPayload = parts[1]
        val encodedSignature = parts[2]

        val dataToSign = "$encodedHeader.$encodedPayload"
        val expectedSignatureBytes = signHmacSha256(dataToSign, secret)
        val expectedSignature = base64UrlEncode(expectedSignatureBytes)

        // Constant-time signature verification
        if (!constantTimeEquals(encodedSignature, expectedSignature)) {
            return null
        }

        return try {
            val payloadBytes = base64UrlDecode(encodedPayload)
            val payloadString = String(payloadBytes, StandardCharsets.UTF_8)
            val json = JSONObject(payloadString)

            val userId = json.optString("sub", "")
            val email = json.optString("email", "")
            val name = json.optString("name", "")
            val phone = json.optString("phone", "")
            val role = json.optString("role", "TRADER")
            val iatSec = json.optLong("iat", 0L)
            val expSec = json.optLong("exp", 0L)
            val jti = json.optString("jti", UUID.randomUUID().toString())

            val expMs = expSec * 1000L
            val iatMs = iatSec * 1000L

            if (System.currentTimeMillis() > expMs) {
                // Token has expired
                return null
            }

            JwtClaims(
                userId = userId,
                email = email,
                name = name,
                phone = phone,
                role = role,
                issuedAt = iatMs,
                expiresAt = expMs,
                tokenId = jti
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decode claims without strict signature verification (for UI inspector/debugging)
     */
    fun decodeClaimsUnverified(token: String): JwtClaims? {
        val parts = token.split(".")
        if (parts.size < 2) return null
        return try {
            val payloadBytes = base64UrlDecode(parts[1])
            val json = JSONObject(String(payloadBytes, StandardCharsets.UTF_8))
            JwtClaims(
                userId = json.optString("sub", ""),
                email = json.optString("email", ""),
                name = json.optString("name", ""),
                phone = json.optString("phone", ""),
                role = json.optString("role", "TRADER"),
                issuedAt = json.optLong("iat", 0L) * 1000L,
                expiresAt = json.optLong("exp", 0L) * 1000L,
                tokenId = json.optString("jti", "")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun signHmacSha256(data: String, secret: String): ByteArray {
        val mac = Mac.getInstance(ALGORITHM)
        val secretKeySpec = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
        mac.init(secretKeySpec)
        return mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    private fun base64UrlEncode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun base64UrlDecode(base64Url: String): ByteArray {
        return Base64.decode(base64Url, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}
