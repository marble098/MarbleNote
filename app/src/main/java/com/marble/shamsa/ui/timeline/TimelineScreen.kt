package com.marble.shamsa.ui.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.marble.shamsa.core.design.IconCatalog
import com.marble.shamsa.core.model.Reminder
import com.marble.shamsa.core.model.ReminderStatus
import com.marble.shamsa.core.time.JalaliCalendar

@Composable
fun TimelineScreen(reminders: List<Reminder>, persian: Boolean, onOpen: (String) -> Unit) {
    val groups = reminders
        .filter { it.status == ReminderStatus.ACTIVE }
        .groupBy { JalaliCalendar.fromEpochMillis(it.dueAtMillis) }
        .toList()
        .sortedBy { it.first.let { d -> d.year * 10000 + d.month * 100 + d.day } }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("🗓️", style = MaterialTheme.typography.displaySmall)
            Text(if (persian) "خط زمان" else "Timeline", style = MaterialTheme.typography.headlineLarge)
            Text(
                if (persian) "قرارها و یادآورها، مرتب و یک‌جا." else "Everything ahead, clearly organized.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }

        if (groups.isEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f)
                    )
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌤️", style = MaterialTheme.typography.displaySmall)
                        Text(
                            if (persian) "چیزی در صف نیست" else "Your horizon is clear",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }

        groups.forEach { (date, list) ->
            item(key = "h-${date.year}-${date.month}-${date.day}") {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .7f)
                ) {
                    Text(
                        "${date.day} ${JalaliCalendar.monthName(date.month, persian)} ${date.year}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            items(list, key = { it.id }) { reminder ->
                val accent = Color(reminder.colorArgb)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpen(reminder.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = accent.copy(alpha = .14f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(IconCatalog.icon(reminder.icon), null, tint = accent)
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                            if (reminder.notes.isNotBlank()) {
                                Text(
                                    reminder.notes,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}
