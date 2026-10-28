package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CashLedgerDao
import com.example.data.local.CashLedgerEntity
import com.example.data.local.TradePositionDao
import com.example.data.local.TradePositionEntity
import com.example.data.local.VirtualCashAccountDao
import com.example.data.local.VirtualCashAccountEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PaperTradingRoomDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var tradePositionDao: TradePositionDao
    private lateinit var virtualCashAccountDao: VirtualCashAccountDao
    private lateinit var cashLedgerDao: CashLedgerDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tradePositionDao = db.tradePositionDao()
        virtualCashAccountDao = db.virtualCashAccountDao()
        cashLedgerDao = db.cashLedgerDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testVirtualCashAccountLifecycle() = runBlocking {
        val account = VirtualCashAccountEntity(
            id = "default_account",
            initialBalance = 100000.0,
            cashBalance = 100000.0,
            reservedMargin = 0.0,
            totalRealizedPnl = 0.0
        )
        virtualCashAccountDao.insertOrUpdateAccount(account)

        val fetched = virtualCashAccountDao.getAccountOnce("default_account")
        assertNotNull(fetched)
        assertEquals(100000.0, fetched!!.cashBalance, 0.001)

        // Record a winning trade PnL
        virtualCashAccountDao.recordTradePnL(
            pnlDelta = 2500.0,
            winIncrement = 1,
            lossIncrement = 0
        )

        val updated = virtualCashAccountDao.getAccount("default_account").first()
        assertNotNull(updated)
        assertEquals(102500.0, updated!!.cashBalance, 0.001)
        assertEquals(2500.0, updated.totalRealizedPnl, 0.001)
        assertEquals(1, updated.totalTradesCount)
        assertEquals(1, updated.winningTradesCount)
        assertEquals(0, updated.losingTradesCount)
    }

    @Test
    fun testTradePositionManagement() = runBlocking {
        val position = TradePositionEntity(
            symbol = "RELIANCE",
            exchange = "NSE",
            token = "2885",
            companyName = "Reliance Industries Ltd",
            positionSide = "BUY",
            productType = "DELIVERY",
            quantity = 10,
            entryPrice = 2500.0,
            currentLtp = 2500.0,
            targetPrice = 2650.0,
            stopLossPrice = 2420.0,
            investedMargin = 25000.0,
            status = "OPEN"
        )

        val id = tradePositionDao.insertPosition(position)
        assertTrue(id > 0)

        val openPositions = tradePositionDao.getOpenPositions().first()
        assertEquals(1, openPositions.size)
        assertEquals("RELIANCE", openPositions[0].symbol)
        assertEquals(10, openPositions[0].quantity)

        // Update live price & unrealized PnL
        tradePositionDao.updatePositionPriceAndPnL(
            positionId = id,
            currentLtp = 2550.0,
            unrealizedPnL = 500.0,
            unrealizedPnLPercent = 2.0
        )

        val updatedPos = tradePositionDao.getPositionById(id)
        assertNotNull(updatedPos)
        assertEquals(2550.0, updatedPos!!.currentLtp, 0.001)
        assertEquals(500.0, updatedPos.unrealizedPnL, 0.001)

        // Close position
        tradePositionDao.closePosition(
            positionId = id,
            exitPrice = 2550.0,
            realizedPnL = 500.0,
            closeReason = "TARGET_HIT"
        )

        val openAfterClose = tradePositionDao.getOpenPositions().first()
        assertEquals(0, openAfterClose.size)

        val closedPositions = tradePositionDao.getClosedPositions().first()
        assertEquals(1, closedPositions.size)
        assertEquals("CLOSED", closedPositions[0].status)
        assertEquals("TARGET_HIT", closedPositions[0].closeReason)
        assertEquals(500.0, closedPositions[0].realizedPnL, 0.001)
    }

    @Test
    fun testCashLedgerTracking() = runBlocking {
        val entry1 = CashLedgerEntity(
            transactionType = "DEPOSIT",
            amount = 100000.0,
            balanceBefore = 0.0,
            balanceAfter = 100000.0,
            description = "Initial Paper Funds Allocation"
        )
        cashLedgerDao.insertEntry(entry1)

        val entry2 = CashLedgerEntity(
            transactionType = "TRADE_BUY",
            amount = 25000.0,
            balanceBefore = 100000.0,
            balanceAfter = 75000.0,
            referenceSymbol = "INFY",
            description = "Paper Buy 15 shares of INFY"
        )
        cashLedgerDao.insertEntry(entry2)

        val ledger = cashLedgerDao.getLedgerEntries("default_account").first()
        assertEquals(2, ledger.size)
        assertEquals("TRADE_BUY", ledger[0].transactionType)
        assertEquals(75000.0, ledger[0].balanceAfter, 0.001)
    }
}
