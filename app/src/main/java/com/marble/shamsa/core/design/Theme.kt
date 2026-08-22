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

// SHAMSA_DESIGN_V5_SOFT_GEOMETRY
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_extrabold, FontWeight.ExtraBold),
    Font(R.font.vazirmatn_black, FontWeight.Black)
)

private val Light = lightColorScheme(
    primary = Color(0xFF5843D8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E2FF),
    onPrimaryContainer = Color(0xFF21105D),
    secondary = Color(0xFFC53F78),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E7),
    onSecondaryContainer = Color(0xFF53102B),
    tertiary = Color(0xFF008A9D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC3F0F6),
    onTertiaryContainer = Color(0xFF00363E),
    background = Color(0xFFFCFAFF),
    onBackground = Color(0xFF1C1B20),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B20),
    surfaceVariant = Color(0xFFF1EDF7),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF7A7481),
    error = Color(0xFFBA1A1A)
)

private val Dark = darkColorScheme(
    primary = Color(0xFFCCBEFF),
    onPrimary = Color(0xFF2E167F),
    primaryContainer = Color(0xFF4330A6),
    onPrimaryContainer = Color(0xFFE8E0FF),
    secondary = Color(0xFFFFB1C9),
    onSecondary = Color(0xFF7D1749),
    secondaryContainer = Color(0xFF87274F),
    onSecondaryContainer = Color(0xFFFFD9E5),
    tertiary = Color(0xFF79D7E7),
    onTertiary = Color(0xFF00363E),
    tertiaryContainer = Color(0xFF07515B),
    onTertiaryContainer = Color(0xFFC3F0F6),
    background = Color(0xFF100E16),
    onBackground = Color(0xFFE9E2EC),
    surface = Color(0xFF17141E),
    onSurface = Color(0xFFE9E2EC),
    surfaceVariant = Color(0xFF2B2732),
    onSurfaceVariant = Color(0xFFCFC5D2),
    outline = Color(0xFF978E9D),
    error = Color(0xFFFFB4AB)
)

private fun style(
    weight: FontWeight,
    size: Int,
    line: Int
) = TextStyle(
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
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(38.dp)
)

@Composable
fun ShamsaTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
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
