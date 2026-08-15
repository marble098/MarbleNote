package com.marble.shamsa.ui.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marble.shamsa.core.model.Reminder
import com.marble.shamsa.core.model.ReminderStatus
import com.marble.shamsa.core.time.JalaliCalendar

@Composable
fun TimelineScreen(reminders: List<Reminder>, persian: Boolean, onOpen: (String) -> Unit) {
    val groups = reminders.filter { it.status == ReminderStatus.ACTIVE }.groupBy { JalaliCalendar.fromEpochMillis(it.dueAtMillis) }.toList().sortedBy { it.first.let { d -> d.year * 10000 + d.month * 100 + d.day } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        groups.forEach { (date, list) ->
            item(key = "h-${date.year}-${date.month}-${date.day}") {
                Text("${date.day} ${JalaliCalendar.monthName(date.month, persian)} ${date.year}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            items(list, key = { it.id }) { r ->
                ListItem(headlineContent = { Text(r.title) }, supportingContent = { if (r.notes.isNotBlank()) Text(r.notes, maxLines = 1) }, leadingContent = { Icon(Icons.Rounded.Event, null) }, modifier = Modifier.fillMaxWidth().clickable { onOpen(r.id) })
                HorizontalDivider()
            }
        }
    }
}
