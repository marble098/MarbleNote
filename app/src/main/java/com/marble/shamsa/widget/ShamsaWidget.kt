package com.marble.shamsa.widget

import android.content.Context
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
import com.marble.shamsa.core.time.CountdownFormatter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun repository(): ReminderRepository
    fun settings(): SettingsStore
}

class ShamsaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val reminders = entry.repository().upcoming(3)
        val appSettings = entry.settings().settings.first()
        val persian = appSettings.language == "fa"
        val countdownStyle = appSettings.countdownStyle
        val now = System.currentTimeMillis()

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF18132B))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Vertical.Top
                ) {
                    Row(GlanceModifier.fillMaxWidth()) {
                        Text("☀️", style = TextStyle(fontSize = 20.sp))
                        Spacer(GlanceModifier.width(8.dp))
                        Text(
                            if (persian) "شمسا" else "Shamsa",
                            modifier = GlanceModifier.defaultWeight(),
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text("⏳", style = TextStyle(fontSize = 16.sp))
                    }

                    Spacer(GlanceModifier.height(9.dp))

                    if (reminders.isEmpty()) {
                        Text(
                            if (persian) "🌱 فعلاً یادآور نزدیکی نیست"
                            else "🌱 Nothing urgent right now",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    reminders.forEachIndexed { index, reminder ->
                        if (index > 0) Spacer(GlanceModifier.height(7.dp))

                        Column(
                            GlanceModifier
                                .fillMaxWidth()
                                .background(
                                    if (index == 0) Color(0xFF28203F)
                                    else Color(0xFF201A31)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(GlanceModifier.fillMaxWidth()) {
                                Text(
                                    if (index == 0) "●" else "•",
                                    style = TextStyle(
                                        color = if (index == 0)
                                            GlanceTheme.colors.secondary
                                        else
                                            GlanceTheme.colors.tertiary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(GlanceModifier.width(7.dp))
                                Text(
                                    reminder.title,
                                    modifier = GlanceModifier.defaultWeight(),
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                            }

                            Spacer(GlanceModifier.height(3.dp))
                            WidgetCountdown(
                                dueAtMillis = reminder.dueAtMillis,
                                now = now,
                                persian = persian,
                                style = if (index == 0)
                                    countdownStyle
                                else
                                    CountdownStyle.COMPACT
                            )
                        }
                    }
                }
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
                CountdownFormatter.compact(dueAtMillis, now, persian),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        CountdownStyle.DIGITAL -> {
            Text(
                CountdownFormatter.digital(dueAtMillis, now, persian),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                CountdownFormatter.digitalLegend(dueAtMillis, now, persian),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 9.sp
                )
            )
        }

        CountdownStyle.SEGMENTS -> {
            Text(
                CountdownFormatter.segments(dueAtMillis, now, persian),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        CountdownStyle.FOCUS -> {
            Text(
                CountdownFormatter.focusPrimary(dueAtMillis, now, persian),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                CountdownFormatter.focusSecondary(dueAtMillis, now, persian),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }
    }
}

class ShamsaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ShamsaWidget()
}
