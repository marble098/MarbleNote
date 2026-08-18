package com.marble.shamsa.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marble.shamsa.R
import com.marble.shamsa.core.model.ThemeMode

// SHAMSA_DESIGN_V3
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_extrabold, FontWeight.ExtraBold),
    Font(R.font.vazirmatn_black, FontWeight.Black)
)

private val Light = lightColorScheme(
    primary = Color(0xFF5B3FD6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E0FF),
    onPrimaryContainer = Color(0xFF21105D),
    secondary = Color(0xFFD93D78),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E5),
    onSecondaryContainer = Color(0xFF53102B),
    tertiary = Color(0xFF008DA7),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC0F0FA),
    onTertiaryContainer = Color(0xFF003640),
    background = Color(0xFFFBF9FF),
    onBackground = Color(0xFF1C1B20),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B20),
    surfaceVariant = Color(0xFFF0ECF8),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A7481),
    error = Color(0xFFBA1A1A)
)

private val Dark = darkColorScheme(
    primary = Color(0xFFCBBEFF),
    onPrimary = Color(0xFF2E167F),
    primaryContainer = Color(0xFF4330A6),
    onPrimaryContainer = Color(0xFFE8E0FF),
    secondary = Color(0xFFFFB1C8),
    onSecondary = Color(0xFF7D1749),
    secondaryContainer = Color(0xFF9D2B60),
    onSecondaryContainer = Color(0xFFFFD9E5),
    tertiary = Color(0xFF75D6ED),
    onTertiary = Color(0xFF003640),
    tertiaryContainer = Color(0xFF00505E),
    onTertiaryContainer = Color(0xFFC0F0FA),
    background = Color(0xFF100E17),
    onBackground = Color(0xFFE8E1EC),
    surface = Color(0xFF17141F),
    onSurface = Color(0xFFE8E1EC),
    surfaceVariant = Color(0xFF2B2733),
    onSurfaceVariant = Color(0xFFCEC5D3),
    outline = Color(0xFF978E9D),
    error = Color(0xFFFFB4AB)
)

private fun style(weight: FontWeight, size: Int, line: Int) = TextStyle(
    fontFamily = VazirmatnFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp
)

private val AppTypography = Typography(
    displayLarge = style(FontWeight.Black, 48, 58),
    displayMedium = style(FontWeight.ExtraBold, 40, 50),
    displaySmall = style(FontWeight.ExtraBold, 34, 43),
    headlineLarge = style(FontWeight.Black, 32, 41),
    headlineMedium = style(FontWeight.ExtraBold, 27, 36),
    headlineSmall = style(FontWeight.Bold, 23, 31),
    titleLarge = style(FontWeight.Bold, 21, 29),
    titleMedium = style(FontWeight.SemiBold, 17, 25),
    titleSmall = style(FontWeight.SemiBold, 15, 22),
    bodyLarge = style(FontWeight.Normal, 16, 25),
    bodyMedium = style(FontWeight.Normal, 14, 22),
    bodySmall = style(FontWeight.Normal, 12, 19),
    labelLarge = style(FontWeight.SemiBold, 14, 20),
    labelMedium = style(FontWeight.Medium, 12, 18),
    labelSmall = style(FontWeight.Medium, 11, 16)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun ShamsaTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
