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

// SHAMSA_DESIGN_V6_AIRY
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_extrabold, FontWeight.ExtraBold)
)

/*
 * V6 intentionally avoids the old purple/grey wash.
 * Neutral surfaces + cobalt + teal + amber keep the app energetic
 * without tinting every large surface.
 */
private val Light = lightColorScheme(
    primary = Color(0xFF2457D6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAF0FF),
    onPrimaryContainer = Color(0xFF102B66),

    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF7F2),
    onSecondaryContainer = Color(0xFF0B4F49),

    tertiary = Color(0xFFE58A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF1CF),
    onTertiaryContainer = Color(0xFF684200),

    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF172033),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF172033),
    surfaceVariant = Color(0xFFEEF1F6),
    onSurfaceVariant = Color(0xFF667085),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE3E8EF),

    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF7F1D1D)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF91B2FF),
    onPrimary = Color(0xFF08265D),
    primaryContainer = Color(0xFF173B7D),
    onPrimaryContainer = Color(0xFFDCE7FF),

    secondary = Color(0xFF62DED1),
    onSecondary = Color(0xFF003D38),
    secondaryContainer = Color(0xFF0D514B),
    onSecondaryContainer = Color(0xFFD3FBF6),

    tertiary = Color(0xFFFFC861),
    onTertiary = Color(0xFF4B3000),
    tertiaryContainer = Color(0xFF664500),
    onTertiaryContainer = Color(0xFFFFE3A7),

    background = Color(0xFF0B1220),
    onBackground = Color(0xFFF5F7FB),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF5F7FB),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFC7D0DD),
    outline = Color(0xFF566273),
    outlineVariant = Color(0xFF293446),

    error = Color(0xFFFF8A8A),
    onError = Color(0xFF680008),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFFDADA)
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

/*
 * Deliberately ~10–20% smaller than v5.
 * Large Persian text needs breathing room, not oversized type.
 */
private val AppTypography = Typography(
    displayLarge = style(FontWeight.ExtraBold, 36, 46),
    displayMedium = style(FontWeight.ExtraBold, 31, 40),
    displaySmall = style(FontWeight.Bold, 27, 36),

    headlineLarge = style(FontWeight.ExtraBold, 25, 34),
    headlineMedium = style(FontWeight.Bold, 21, 30),
    headlineSmall = style(FontWeight.Bold, 18, 27),

    titleLarge = style(FontWeight.Bold, 18, 26),
    titleMedium = style(FontWeight.SemiBold, 15, 23),
    titleSmall = style(FontWeight.SemiBold, 13, 20),

    bodyLarge = style(FontWeight.Normal, 14, 23),
    bodyMedium = style(FontWeight.Normal, 13, 21),
    bodySmall = style(FontWeight.Normal, 11, 18),

    labelLarge = style(FontWeight.SemiBold, 13, 19),
    labelMedium = style(FontWeight.Medium, 11, 17),
    labelSmall = style(FontWeight.Medium, 10, 15)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
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
