package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.NotificationLogEntity
import com.example.data.local.PasswordResetOtpEntity
import com.example.data.local.UserAuthEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.UserSessionEntity
import com.example.data.local.VirtualCashAccountEntity
import com.example.security.crypto.BCrypt
import com.example.security.jwt.JwtService
import com.example.security.session.SessionManager
import com.example.security.session.UserSession
import kotlinx.coroutines.flow.Flow
import java.security.SecureRandom
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: UserAuthEntity, val session: UserSession, val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class OtpResult {
    data class Success(val email: String, val otpCode: String, val message: String) : OtpResult()
    data class Error(val message: String) : OtpResult()
}

sealed class PasswordResetResult {
    data class Success(val message: String) : PasswordResetResult()
    data class Error(val message: String) : PasswordResetResult()
}

class AuthRepository(
    private val db: AppDatabase,
    private val sessionManager: SessionManager
) {
    private val userAuthDao = db.userAuthDao()
    private val otpDao = db.passwordResetOtpDao()
    private val sessionDao = db.userSessionDao()
    private val userProfileDao = db.userProfileDao()
    private val virtualCashAccountDao = db.virtualCashAccountDao()
    private val notificationLogDao = db.notificationLogDao()

    private val secureRandom = SecureRandom()

    /**
     * Ensure default test user is pre-seeded with valid BCrypt hash
     */
    suspend fun seedDefaultUserIfEmpty() {
        if (userAuthDao.getUserCount() == 0) {
            val defaultEmail = "rakeshnironi@gmail.com"
            val defaultPassword = "Password@123"
            val salt = BCrypt.gensalt(10)
            val hash = BCrypt.hashpw(defaultPassword, salt)

            val defaultUser = UserAuthEntity(
                id = "usr_rakesh_001",
                email = defaultEmail,
                name = "Rakesh Nironi",
                phone = "+91 9876543210",
                passwordHash = hash,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
            userAuthDao.insertUser(defaultUser)

            // Seed Profile Entity
            userProfileDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = defaultUser.id,
                    name = defaultUser.name,
                    email = defaultUser.email,
                    phone = defaultUser.phone,
                    virtualBalance = 100000.0,
                    isWhatsappEnabled = true,
                    isTradeAlertsEnabled = true,
                    isAiSignalsEnabled = true,
                    isPriceAlertsEnabled = true,
                    isDailySummaryEnabled = true
                )
            )

            // Seed Virtual Cash Account
            if (virtualCashAccountDao.getAccountOnce(defaultUser.id) == null) {
                virtualCashAccountDao.insertOrUpdateAccount(
                    VirtualCashAccountEntity(
                        id = defaultUser.id,
                        initialBalance = 100000.0,
                        cashBalance = 100000.0,
                        reservedMargin = 0.0,
                        totalRealizedPnl = 0.0
                    )
                )
            }
        }
    }

    /**
     * User Registration with BCrypt password hashing & JWT generation
     */
    suspend fun registerUser(
        name: String,
        email: String,
        phone: String,
        password: String
    ): AuthResult {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()
        val cleanPhone = phone.trim()

        if (cleanName.length < 2) {
            return AuthResult.Error("Please enter your full name (minimum 2 characters)")
        }

        if (!isValidEmail(cleanEmail)) {
            return AuthResult.Error("Please enter a valid email address")
        }

        val passwordValidation = validatePasswordStrength(password)
        if (passwordValidation != null) {
            return AuthResult.Error(passwordValidation)
        }

        // Check if user already exists
        val existing = userAuthDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return AuthResult.Error("An account with $cleanEmail already exists. Please login.")
        }

        // 1. BCrypt Password Hashing
        val salt = BCrypt.gensalt(10)
        val passwordHash = BCrypt.hashpw(password, salt)

        val userId = "usr_" + UUID.randomUUID().toString().take(12)
        val now = System.currentTimeMillis()

        val newUser = UserAuthEntity(
            id = userId,
            email = cleanEmail,
            name = cleanName,
            phone = cleanPhone.ifEmpty { "+91 9876543210" },
            passwordHash = passwordHash,
            createdAt = now,
            updatedAt = now,
            lastLoginAt = now
        )

        userAuthDao.insertUser(newUser)

        // 2. Initialize User Profile and ₹1,00,000 Virtual Balance
        userProfileDao.insertOrUpdateProfile(
            UserProfileEntity(
                id = userId,
                name = cleanName,
                email = cleanEmail,
                phone = cleanPhone.ifEmpty { "+91 9876543210" },
                virtualBalance = 100000.0,
                isWhatsappEnabled = true,
                isTradeAlertsEnabled = true,
                isAiSignalsEnabled = true,
                isPriceAlertsEnabled = true,
                isDailySummaryEnabled = true
            )
        )

        virtualCashAccountDao.insertOrUpdateAccount(
            VirtualCashAccountEntity(
                id = userId,
                initialBalance = 100000.0,
                cashBalance = 100000.0,
                reservedMargin = 0.0,
                totalRealizedPnl = 0.0
            )
        )

        // 3. Generate JWT & Save Session
        val token = JwtService.generateAccessToken(
            userId = userId,
            email = cleanEmail,
            name = cleanName,
            phone = cleanPhone,
            expiresInMillis = JwtService.ACCESS_TOKEN_EXPIRY_MS
        )
        val refreshToken = JwtService.generateRefreshToken()
        val expiresAt = now + JwtService.ACCESS_TOKEN_EXPIRY_MS

        val session = sessionManager.saveSession(
            userId = userId,
            email = cleanEmail,
            name = cleanName,
            phone = cleanPhone,
            token = token,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            issuedAt = now
        )

        // Record session in DB
        sessionDao.insertSession(
            UserSessionEntity(
                sessionId = session.sessionId,
                userId = userId,
                email = cleanEmail,
                jwtToken = token,
                refreshToken = refreshToken,
                deviceInfo = session.deviceModel,
                createdAt = now,
                expiresAt = expiresAt,
                isActive = true
            )
        )

        return AuthResult.Success(
            user = newUser,
            session = session,
            message = "Account created successfully! ₹1,00,000 virtual balance credited."
        )
    }

    /**
     * Secure Login with BCrypt hash verification, lockout protection, and JWT generation
     */
    suspend fun loginUser(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty()) {
            return AuthResult.Error("Please enter your email address")
        }
        if (password.isEmpty()) {
            return AuthResult.Error("Please enter your password")
        }

        // Seed test account if needed so login never fails for demo
        seedDefaultUserIfEmpty()

        val user = userAuthDao.getUserByEmail(cleanEmail)
        if (user == null) {
            return AuthResult.Error("No account found with $cleanEmail. Please register.")
        }

        // Check account lock status
        val now = System.currentTimeMillis()
        if (user.lockedUntil > now) {
            val remainingSec = (user.lockedUntil - now) / 1000
            return AuthResult.Error("Account temporarily locked due to failed attempts. Try again in $remainingSec seconds.")
        }

        // Verify password using BCrypt
        val isPasswordCorrect = BCrypt.checkpw(password, user.passwordHash)
        if (!isPasswordCorrect) {
            val newAttempts = user.failedAttempts + 1
            val lockDuration = if (newAttempts >= 5) 5 * 60 * 1000L else 0L // 5 mins lockout after 5 fails
            val lockedUntil = if (lockDuration > 0) now + lockDuration else 0L

            userAuthDao.recordFailedLogin(cleanEmail, newAttempts, lockedUntil)
            val msg = if (lockedUntil > 0) {
                "Too many failed login attempts. Account locked for 5 minutes."
            } else {
                "Invalid password. (${5 - newAttempts} attempts remaining)"
            }
            return AuthResult.Error(msg)
        }

        // Successful authentication: Reset attempts and update login time
        userAuthDao.recordSuccessfulLogin(cleanEmail, now)

        // Generate JWT Access Token and Refresh Token
        val token = JwtService.generateAccessToken(
            userId = user.id,
            email = user.email,
            name = user.name,
            phone = user.phone,
            expiresInMillis = JwtService.ACCESS_TOKEN_EXPIRY_MS
        )
        val refreshToken = JwtService.generateRefreshToken()
        val expiresAt = now + JwtService.ACCESS_TOKEN_EXPIRY_MS

        val session = sessionManager.saveSession(
            userId = user.id,
            email = user.email,
            name = user.name,
            phone = user.phone,
            token = token,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            issuedAt = now
        )

        // Store session in Room DB
        sessionDao.insertSession(
            UserSessionEntity(
                sessionId = session.sessionId,
                userId = user.id,
                email = user.email,
                jwtToken = token,
                refreshToken = refreshToken,
                deviceInfo = session.deviceModel,
                createdAt = now,
                expiresAt = expiresAt,
                isActive = true
            )
        )

        return AuthResult.Success(
            user = user,
            session = session,
            message = "Logged in securely with JWT token"
        )
    }

    /**
     * Request Password Reset OTP (6-digit cryptographically secure code)
     */
    suspend fun requestPasswordResetOtp(email: String): OtpResult {
        val cleanEmail = email.trim().lowercase()
        if (!isValidEmail(cleanEmail)) {
            return OtpResult.Error("Please enter a valid email address")
        }

        seedDefaultUserIfEmpty()

        val user = userAuthDao.getUserByEmail(cleanEmail)
        if (user == null) {
            return OtpResult.Error("No registered user found with $cleanEmail")
        }

        // Invalidate older OTPs
        otpDao.invalidateOtpsForEmail(cleanEmail)

        // Generate random 6-digit numeric OTP
        val otpNumber = 100000 + secureRandom.nextInt(900000)
        val otpCode = otpNumber.toString()

        val now = System.currentTimeMillis()
        val expiresAt = now + (5 * 60 * 1000L) // 5 minutes validity

        otpDao.insertOtp(
            PasswordResetOtpEntity(
                email = cleanEmail,
                otpCode = otpCode,
                createdAt = now,
                expiresAt = expiresAt,
                isUsed = false
            )
        )

        // Log notification entry in WhatsApp/SMS notification table
        notificationLogDao.insertLog(
            NotificationLogEntity(
                eventType = "SECURITY_ALERT",
                recipientNumber = user.phone,
                messageText = "Your Autonomous Trading OTP is: $otpCode. Valid for 5 minutes. Do not share with anyone.",
                status = "SENT",
                timestamp = now
            )
        )

        return OtpResult.Success(
            email = cleanEmail,
            otpCode = otpCode,
            message = "A 6-digit verification code has been sent to $cleanEmail and ${user.phone}."
        )
    }

    /**
     * Verify OTP Code
     */
    suspend fun verifyOtp(email: String, otpCode: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val cleanOtp = otpCode.trim()
        val now = System.currentTimeMillis()

        val validOtp = otpDao.getLatestValidOtp(cleanEmail, now) ?: return false
        return validOtp.otpCode == cleanOtp
    }

    /**
     * Reset Password using Verified OTP
     */
    suspend fun resetPasswordWithOtp(
        email: String,
        otpCode: String,
        newPassword: String
    ): PasswordResetResult {
        val cleanEmail = email.trim().lowercase()
        val cleanOtp = otpCode.trim()
        val now = System.currentTimeMillis()

        val validOtp = otpDao.getLatestValidOtp(cleanEmail, now)
            ?: return PasswordResetResult.Error("OTP has expired or is invalid. Please request a new code.")

        if (validOtp.otpCode != cleanOtp) {
            return PasswordResetResult.Error("Incorrect OTP entered. Please check and try again.")
        }

        val passwordStrengthError = validatePasswordStrength(newPassword)
        if (passwordStrengthError != null) {
            return PasswordResetResult.Error(passwordStrengthError)
        }

        // Hash new password with BCrypt
        val salt = BCrypt.gensalt(10)
        val newHash = BCrypt.hashpw(newPassword, salt)

        // Update DB
        userAuthDao.updatePassword(cleanEmail, newHash, now)
        otpDao.markOtpUsed(validOtp.id)

        // Invalidate active sessions for security
        val user = userAuthDao.getUserByEmail(cleanEmail)
        if (user != null) {
            sessionDao.terminateAllUserSessions(user.id)
        }

        return PasswordResetResult.Success("Password updated successfully! Please login with your new password.")
    }

    /**
     * Get active device sessions for the current user
     */
    fun getActiveSessions(userId: String): Flow<List<UserSessionEntity>> {
        return sessionDao.getActiveSessions(userId)
    }

    /**
     * Terminate a specific session
     */
    suspend fun terminateSession(sessionId: String) {
        sessionDao.terminateSession(sessionId)
        if (sessionManager.currentSession.value?.sessionId == sessionId) {
            sessionManager.clearSession()
        }
    }

    /**
     * User Logout: Clears persistent JWT session and marks DB session inactive
     */
    suspend fun logout() {
        val current = sessionManager.currentSession.value
        if (current != null) {
            sessionDao.terminateSession(current.sessionId)
        }
        sessionManager.clearSession()
    }

    /**
     * Update user profile details across Auth, Profile, and Session layers
     */
    suspend fun updateUserProfile(name: String, email: String, phone: String, clientCode: String): Result<Unit> {
        val currentSession = sessionManager.currentSession.value
        val userId = currentSession?.userId ?: "usr_rakesh_001"
        val userEmail = currentSession?.email ?: email

        // Update Room DB UserAuthEntity
        userAuthDao.updateUserDetails(
            id = userId,
            email = userEmail,
            name = name,
            phone = phone
        )

        // Update Room DB UserProfileEntity
        val existingProfile = userProfileDao.getUserProfileOnce(userId)
            ?: userProfileDao.getUserProfileOnce("default_user")
            ?: UserProfileEntity(id = userId)

        val updatedProfile = existingProfile.copy(
            id = userId,
            name = name,
            email = email,
            phone = phone,
            angelClientId = clientCode
        )
        userProfileDao.insertOrUpdateProfile(updatedProfile)

        // Also ensure default_user profile stays synchronized
        val defaultProfile = userProfileDao.getUserProfileOnce("default_user")
        if (defaultProfile != null && userId != "default_user") {
            userProfileDao.insertOrUpdateProfile(
                defaultProfile.copy(
                    name = name,
                    email = email,
                    phone = phone,
                    angelClientId = clientCode
                )
            )
        }

        // Update session in memory & persistent prefs
        sessionManager.updateSessionUser(name = name, email = email, phone = phone)

        return Result.success(Unit)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        return email.matches(emailRegex)
    }

    private fun validatePasswordStrength(password: String): String? {
        if (password.length < 8) {
            return "Password must be at least 8 characters long"
        }
        if (!password.any { it.isDigit() }) {
            return "Password must contain at least one number"
        }
        if (!password.any { it.isLetter() }) {
            return "Password must contain at least one letter"
        }
        return null
    }
}
