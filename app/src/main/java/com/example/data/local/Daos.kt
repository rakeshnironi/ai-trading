package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun getUserProfile(id: String = "default_user"): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getUserProfileOnce(id: String = "default_user"): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET name = :name, email = :email, phone = :phone, angelClientId = :clientCode WHERE id = :id")
    suspend fun updateProfileBasicDetails(id: String, name: String, email: String, phone: String, clientCode: String)

    @Query("UPDATE user_profile SET virtualBalance = :newBalance WHERE id = :id")
    suspend fun updateBalance(newBalance: Double, id: String = "default_user")

    @Query("UPDATE user_profile SET isWhatsappEnabled = :whatsapp, isTradeAlertsEnabled = :trade, isAiSignalsEnabled = :ai, isPriceAlertsEnabled = :price, isDailySummaryEnabled = :daily WHERE id = :id")
    suspend fun updateNotificationSettings(
        whatsapp: Boolean,
        trade: Boolean,
        ai: Boolean,
        price: Boolean,
        daily: Boolean,
        id: String = "default_user"
    )
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY isFavorite DESC, addedAt DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun deleteWatchlist(symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    fun isInWatchlist(symbol: String): Flow<Boolean>

    @Query("UPDATE watchlist SET isFavorite = :favorite WHERE symbol = :symbol")
    suspend fun toggleFavorite(symbol: String, favorite: Boolean)
}

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_holdings WHERE quantity > 0 ORDER BY symbol ASC")
    fun getAllHoldings(): Flow<List<PortfolioHoldingEntity>>

    @Query("SELECT * FROM portfolio_holdings WHERE symbol = :symbol LIMIT 1")
    suspend fun getHoldingBySymbol(symbol: String): PortfolioHoldingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHolding(holding: PortfolioHoldingEntity)

    @Query("DELETE FROM portfolio_holdings WHERE symbol = :symbol")
    suspend fun deleteHolding(symbol: String)

    @Query("DELETE FROM portfolio_holdings")
    suspend fun clearHoldings()
}

@Dao
interface PaperTransactionDao {
    @Query("SELECT * FROM paper_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<PaperTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PaperTransactionEntity): Long

    @Query("DELETE FROM paper_transactions")
    suspend fun clearTransactions()
}

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts WHERE isActive = 1")
    suspend fun getActiveAlerts(): List<PriceAlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlertEntity): Long

    @Update
    suspend fun updateAlert(alert: PriceAlertEntity)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteAlert(id: Long)

    @Query("UPDATE price_alerts SET isActive = 0, triggeredAt = :triggeredAt WHERE id = :id")
    suspend fun markTriggered(id: Long, triggeredAt: Long = System.currentTimeMillis())
}

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllNotificationLogs(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLogEntity): Long

    @Query("DELETE FROM notification_logs")
    suspend fun clearLogs()
}

/**
 * Data Access Object for Open & Closed Trade Positions in Paper Trading Portfolio
 */
@Dao
interface TradePositionDao {

    @Query("SELECT * FROM trade_positions WHERE status = 'OPEN' ORDER BY openedAt DESC")
    fun getOpenPositions(): Flow<List<TradePositionEntity>>

    @Query("SELECT * FROM trade_positions ORDER BY openedAt DESC")
    fun getAllPositions(): Flow<List<TradePositionEntity>>

    @Query("SELECT * FROM trade_positions WHERE status = 'CLOSED' ORDER BY closedAt DESC")
    fun getClosedPositions(): Flow<List<TradePositionEntity>>

    @Query("SELECT * FROM trade_positions WHERE positionId = :id LIMIT 1")
    suspend fun getPositionById(id: Long): TradePositionEntity?

    @Query("SELECT * FROM trade_positions WHERE symbol = :symbol AND status = 'OPEN' LIMIT 1")
    suspend fun getOpenPositionBySymbol(symbol: String): TradePositionEntity?

    @Query("SELECT * FROM trade_positions WHERE symbol = :symbol AND status = 'OPEN'")
    fun getOpenPositionsFlowBySymbol(symbol: String): Flow<List<TradePositionEntity>>

    @Query("SELECT * FROM trade_positions WHERE symbol = :symbol AND positionSide = :side AND status = 'OPEN' LIMIT 1")
    suspend fun getOpenPositionBySymbolAndSide(symbol: String, side: String): TradePositionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: TradePositionEntity): Long

    @Update
    suspend fun updatePosition(position: TradePositionEntity)

    @Query("UPDATE trade_positions SET currentLtp = :currentLtp, unrealizedPnL = :unrealizedPnL, unrealizedPnLPercent = :unrealizedPnLPercent WHERE positionId = :positionId")
    suspend fun updatePositionPriceAndPnL(positionId: Long, currentLtp: Double, unrealizedPnL: Double, unrealizedPnLPercent: Double)

    @Query("UPDATE trade_positions SET stopLossPrice = :stopLoss, targetPrice = :target WHERE positionId = :positionId")
    suspend fun updateStopLossAndTarget(positionId: Long, stopLoss: Double?, target: Double?)

    @Query("UPDATE trade_positions SET status = 'CLOSED', exitPrice = :exitPrice, realizedPnL = :realizedPnL, closeReason = :closeReason, closedAt = :closedAt WHERE positionId = :positionId")
    suspend fun closePosition(
        positionId: Long,
        exitPrice: Double,
        realizedPnL: Double,
        closeReason: String,
        closedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT SUM(investedMargin) FROM trade_positions WHERE status = 'OPEN'")
    fun getTotalInvestedMargin(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM trade_positions WHERE status = 'OPEN'")
    fun getOpenPositionsCount(): Flow<Int>

    @Query("DELETE FROM trade_positions WHERE positionId = :id")
    suspend fun deletePositionById(id: Long)

    @Query("DELETE FROM trade_positions")
    suspend fun clearAllPositions()
}

/**
 * Data Access Object for Paper Trading Virtual Cash Balance & Account Metrics
 */
@Dao
interface VirtualCashAccountDao {

    @Query("SELECT * FROM virtual_cash_account WHERE id = :id LIMIT 1")
    fun getAccount(id: String = "default_account"): Flow<VirtualCashAccountEntity?>

    @Query("SELECT * FROM virtual_cash_account WHERE id = :id LIMIT 1")
    suspend fun getAccountOnce(id: String = "default_account"): VirtualCashAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccount(account: VirtualCashAccountEntity)

    @Query("UPDATE virtual_cash_account SET cashBalance = :cashBalance, lastUpdatedAt = :now WHERE id = :id")
    suspend fun updateCashBalance(cashBalance: Double, now: Long = System.currentTimeMillis(), id: String = "default_account")

    @Query("UPDATE virtual_cash_account SET reservedMargin = :margin, lastUpdatedAt = :now WHERE id = :id")
    suspend fun updateReservedMargin(margin: Double, now: Long = System.currentTimeMillis(), id: String = "default_account")

    @Query("UPDATE virtual_cash_account SET cashBalance = cashBalance + :pnlDelta, totalRealizedPnl = totalRealizedPnl + :pnlDelta, totalTradesCount = totalTradesCount + 1, winningTradesCount = winningTradesCount + :winIncrement, losingTradesCount = losingTradesCount + :lossIncrement, lastUpdatedAt = :now WHERE id = :id")
    suspend fun recordTradePnL(
        pnlDelta: Double,
        winIncrement: Int,
        lossIncrement: Int,
        now: Long = System.currentTimeMillis(),
        id: String = "default_account"
    )

    @Query("UPDATE virtual_cash_account SET cashBalance = :initialAmount, initialBalance = :initialAmount, reservedMargin = 0.0, totalRealizedPnl = 0.0, totalTradesCount = 0, winningTradesCount = 0, losingTradesCount = 0, lastUpdatedAt = :now WHERE id = :id")
    suspend fun resetAccount(initialAmount: Double = 100000.0, now: Long = System.currentTimeMillis(), id: String = "default_account")
}

/**
 * Data Access Object for Virtual Cash Ledger Entries
 */
@Dao
interface CashLedgerDao {

    @Query("SELECT * FROM cash_ledger WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getLedgerEntries(accountId: String = "default_account"): Flow<List<CashLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CashLedgerEntity): Long

    @Query("DELETE FROM cash_ledger WHERE accountId = :accountId")
    suspend fun clearLedger(accountId: String = "default_account")
}

/**
 * Data Access Object for User Authentication
 */
@Dao
interface UserAuthDao {

    @Query("SELECT * FROM user_auth WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAuthEntity?

    @Query("SELECT * FROM user_auth WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserAuthEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAuthEntity)

    @Update
    suspend fun updateUser(user: UserAuthEntity)

    @Query("UPDATE user_auth SET name = :name, phone = :phone, updatedAt = :now WHERE id = :id OR LOWER(email) = LOWER(:email)")
    suspend fun updateUserDetails(id: String, email: String, name: String, phone: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE user_auth SET passwordHash = :newHash, updatedAt = :now WHERE LOWER(email) = LOWER(:email)")
    suspend fun updatePassword(email: String, newHash: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE user_auth SET lastLoginAt = :now, failedAttempts = 0, lockedUntil = 0 WHERE LOWER(email) = LOWER(:email)")
    suspend fun recordSuccessfulLogin(email: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE user_auth SET failedAttempts = :attempts, lockedUntil = :lockoutUntil WHERE LOWER(email) = LOWER(:email)")
    suspend fun recordFailedLogin(email: String, attempts: Int, lockoutUntil: Long)

    @Query("SELECT COUNT(*) FROM user_auth")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM user_auth ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAuthEntity>>
}

/**
 * Data Access Object for Password Reset OTP verification
 */
@Dao
interface PasswordResetOtpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtp(otp: PasswordResetOtpEntity): Long

    @Query("SELECT * FROM password_reset_otp WHERE LOWER(email) = LOWER(:email) AND isUsed = 0 AND expiresAt > :now ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestValidOtp(email: String, now: Long = System.currentTimeMillis()): PasswordResetOtpEntity?

    @Query("UPDATE password_reset_otp SET isUsed = 1 WHERE id = :id")
    suspend fun markOtpUsed(id: Long)

    @Query("UPDATE password_reset_otp SET isUsed = 1 WHERE LOWER(email) = LOWER(:email)")
    suspend fun invalidateOtpsForEmail(email: String)
}

/**
 * Data Access Object for User Sessions
 */
@Dao
interface UserSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSessionEntity)

    @Query("SELECT * FROM user_sessions WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveSessions(userId: String): Flow<List<UserSessionEntity>>

    @Query("SELECT * FROM user_sessions WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveSessionsOnce(userId: String): List<UserSessionEntity>

    @Query("UPDATE user_sessions SET isActive = 0 WHERE sessionId = :sessionId")
    suspend fun terminateSession(sessionId: String)

    @Query("UPDATE user_sessions SET isActive = 0 WHERE userId = :userId")
    suspend fun terminateAllUserSessions(userId: String)
}

