package com.example

import com.example.data.model.Exchange
import com.example.data.remote.IndianStockDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IndianStockDirectoryTest {

    @Test
    fun testMasterStockCountAndCoverage() {
        val master = IndianStockDirectory.allMasterStocks
        assertTrue("Master stock list should have extensive stock coverage", master.size >= 80)
        
        // Ensure core bluechips and multi-sector companies are present
        val symbols = master.map { it.symbol }.toSet()
        val keySymbols = listOf(
            "RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK", 
            "TATAMOTORS", "SBIN", "BHARTIARTL", "ITC", "LT",
            "HAL", "BEL", "RVNL", "IRFC", "IREDA", "SUZLON", "TRENT", "ZOMATO"
        )
        for (sym in keySymbols) {
            assertTrue("Expected symbol $sym to be in master directory", symbols.contains(sym))
        }
    }

    @Test
    fun testDynamicStockResolutionForArbitraryTicker() {
        // Unknown symbol should be dynamically resolved and synthesized with realistic pricing
        val customStock = IndianStockDirectory.resolveOrSynthesizeStock("MYCUSTOMSTOCK", Exchange.NSE)
        assertNotNull(customStock)
        assertEquals("MYCUSTOMSTOCK", customStock.symbol)
        assertTrue(customStock.currentPrice > 0.0)
        assertTrue(customStock.high52Week >= customStock.low52Week)
        assertTrue(customStock.token.isNotEmpty())
        assertEquals(Exchange.NSE, customStock.exchange)
    }

    @Test
    fun testBseStockResolution() {
        val bseStock = IndianStockDirectory.resolveOrSynthesizeStock("BOMBAYDYEING", Exchange.BSE)
        assertNotNull(bseStock)
        assertEquals("BOMBAYDYEING", bseStock.symbol)
        assertEquals(Exchange.BSE, bseStock.exchange)
    }
}
