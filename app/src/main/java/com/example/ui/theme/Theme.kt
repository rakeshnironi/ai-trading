package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val displayName: String) {
    SYSTEM("System"),
    DARK("Dark"),
    LIGHT("Light")
}

data class TradingColors(
    val canvasBackground: Color,
    val surface: Color,
    val cardBackground: Color,
    val cardElevated: Color,
    val cardSubtle: Color,
    val border: Color,
    val borderSubtle: Color,
    val primaryAccent: Color,
    val primaryContainer: Color,
    val accentGlow: Color,
    val bullishGreen: Color,
    val bullishGreenDark: Color,
    val bearishRed: Color,
    val bearishRedDark: Color,
    val neutralAmber: Color,
    val aiCardBg: Color,
    val aiCardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean
)

val DarkTradingColors = TradingColors(
    canvasBackground = DarkNavyDeep,
    surface = DarkNavySurface,
    cardBackground = DarkNavyCard,
    cardElevated = DarkNavyCardElevated,
    cardSubtle = DarkNavyCardSubtle,
    border = DarkNavyBorder,
    borderSubtle = DarkNavyBorderSubtle,
    primaryAccent = DarkSleekLavender,
    primaryContainer = DarkSleekDarkPurple,
    accentGlow = DarkSleekLavenderLight,
    bullishGreen = DarkEmeraldGreen,
    bullishGreenDark = DarkEmeraldGreenDark,
    bearishRed = DarkCrimsonRed,
    bearishRedDark = DarkCrimsonRedDark,
    neutralAmber = DarkAmberGold,
    aiCardBg = DarkAiCardBg,
    aiCardBorder = DarkAiCardBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    isDark = true
)

val LightTradingColors = TradingColors(
    canvasBackground = LightNavyDeep,
    surface = LightNavySurface,
    cardBackground = LightNavyCard,
    cardElevated = LightNavyCardElevated,
    cardSubtle = LightNavyCardSubtle,
    border = LightNavyBorder,
    borderSubtle = LightNavyBorderSubtle,
    primaryAccent = LightSleekLavender,
    primaryContainer = LightSleekDarkPurple,
    accentGlow = LightSleekLavenderLight,
    bullishGreen = LightEmeraldGreen,
    bullishGreenDark = LightEmeraldGreenDark,
    bearishRed = LightCrimsonRed,
    bearishRedDark = LightCrimsonRedDark,
    neutralAmber = LightAmberGold,
    aiCardBg = LightAiCardBg,
    aiCardBorder = LightAiCardBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    isDark = false
)

val LocalTradingColors = staticCompositionLocalOf { DarkTradingColors }

object TradingTheme {
    val colors: TradingColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTradingColors.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalTradingColors.current.isDark
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkSleekLavender,
    onPrimary = DarkSleekDarkPurple,
    primaryContainer = DarkSleekDarkPurple,
    onPrimaryContainer = DarkSleekLavender,
    secondary = DarkEmeraldGreen,
    onSecondary = DarkSleekDarkPurple,
    secondaryContainer = DarkAiCardBg,
    onSecondaryContainer = DarkEmeraldGreen,
    tertiary = DarkAmberGold,
    onTertiary = DarkSleekDarkPurple,
    error = DarkCrimsonRed,
    onError = Color(0xFF601410),
    background = DarkNavyDeep,
    onBackground = DarkTextPrimary,
    surface = DarkNavySurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkNavyBorder,
    outlineVariant = DarkNavyBorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = LightSleekLavender,
    onPrimary = Color.White,
    primaryContainer = LightSleekDarkPurple,
    onPrimaryContainer = LightSleekLavender,
    secondary = LightEmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = LightEmeraldGreenDark,
    tertiary = LightAmberGold,
    onTertiary = Color.White,
    error = LightCrimsonRed,
    onError = Color.White,
    background = LightNavyDeep,
    onBackground = LightTextPrimary,
    surface = LightNavySurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightNavyCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightNavyBorder,
    outlineVariant = LightNavyBorderSubtle
)

@Composable
fun AppThemeProvider(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    AutonomousTradingTheme(
        darkTheme = isDark,
        content = content
    )
}

@Composable
fun AutonomousTradingTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val tradingColors = if (darkTheme) DarkTradingColors else LightTradingColors

    CompositionLocalProvider(
        LocalTradingColors provides tradingColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


