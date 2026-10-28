package com.example

import com.example.ui.theme.DarkTradingColors
import com.example.ui.theme.LightTradingColors
import com.example.ui.theme.ThemeMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testThemeModeEnumValues() {
    assertEquals("Dark", ThemeMode.DARK.displayName)
    assertEquals("Light", ThemeMode.LIGHT.displayName)
    assertEquals("System", ThemeMode.SYSTEM.displayName)
  }

  @Test
  fun testTradingColorsDarkVersusLight() {
    assertTrue(DarkTradingColors.isDark)
    assertFalse(LightTradingColors.isDark)

    // Verify background and accents differ appropriately between dark and light
    assertNotEquals(DarkTradingColors.canvasBackground, LightTradingColors.canvasBackground)
    assertNotEquals(DarkTradingColors.cardBackground, LightTradingColors.cardBackground)
    assertNotEquals(DarkTradingColors.textPrimary, LightTradingColors.textPrimary)
    assertNotEquals(DarkTradingColors.primaryAccent, LightTradingColors.primaryAccent)
  }
}
