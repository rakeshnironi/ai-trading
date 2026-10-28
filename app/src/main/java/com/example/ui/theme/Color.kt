package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Static Dark Palette Constants
val DarkNavyDeep = Color(0xFF111318)
val DarkNavySurface = Color(0xFF1C1B1F)
val DarkNavyCard = Color(0xFF2D2F33)
val DarkNavyCardElevated = Color(0xFF383B40)
val DarkNavyCardSubtle = Color(0x802D2F33)
val DarkNavyBorder = Color(0xFF3E4246)
val DarkNavyBorderSubtle = Color(0x4D3E4246)
val DarkSleekLavender = Color(0xFFD0BCFF)
val DarkSleekDarkPurple = Color(0xFF381E72)
val DarkSleekLavenderLight = Color(0xFFEADDFF)
val DarkEmeraldGreen = Color(0xFFB4E2B4)
val DarkEmeraldGreenDark = Color(0xFF2E6B34)
val DarkCrimsonRed = Color(0xFFF2B8B5)
val DarkCrimsonRedDark = Color(0xFF8C1D18)
val DarkAmberGold = Color(0xFFFFD8E4)
val DarkAiCardBg = Color(0xFF1D241F)
val DarkAiCardBorder = Color(0xFF37493A)
val DarkTextPrimary = Color(0xFFE2E2E6)
val DarkTextSecondary = Color(0xFF919196)
val DarkTextMuted = Color(0xFF75757A)

// Static Light Palette Constants
val LightNavyDeep = Color(0xFFF4F4F8)
val LightNavySurface = Color(0xFFFFFFFF)
val LightNavyCard = Color(0xFFFFFFFF)
val LightNavyCardElevated = Color(0xFFF9FAFD)
val LightNavyCardSubtle = Color(0xFFEFF1F6)
val LightNavyBorder = Color(0xFFE2E4EC)
val LightNavyBorderSubtle = Color(0xFFECEEF4)
val LightSleekLavender = Color(0xFF6750A4)
val LightSleekDarkPurple = Color(0xFFEADDFF)
val LightSleekLavenderLight = Color(0xFFF3EDF7)
val LightEmeraldGreen = Color(0xFF15803D)
val LightEmeraldGreenDark = Color(0xFF0D5326)
val LightCrimsonRed = Color(0xFFDC2626)
val LightCrimsonRedDark = Color(0xFF991B1B)
val LightAmberGold = Color(0xFFB45309)
val LightAiCardBg = Color(0xFFF0FDF4)
val LightAiCardBorder = Color(0xFFBBF7D0)
val LightTextPrimary = Color(0xFF191C24)
val LightTextSecondary = Color(0xFF555966)
val LightTextMuted = Color(0xFF7F8494)

// Legacy compatibility
val LightBackground = LightNavyDeep
val LightSurface = LightNavySurface
val LightCard = LightNavyCard
val LightBorder = LightNavyBorder

// Dynamic Theme-Aware Color Tokens
// These resolve dynamically based on the active ThemeProvider mode!
val NavyDeep: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.canvasBackground

val NavySurface: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.surface

val NavyCard: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.cardBackground

val NavyCardElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.cardElevated

val NavyCardSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.cardSubtle

val NavyBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.border

val NavyBorderSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.borderSubtle

val SleekLavender: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.primaryAccent

val SleekDarkPurple: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.primaryContainer

val SleekLavenderLight: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.accentGlow

val CyanAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.primaryAccent

val CyanGlow: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.accentGlow

val PurpleAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.primaryAccent

val EmeraldGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.bullishGreen

val EmeraldGreenDark: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.bullishGreenDark

val CrimsonRed: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.bearishRed

val CrimsonRedDark: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.bearishRedDark

val AmberGold: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.neutralAmber

val AiCardBg: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.aiCardBg

val AiCardBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.aiCardBorder

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = TradingTheme.colors.textMuted

// Account Mode Accents (#00C853 for Live, #2979FF for Paper)
val LiveMarketGreen = Color(0xFF00C853)
val PaperTradingBlue = Color(0xFF2979FF)


