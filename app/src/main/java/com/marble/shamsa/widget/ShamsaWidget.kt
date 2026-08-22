package com.marble.shamsa.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.material3.ColorProviders
import androidx.glance.text.*
import com.marble.shamsa.core.data.ReminderRepository
import com.marble.shamsa.core.data.SettingsStore
import com.marble.shamsa.core.model.CountdownStyle
import com.marble.shamsa.core.model.Reminder
import com.marble.shamsa.core.model.ThemeMode
import com.marble.shamsa.core.time.CountdownFormatter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlin.math.ceil

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun repository(): ReminderRepository
    fun settings(): SettingsStore
}

private data class WidgetPalette(
    val background: Color,
    val card: Color,
    val featured: Color,
    val primary: Color,
    val track: Color
)

private val WidgetLightColors = lightColorScheme(
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
    outline = Color(0xFFCBD5E1)
)

private val WidgetDarkColors = darkColorScheme(
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
    onSurfaceVariant = Color(0xFFAAB6C8),
    outline = Color(0xFF566273)
)

class ShamsaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val entry = EntryPointAccessors.fromApplication(
            context,
            WidgetEntryPoint::class.java
        )
        val reminders = entry.repository().upcoming(3)
        val appSettings = entry.settings().settings.first()
        val persian = appSettings.language == "fa"
        val now = System.currentTimeMillis()

        val systemDark =
            context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

        val dark = when (appSettings.themeMode) {
            ThemeMode.SYSTEM -> systemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        val palette =
            if (dark) {
                WidgetPalette(
                    background = Color(0xFF0B1220),
                    card = Color(0xFF151E2E),
                    featured = Color(0xFF17284A),
                    primary = Color(0xFF91B2FF),
                    track = Color(0xFF2A3648)
                )
            } else {
                WidgetPalette(
                    background = Color(0xFFF7F8FC),
                    card = Color(0xFFFFFFFF),
                    featured = Color(0xFFEEF4FF),
                    primary = Color(0xFF2457D6),
                    track = Color(0xFFDDE4EE)
                )
            }

        val glanceColors = ColorProviders(
            if (dark) WidgetDarkColors else WidgetLightColors
        )

        provideContent {
            GlanceTheme(colors = glanceColors) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(palette.background)
                        .padding(14.dp),
                    verticalAlignment = Alignment.Vertical.Top
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Text(
                            if (persian) "شمسا" else "Shamsa",
                            modifier = GlanceModifier.defaultWeight(),
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            if (persian)
                                "زمانِ پیش‌رو"
                            else
                                "UP NEXT",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(GlanceModifier.height(9.dp))

                    if (reminders.isEmpty()) {
                        Column(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(palette.card)
                                .padding(12.dp)
                        ) {
                            Text(
                                if (persian)
                                    "فعلاً چیزی نزدیک نیست"
                                else
                                    "Nothing close right now",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(GlanceModifier.height(4.dp))

                            Text(
                                if (persian)
                                    "فضایت خلوت است."
                                else
                                    "Your timeline is clear.",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    reminders.forEachIndexed { index, reminder ->
                        if (index > 0) {
                            Spacer(GlanceModifier.height(7.dp))
                        }

                        WidgetReminder(
                            reminder = reminder,
                            now = now,
                            persian = persian,
                            style =
                                if (index == 0)
                                    appSettings.countdownStyle
                                else
                                    CountdownStyle.COMPACT,
                            palette = palette,
                            featured = index == 0
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetReminder(
    reminder: Reminder,
    now: Long,
    persian: Boolean,
    style: CountdownStyle,
    palette: WidgetPalette,
    featured: Boolean
) {
    val progress = reminderProgress(reminder, now)

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(
                if (featured)
                    palette.featured
                else
                    palette.card
            )
            .padding(
                horizontal = 10.dp,
                vertical = if (featured) 9.dp else 8.dp
            )
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Text(
                "●",
                style = TextStyle(
                    color =
                        if (featured)
                            GlanceTheme.colors.primary
                        else
                            GlanceTheme.colors.secondary,
                    fontSize = 10.sp
                )
            )

            Spacer(GlanceModifier.width(7.dp))

            Text(
                reminder.title,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize =
                        if (featured) 14.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }

        Spacer(GlanceModifier.height(5.dp))

        WidgetProgressStrip(
            progress = progress,
            palette = palette
        )

        Spacer(GlanceModifier.height(5.dp))

        WidgetCountdown(
            dueAtMillis = reminder.dueAtMillis,
            now = now,
            persian = persian,
            style = style
        )
    }
}

@Composable
private fun WidgetProgressStrip(
    progress: Float,
    palette: WidgetPalette
) {
    val count = 10
    val filled = ceil(progress * count)
        .toInt()
        .coerceIn(0, count)

    Row(
        modifier = GlanceModifier.fillMaxWidth()
    ) {
        repeat(count) { index ->
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(5.dp)
                    .background(
                        if (index < filled)
                            palette.primary
                        else
                            palette.track
                    )
            ) {}

            if (index < count - 1) {
                Spacer(GlanceModifier.width(3.dp))
            }
        }
    }
}

@Composable
private fun WidgetCountdown(
    dueAtMillis: Long,
    now: Long,
    persian: Boolean,
    style: CountdownStyle
) {
    when (style) {
        CountdownStyle.COMPACT -> {
            Text(
                CountdownFormatter.compact(
                    dueAtMillis,
                    now,
                    persian
                ),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        CountdownStyle.DIGITAL -> {
            Text(
                CountdownFormatter.digital(
                    dueAtMillis,
                    now,
                    persian
                ),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        CountdownStyle.SEGMENTS -> {
            Text(
                CountdownFormatter.segments(
                    dueAtMillis,
                    now,
                    persian
                ),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        CountdownStyle.FOCUS -> {
            Text(
                CountdownFormatter.focusPrimary(
                    dueAtMillis,
                    now,
                    persian
                ),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                CountdownFormatter.focusSecondary(
                    dueAtMillis,
                    now,
                    persian
                ),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 9.sp
                )
            )
        }
    }
}

private fun reminderProgress(
    reminder: Reminder,
    now: Long
): Float {
    val total =
        (reminder.dueAtMillis - reminder.createdAtMillis)
            .coerceAtLeast(1L)
    val remaining =
        (reminder.dueAtMillis - now)
            .coerceIn(0L, total)

    return (
        remaining.toDouble() /
            total.toDouble()
        )
        .toFloat()
        .coerceIn(0f, 1f)
}

class ShamsaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget =
        ShamsaWidget()
}
