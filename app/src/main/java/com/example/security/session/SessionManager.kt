package com.example.security.session

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.security.jwt.JwtClaims
import com.example.security.jwt.JwtService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class UserSession(
    val sessionId: String,
    val userId: String,
    val email: String,
    val name: String,
    val phone: String,
    val token: String,
    val refreshToken: String,
    val issuedAt: Long,
    val expiresAt: Long,
    val deviceModel: String,
    val isActive: Boolean
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAt

    val remainingMinutes: Long
        get() = ((expiresAt - System.currentTimeMillis()) / (60 * 1000L)).coerceAtLeast(0L)

    val maskedToken: String
        get() = if (token.length > 16) {
            "${token.take(8)}...${token.takeLast(8)}"
        } else {
            token
        }
}

class SessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("autonomous_trading_session_prefs", Context.MODE_PRIVATE)

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _isSessionValid = MutableStateFlow(false)
    val isSessionValid: StateFlow<Boolean> = _isSessionValid.asStateFlow()

    init {
        restoreSession()
    }

    /**
     * Restore session from persistent storage and validate JWT
     */
    fun restoreSession(): Boolean {
        val token = prefs.getString(KEY_JWT_TOKEN, null) ?: run {
            clearSessionMemory()
            return false
        }
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
        val userId = prefs.getString(KEY_USER_ID, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_NAME, "") ?: ""
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val sessionId = prefs.getString(KEY_SESSION_ID, UUID.randomUUID().toString()) ?: UUID.randomUUID().toString()
        val deviceModel = prefs.getString(KEY_DEVICE_MODEL, getDeviceName()) ?: getDeviceName()
        val issuedAt = prefs.getLong(KEY_ISSUED_AT, System.currentTimeMillis())
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)

        // Validate JWT signature and expiration
        val claims = JwtService.verifyAndParseToken(token)
        if (claims != null && !claims.isExpired) {
            val session = UserSession(
                sessionId = sessionId,
                userId = claims.userId.ifEmpty { userId },
                email = claims.email.ifEmpty { email },
                name = claims.name.ifEmpty { name },
                phone = claims.phone.ifEmpty { phone },
                token = token,
                refreshToken = refreshToken,
                issuedAt = claims.issuedAt,
                expiresAt = claims.expiresAt,
                deviceModel = deviceModel,
                isActive = true
            )
            _currentSession.value = session
            _isSessionValid.value = true
            return true
        } else {
            // Token expired or invalid
            clearSession()
            return false
        }
    }

    /**
     * Start a new authenticated session with JWT and refresh tokens
     */
    fun saveSession(
        userId: String,
        email: String,
        name: String,
        phone: String,
        token: String,
        refreshToken: String,
        expiresAt: Long,
        issuedAt: Long = System.currentTimeMillis()
    ): UserSession {
        val sessionId = UUID.randomUUID().toString()
        val deviceModel = getDeviceName()

        prefs.edit()
            .putString(KEY_SESSION_ID, sessionId)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, name)
            .putString(KEY_PHONE, phone)
            .putString(KEY_JWT_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_DEVICE_MODEL, deviceModel)
            .putLong(KEY_ISSUED_AT, issuedAt)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()

        val session = UserSession(
            sessionId = sessionId,
            userId = userId,
            email = email,
            name = name,
            phone = phone,
            token = token,
            refreshToken = refreshToken,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            deviceModel = deviceModel,
            isActive = true
        )

        _currentSession.value = session
        _isSessionValid.value = true
        return session
    }

    /**
     * Refresh active session token with new expiry
     */
    fun refreshSessionToken(newToken: String, newExpiresAt: Long) {
        val current = _currentSession.value ?: return
        val updated = current.copy(
            token = newToken,
            expiresAt = newExpiresAt
        )
        prefs.edit()
            .putString(KEY_JWT_TOKEN, newToken)
            .putLong(KEY_EXPIRES_AT, newExpiresAt)
            .apply()
        _currentSession.value = updated
        _isSessionValid.value = true
    }

    /**
     * Update active session user details when profile is edited
     */
    fun updateSessionUser(name: String, email: String, phone: String) {
        val current = _currentSession.value ?: return
        val updated = current.copy(
            name = name,
            email = email,
            phone = phone
        )
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PHONE, phone)
            .apply()
        _currentSession.value = updated
    }

    /**
     * Terminate active session and clear credentials
     */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_SESSION_ID)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_NAME)
            .remove(KEY_PHONE)
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ISSUED_AT)
            .remove(KEY_EXPIRES_AT)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        clearSessionMemory()
    }

    private fun clearSessionMemory() {
        _currentSession.value = null
        _isSessionValid.value = false
    }

    fun getActiveToken(): String? {
        return _currentSession.value?.token ?: prefs.getString(KEY_JWT_TOKEN, null)
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    companion object {
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_DEVICE_MODEL = "device_model"
        private const val KEY_ISSUED_AT = "issued_at"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
