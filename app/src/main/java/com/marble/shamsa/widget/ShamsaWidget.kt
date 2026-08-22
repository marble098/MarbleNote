package com.marble.shamsa.widget

import android.content.Context
import android.content.res.Configuration
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
    val text: Color,
    val muted: Color,
    val primary: Color,
    val track: Color
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
                    text = Color(0xFFF5F7FB),
                    muted = Color(0xFFAAB6C8),
                    primary = Color(0xFF91B2FF),
                    track = Color(0xFF2A3648)
                )
            } else {
                WidgetPalette(
                    background = Color(0xFFF7F8FC),
                    card = Color(0xFFFFFFFF),
                    featured = Color(0xFFEEF4FF),
                    text = Color(0xFF172033),
                    muted = Color(0xFF667085),
                    primary = Color(0xFF2457D6),
                    track = Color(0xFFDDE4EE)
                )
            }

        provideContent {
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
                            color = palette.text,
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
                            color = palette.primary,
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
                                color = palette.text,
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
                                color = palette.muted,
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
                            palette.primary
                        else
                            Color(reminder.colorArgb),
                    fontSize = 10.sp
                )
            )

            Spacer(GlanceModifier.width(7.dp))

            Text(
                reminder.title,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = palette.text,
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
            style = style,
            palette = palette
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
            )

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
    style: CountdownStyle,
    palette: WidgetPalette
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
                    color = palette.primary,
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
                    color = palette.primary,
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
                    color = palette.primary,
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
                    color = palette.primary,
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
                    color = palette.muted,
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
    return (remaining.toDouble() / total.toDouble())
        .toFloat()
        .coerceIn(0f, 1f)
}

class ShamsaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget =
        ShamsaWidget()
}
