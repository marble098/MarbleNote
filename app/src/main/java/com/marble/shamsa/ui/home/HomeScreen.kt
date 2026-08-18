package com.marble.shamsa.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marble.shamsa.R
import com.marble.shamsa.core.design.ReminderCard
import com.marble.shamsa.core.model.*
import com.marble.shamsa.core.time.JalaliCalendar
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    reminders: List<Reminder>,
    query: String,
    filter: ReminderFilter,
    sort: ReminderSort,
    display: DisplayMode,
    countdownStyle: CountdownStyle,
    persian: Boolean,
    onQuery: (String) -> Unit,
    onFilter: (ReminderFilter) -> Unit,
    onSort: (ReminderSort) -> Unit,
    onOpen: (String) -> Unit,
    onComplete: (String) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            now = System.currentTimeMillis()
        }
    }

    val today = JalaliCalendar.fromEpochMillis(now)
    val shown = remember(reminders, query, filter, sort, now / 60_000) {
        reminders.asSequence()
            .filter { it.deletedAtMillis == null }
            .filter { r ->
                val q = query.trim()
                (q.isBlank() || r.title.contains(q, true) || r.notes.contains(q, true)) &&
                    when (filter) {
                        ReminderFilter.ALL -> true
                        ReminderFilter.COMPLETED -> r.status == ReminderStatus.COMPLETED
                        ReminderFilter.CANCELED -> r.status == ReminderStatus.CANCELED
                        ReminderFilter.UPCOMING ->
                            r.status == ReminderStatus.ACTIVE && r.dueAtMillis >= now
                        ReminderFilter.TODAY ->
                            r.status == ReminderStatus.ACTIVE &&
                                JalaliCalendar.fromEpochMillis(r.dueAtMillis) == today
                    }
            }
            .sortedWith(
                when (sort) {
                    ReminderSort.DUE -> compareBy { it.dueAtMillis }
                    ReminderSort.CREATED -> compareByDescending { it.createdAtMillis }
                    ReminderSort.PRIORITY ->
                        compareByDescending<Reminder> { it.priority.level }
                            .thenBy { it.dueAtMillis }
                }
            )
            .toList()
    }

    val upcomingCount = reminders.count {
        it.deletedAtMillis == null &&
            it.status == ReminderStatus.ACTIVE &&
            it.dueAtMillis >= now
    }
    val todayCount = reminders.count {
        it.deletedAtMillis == null &&
            it.status == ReminderStatus.ACTIVE &&
            JalaliCalendar.fromEpochMillis(it.dueAtMillis) == today
    }
    val completedCount = reminders.count {
        it.deletedAtMillis == null && it.status == ReminderStatus.COMPLETED
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            val date = JalaliCalendar.fromEpochMillis(now)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .78f)
                                )
                            )
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                if (persian) "امروز، وقتِ چیزهای مهمه" else "Make today count",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                if (persian) {
                                    "${date.day} ${JalaliCalendar.monthName(date.month, true)} ${date.year}"
                                } else {
                                    "${date.day} ${JalaliCalendar.monthName(date.month, false)} ${date.year}"
                                },
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Text(
                        if (persian) "یادآورهای من" else "My countdowns",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill(
                            "☀️",
                            todayCount,
                            if (persian) "امروز" else "Today",
                            Modifier.weight(1f)
                        )
                        StatPill(
                            "⏳",
                            upcomingCount,
                            if (persian) "پیش‌رو" else "Next",
                            Modifier.weight(1f)
                        )
                        StatPill(
                            "✅",
                            completedCount,
                            if (persian) "انجام" else "Done",
                            Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = { Text(stringResource(R.string.search)) },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReminderFilter.entries.forEach { f ->
                        FilterChip(
                            selected = f == filter,
                            onClick = { onFilter(f) },
                            label = { Text(filterLabel(f, persian)) },
                            leadingIcon = if (f == filter) {
                                { Icon(Icons.Rounded.Done, null, Modifier.size(17.dp)) }
                            } else null
                        )
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (persian) "مرتب‌سازی" else "Sort",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ReminderSort.entries.forEach { option ->
                        AssistChip(
                            onClick = { onSort(option) },
                            label = { Text(sortLabel(option, persian)) },
                            leadingIcon = if (option == sort) {
                                { Text("•", fontWeight = FontWeight.Black) }
                            } else null
                        )
                    }
                }
            }
        }

        if (shown.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f)
                    )
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 42.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌱", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.no_reminders),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            stringResource(R.string.no_reminders_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(shown, key = { it.id }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    persian = persian,
                    onClick = { onOpen(reminder.id) },
                    onComplete = { onComplete(reminder.id) },
                    onCancel = { onCancel(reminder.id) },
                    onDelete = { onDelete(reminder.id) },
                    displayMode = display,
                    countdownStyle = countdownStyle
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    emoji: String,
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = .76f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$emoji $count", style = MaterialTheme.typography.titleMedium)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun filterLabel(value: ReminderFilter, persian: Boolean): String =
    if (persian) {
        when (value) {
            ReminderFilter.ALL -> "همه"
            ReminderFilter.TODAY -> "امروز"
            ReminderFilter.UPCOMING -> "پیش‌رو"
            ReminderFilter.COMPLETED -> "انجام‌شده"
            ReminderFilter.CANCELED -> "لغوشده"
        }
    } else {
        when (value) {
            ReminderFilter.ALL -> "All"
            ReminderFilter.TODAY -> "Today"
            ReminderFilter.UPCOMING -> "Upcoming"
            ReminderFilter.COMPLETED -> "Completed"
            ReminderFilter.CANCELED -> "Cancelled"
        }
    }

private fun sortLabel(value: ReminderSort, persian: Boolean): String =
    if (persian) {
        when (value) {
            ReminderSort.DUE -> "زمان"
            ReminderSort.PRIORITY -> "اولویت"
            ReminderSort.CREATED -> "ایجاد"
        }
    } else {
        when (value) {
            ReminderSort.DUE -> "Due"
            ReminderSort.PRIORITY -> "Priority"
            ReminderSort.CREATED -> "Created"
        }
    }
