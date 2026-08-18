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
                                color = Color(0xFFD5C8FF),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text("✨", style = TextStyle(fontSize = 16.sp))
                    }

                    Spacer(GlanceModifier.height(10.dp))

                    if (reminders.isEmpty()) {
                        Text(
                            if (persian) "🌱 فعلاً یادآور نزدیکی نیست" else "🌱 Nothing urgent right now",
                            style = TextStyle(
                                color = Color(0xFFE8E1EC),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    reminders.forEachIndexed { index, reminder ->
                        if (index > 0) Spacer(GlanceModifier.height(4.dp))
                        Row(GlanceModifier.fillMaxWidth().padding(vertical = 5.dp)) {
                            Text(
                                if (index == 0) "●" else "•",
                                style = TextStyle(
                                    color = if (index == 0) Color(0xFFFF8EB5) else Color(0xFF75D6ED),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(GlanceModifier.width(7.dp))
                            Text(
                                reminder.title,
                                modifier = GlanceModifier.defaultWeight(),
                                style = TextStyle(
                                    color = Color(0xFFE8E1EC),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            Text(
                                CountdownFormatter.compact(reminder.dueAtMillis, persian = persian),
                                style = TextStyle(
                                    color = Color(0xFFD5C8FF),
                                    fontSize = 13.sp,
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
