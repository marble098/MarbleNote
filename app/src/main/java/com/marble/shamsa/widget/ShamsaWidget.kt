package com.marble.shamsa.widget

import android.content.Context
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
        val persian = entry.settings().settings.first().language == "fa"

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF201A32))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Vertical.Top
                ) {
                    Text(
                        if (persian) "شمسا" else "Shamsa",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.height(10.dp))

                    if (reminders.isEmpty()) {
                        Text(
                            if (persian) "یادآور نزدیکی نیست" else "No upcoming reminders",
                            style = TextStyle(color = GlanceTheme.colors.onSurface)
                        )
                    }

                    reminders.forEach { reminder ->
                        Row(GlanceModifier.fillMaxWidth().padding(vertical = 5.dp)) {
                            Text(
                                reminder.title,
                                modifier = GlanceModifier.defaultWeight(),
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurface,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                            Text(
                                CountdownFormatter.compact(reminder.dueAtMillis, persian = persian),
                                style = TextStyle(
                                    color = GlanceTheme.colors.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class ShamsaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ShamsaWidget()
}
