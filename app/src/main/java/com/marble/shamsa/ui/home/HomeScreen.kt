package com.marble.shamsa.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.marble.shamsa.R
import com.marble.shamsa.core.design.ReminderCard
import com.marble.shamsa.core.model.*
import com.marble.shamsa.core.time.JalaliCalendar
import java.time.ZoneId

@Composable
fun HomeScreen(
    reminders: List<Reminder>, query: String, filter: ReminderFilter, sort: ReminderSort, display: DisplayMode,
    persian: Boolean, onQuery: (String) -> Unit, onFilter: (ReminderFilter) -> Unit, onSort: (ReminderSort) -> Unit,
    onOpen: (String) -> Unit, onComplete: (String) -> Unit
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { delay(60_000L); now = System.currentTimeMillis() }
    }
    val today = JalaliCalendar.fromEpochMillis(now)
    val shown = remember(reminders, query, filter, sort, now / 60_000) {
        reminders.asSequence().filter { r ->
            val q = query.trim()
            (q.isBlank() || r.title.contains(q, true) || r.notes.contains(q, true)) && when (filter) {
                ReminderFilter.ALL -> r.deletedAtMillis == null
                ReminderFilter.COMPLETED -> r.status == ReminderStatus.COMPLETED
                ReminderFilter.UPCOMING -> r.status == ReminderStatus.ACTIVE && r.dueAtMillis >= now
                ReminderFilter.TODAY -> JalaliCalendar.fromEpochMillis(r.dueAtMillis) == today
            }
        }.sortedWith(when (sort) {
            ReminderSort.DUE -> compareBy { it.dueAtMillis }
            ReminderSort.CREATED -> compareByDescending { it.createdAtMillis }
            ReminderSort.PRIORITY -> compareByDescending<Reminder> { it.priority.level }.thenBy { it.dueAtMillis }
        }).toList()
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            val date = JalaliCalendar.fromEpochMillis(now)
            Text(if (persian) "${date.day} ${JalaliCalendar.monthName(date.month, true)} ${date.year}" else "${date.day} ${JalaliCalendar.monthName(date.month, false)} ${date.year}", color = MaterialTheme.colorScheme.primary)
            Text(if (persian) "یادآورهای من" else "My countdowns", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Rounded.Search, null) }, placeholder = { Text(stringResource(R.string.search)) }, singleLine = true)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReminderFilter.entries.forEach { f ->
                        AssistChip(
                            onClick = { onFilter(f) },
                            label = { Text(when(f){ReminderFilter.ALL->stringResource(R.string.filter_all);ReminderFilter.TODAY->stringResource(R.string.filter_today);ReminderFilter.UPCOMING->stringResource(R.string.filter_upcoming);ReminderFilter.COMPLETED->stringResource(R.string.filter_completed)}) },
                            leadingIcon = if (f == filter) { { Icon(Icons.Rounded.Done, null, Modifier.size(18.dp)) } } else null
                        )
                    }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (persian) "مرتب‌سازی:" else "Sort:", modifier = Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ReminderSort.entries.forEach { option ->
                        FilterChip(
                            selected = option == sort,
                            onClick = { onSort(option) },
                            label = { Text(sortLabel(option, persian)) }
                        )
                    }
                }
            }
        }
        if (shown.isEmpty()) item {
            Column(Modifier.fillParentMaxHeight(.6f).fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.no_reminders), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.no_reminders_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else items(shown, key = { it.id }) { reminder ->
            ReminderCard(reminder, persian, onClick = { onOpen(reminder.id) }, onComplete = { onComplete(reminder.id) }, displayMode = display)
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

private fun sortLabel(value: ReminderSort, persian: Boolean): String = if (persian) when (value) {
    ReminderSort.DUE -> "زمان"
    ReminderSort.PRIORITY -> "اولویت"
    ReminderSort.CREATED -> "ایجاد"
} else when (value) {
    ReminderSort.DUE -> "Due"
    ReminderSort.PRIORITY -> "Priority"
    ReminderSort.CREATED -> "Created"
}
