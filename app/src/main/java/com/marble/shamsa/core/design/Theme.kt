package com.marble.shamsa.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.marble.shamsa.core.model.ThemeMode

private val Light = lightColorScheme(
    primary = Color(0xFF6D4AFF),
    onPrimary = Color.White,
    secondary = Color(0xFFFF4F9A),
    tertiary = Color(0xFF00A7C7),
    background = Color(0xFFF9F7FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEFEAFB)
)
private val Dark = darkColorScheme(
    primary = Color(0xFFB9A7FF),
    secondary = Color(0xFFFF91BE),
    tertiary = Color(0xFF72D8EE),
    background = Color(0xFF111018),
    surface = Color(0xFF191720),
    surfaceVariant = Color(0xFF292532)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 34.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
)

@Composable
fun ShamsaTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true }
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = AppTypography, content = content)
}
