package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        WatchlistEntity::class,
        PortfolioHoldingEntity::class,
        PaperTransactionEntity::class,
        PriceAlertEntity::class,
        NotificationLogEntity::class,
        VirtualCashAccountEntity::class,
        TradePositionEntity::class,
        CashLedgerEntity::class,
        UserAuthEntity::class,
        PasswordResetOtpEntity::class,
        UserSessionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun paperTransactionDao(): PaperTransactionDao
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun tradePositionDao(): TradePositionDao
    abstract fun virtualCashAccountDao(): VirtualCashAccountDao
    abstract fun cashLedgerDao(): CashLedgerDao
    abstract fun userAuthDao(): UserAuthDao
    abstract fun passwordResetOtpDao(): PasswordResetOtpDao
    abstract fun userSessionDao(): UserSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autonomous_trading_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
