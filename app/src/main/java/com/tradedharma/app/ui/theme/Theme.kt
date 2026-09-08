package com.tradedharma.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED_BLACK }

private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E8FF),
    onPrimaryContainer = Color(0xFF001A3A),
    secondary = Color(0xFF5F6368),
    onSecondary = Color.White,
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7E8EB),
    onBackground = Color(0xFF17191C),
    onSurface = Color(0xFF17191C),
    error = Color(0xFFD32F2F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80BFFF),
    onPrimary = Color(0xFF002F5E),
    primaryContainer = Color(0xFF164A7A),
    onPrimaryContainer = Color(0xFFD7E9FF),
    secondary = Color(0xFFB8BDC4),
    onSecondary = Color(0xFF1B1D20),
    background = Color(0xFF121416),
    surface = Color(0xFF181A1E),
    surfaceVariant = Color(0xFF272A30),
    onBackground = Color(0xFFE9EAED),
    onSurface = Color(0xFFE9EAED),
    outline = Color(0xFF70757D),
    outlineVariant = Color(0xFF3E4248),
    error = Color(0xFFFF8A80)
)

private val AmoledColors = darkColorScheme(
    primary = Color(0xFF80BFFF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0B2A48),
    onPrimaryContainer = Color(0xFFD7E9FF),
    secondary = Color(0xFFBFC4CB),
    onSecondary = Color.Black,
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF101114),
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0xFF777B82),
    outlineVariant = Color(0xFF34373C),
    error = Color(0xFFFF8A80)
)

private val TradeDharmaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun TradeDharmaTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED_BLACK -> true
    }
    val colors = when (themeMode) {
        ThemeMode.AMOLED_BLACK -> AmoledColors
        ThemeMode.DARK -> DarkColors
        ThemeMode.LIGHT -> LightColors
        ThemeMode.SYSTEM -> if (isDark) DarkColors else LightColors
    }
    MaterialTheme(colorScheme = colors, shapes = TradeDharmaShapes, content = content)
}
