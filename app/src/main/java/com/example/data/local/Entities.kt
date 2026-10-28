package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "default_user",
    val name: String = "Rakesh Nironi",
    val email: String = "rakeshnironi@gmail.com",
    val phone: String = "+91 9876543210",
    val virtualBalance: Double = 100000.0, // ₹1,00,000 virtual balance for paper trading
    val realBalance: Double = 148250.0, // Live broker margin for real trading
    val isWhatsappEnabled: Boolean = true,
    val isTradeAlertsEnabled: Boolean = true,
    val isAiSignalsEnabled: Boolean = true,
    val isPriceAlertsEnabled: Boolean = true,
    val isDailySummaryEnabled: Boolean = true,
    val whatsappApiKey: String = "",
    val whatsappPhoneNumberId: String = "",
    val whatsappBusinessAccountId: String = "",
    val angelApiKey: String = "",
    val angelClientId: String = "",
    val isDarkTheme: Boolean = true
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String,
    val exchange: String,
    val token: String,
    val companyName: String,
    val addedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "portfolio_holdings")
data class PortfolioHoldingEntity(
    @PrimaryKey val symbol: String,
    val exchange: String,
    val token: String,
    val companyName: String,
    val quantity: Int,
    val avgBuyPrice: Double,
    val totalInvested: Double
)

@Entity(tableName = "paper_transactions")
data class PaperTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val companyName: String,
    val action: String, // "BUY" or "SELL"
    val quantity: Int,
    val executionPrice: Double,
    val totalValue: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val realizedPnl: Double = 0.0,
    val virtualBalanceAfter: Double
)

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val token: String,
    val targetPrice: Double,
    val condition: String, // "ABOVE" or "BELOW"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val recipientNumber: String,
    val messageText: String,
    val status: String, // "SENT", "FAILED", "PENDING"
    val timestamp: Long = System.currentTimeMillis(),
    val errorDetails: String? = null
)

/**
 * Entity representing the Virtual Cash Balance and Margin Account for Paper Trading
 */
@Entity(tableName = "virtual_cash_account")
data class VirtualCashAccountEntity(
    @PrimaryKey val id: String = "default_account",
    val initialBalance: Double = 100000.0,
    val cashBalance: Double = 100000.0,
    val reservedMargin: Double = 0.0,
    val totalRealizedPnl: Double = 0.0,
    val totalTradesCount: Int = 0,
    val winningTradesCount: Int = 0,
    val losingTradesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

/**
 * Entity tracking Open and Closed Trade Positions in the Paper Trading Portfolio
 */
@Entity(tableName = "trade_positions")
data class TradePositionEntity(
    @PrimaryKey(autoGenerate = true) val positionId: Long = 0,
    val symbol: String,
    val exchange: String, // "NSE" or "BSE"
    val token: String,
    val companyName: String,
    val positionSide: String, // "BUY" / "LONG" or "SELL" / "SHORT"
    val productType: String = "DELIVERY", // "DELIVERY", "INTRADAY", "CNC", "MIS"
    val quantity: Int,
    val entryPrice: Double,
    val currentLtp: Double,
    val targetPrice: Double? = null,
    val stopLossPrice: Double? = null,
    val investedMargin: Double,
    val leverage: Double = 1.0,
    val unrealizedPnL: Double = 0.0,
    val unrealizedPnLPercent: Double = 0.0,
    val realizedPnL: Double = 0.0,
    val status: String = "OPEN", // "OPEN", "CLOSED", "PARTIALLY_CLOSED"
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val exitPrice: Double? = null,
    val closeReason: String? = null // "MANUAL", "TARGET_HIT", "STOP_LOSS_HIT", "SQUARE_OFF"
)

/**
 * Entity logging every Virtual Cash credit/debit transaction and margin adjustments
 */
@Entity(tableName = "cash_ledger")
data class CashLedgerEntity(
    @PrimaryKey(autoGenerate = true) val ledgerId: Long = 0,
    val accountId: String = "default_account",
    val transactionType: String, // "DEPOSIT", "WITHDRAWAL", "TRADE_BUY", "TRADE_SELL", "REALIZED_PROFIT", "REALIZED_LOSS", "RESET"
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val referenceSymbol: String? = null,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Entity storing secure user authentication credentials with BCrypt hashed passwords
 */
@Entity(tableName = "user_auth")
data class UserAuthEntity(
    @PrimaryKey val id: String, // Unique user UUID or ID
    val email: String,
    val name: String,
    val phone: String,
    val passwordHash: String, // BCrypt salt & hash format
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val failedAttempts: Int = 0,
    val lockedUntil: Long = 0L
)

/**
 * Entity tracking time-bound 6-digit OTP verification codes for password resets
 */
@Entity(tableName = "password_reset_otp")
data class PasswordResetOtpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val otpCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val isUsed: Boolean = false
)

/**
 * Entity recording device sessions and active JWT access tokens
 */
@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val sessionId: String,
    val userId: String,
    val email: String,
    val jwtToken: String,
    val refreshToken: String,
    val deviceInfo: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val isActive: Boolean = true
)

