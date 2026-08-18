package com.marble.shamsa.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.marble.shamsa.R
import com.marble.shamsa.core.design.AccentPresets
import com.marble.shamsa.core.design.IconCatalog
import com.marble.shamsa.core.model.*
import com.marble.shamsa.core.time.JalaliCalendar
import com.marble.shamsa.core.time.JalaliDate
import java.time.ZoneId
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditorScreen(
    existing: Reminder?,
    categories: List<Category>,
    persian: Boolean,
    popupDefault: Boolean,
    onSave: (Reminder) -> Unit,
    onDelete: (() -> Unit)?,
    onBack: () -> Unit
) {
    val now = System.currentTimeMillis()
    val initialDate = remember(existing) { JalaliCalendar.fromEpochMillis(existing?.dueAtMillis ?: now + 3_600_000L) }
    var title by remember(existing) { mutableStateOf(existing?.title.orEmpty()) }
    var notes by remember(existing) { mutableStateOf(existing?.notes.orEmpty()) }
    var year by remember(existing) { mutableIntStateOf(initialDate.year) }
    var month by remember(existing) { mutableIntStateOf(initialDate.month) }
    var day by remember(existing) { mutableIntStateOf(initialDate.day) }
    val initialTime = remember(existing) { java.time.Instant.ofEpochMilli(existing?.dueAtMillis ?: now + 3_600_000L).atZone(ZoneId.systemDefault()) }
    var hour by remember(existing) { mutableIntStateOf(initialTime.hour) }
    var minute by remember(existing) { mutableIntStateOf(initialTime.minute) }
    var priority by remember(existing) { mutableStateOf(existing?.priority ?: ReminderPriority.NORMAL) }
    var popup by remember(existing) { mutableStateOf(existing?.popupEnabled ?: popupDefault) }
    var categoryId by remember(existing) { mutableStateOf(existing?.categoryId) }
    var icon by remember(existing) { mutableStateOf(existing?.icon ?: "event") }
    var color by remember(existing) { mutableLongStateOf(existing?.colorArgb ?: 0xFF6D4AFF) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) stringResource(R.string.new_reminder) else existing.title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.title)) }, singleLine = true)
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.notes)) }, minLines = 2)

            Text("📅 " + stringResource(R.string.date), style = MaterialTheme.typography.titleMedium)
            JalaliDateStepper(year, month, day, persian, { year = it }, { month = it; day = day.coerceAtMost(JalaliCalendar.daysInMonth(year, it)) }, { day = it })
            Text("⏰ " + stringResource(R.string.time), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                NumberStepper(hour, 0, 23, { hour = it }, Modifier.weight(1f), "%02d")
                Text(":", style = MaterialTheme.typography.headlineMedium)
                NumberStepper(minute, 0, 59, { minute = it }, Modifier.weight(1f), "%02d")
            }

            Text("⚡ " + stringResource(R.string.priority), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderPriority.entries.forEach { p ->
                    FilterChip(selected = p == priority, onClick = { priority = p }, label = { Text(priorityLabel(p, persian)) })
                }
            }

            Text(if (persian) "🗂️ دسته‌بندی" else "🗂️ Category", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = categoryId == null, onClick = { categoryId = null }, label = { Text(if (persian) "بدون دسته" else "None") })
                categories.forEach { category ->
                    FilterChip(
                        selected = categoryId == category.id,
                        onClick = { categoryId = category.id },
                        label = { Text(category.name) },
                        leadingIcon = { Icon(IconCatalog.icon(category.icon), null, Modifier.size(18.dp), tint = Color(category.colorArgb)) }
                    )
                }
            }

            Text(if (persian) "✨ آیکن" else "✨ Icon", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconCatalog.reminderKeys.forEach { key ->
                    FilterChip(
                        selected = icon == key,
                        onClick = { icon = key },
                        label = { Icon(IconCatalog.icon(key), null, Modifier.size(20.dp)) }
                    )
                }
            }

            Text(if (persian) "🎨 رنگ و گرادیان" else "🎨 Color & gradient", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccentPresets.values.forEach { argb ->
                    val selected = color == argb
                    Box(
                        Modifier.size(if (selected) 44.dp else 38.dp)
                            .background(Brush.linearGradient(listOf(Color(argb), Color(argb).copy(alpha = .55f))), CircleShape)
                            .clickable { color = argb },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) Text("✓", color = Color.White)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (persian) "نمایش تمام‌صفحه" else "Full-screen reminder")
                Switch(popup, { popup = it })
            }

            if (onDelete != null) OutlinedButton(onClick = onDelete, Modifier.fillMaxWidth()) { Text(stringResource(R.string.delete)) }
            Button(
                onClick = {
                    val safeDay = day.coerceAtMost(JalaliCalendar.daysInMonth(year, month))
                    val due = JalaliCalendar.toEpochMillis(JalaliDate(year, month, safeDay), hour, minute)
                    val created = existing?.createdAtMillis ?: now
                    onSave(
                        Reminder(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            notes = notes.trim(),
                            dueAtMillis = due,
                            priority = priority,
                            categoryId = categoryId,
                            icon = icon,
                            colorArgb = color,
                            status = existing?.status ?: ReminderStatus.ACTIVE,
                            popupEnabled = popup,
                            createdAtMillis = created,
                            updatedAtMillis = now
                        )
                    )
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.save)) }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun JalaliDateStepper(year: Int, month: Int, day: Int, persian: Boolean, onYear: (Int)->Unit, onMonth: (Int)->Unit, onDay: (Int)->Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        NumberStepper(day, 1, JalaliCalendar.daysInMonth(year, month), onDay, Modifier.weight(1f))
        NumberStepper(month, 1, 12, onMonth, Modifier.weight(1f), formatter = { JalaliCalendar.monthName(it, persian) })
        NumberStepper(year, 1300, 1600, onYear, Modifier.weight(1f))
    }
}

@Composable
private fun NumberStepper(value: Int, min: Int, max: Int, onValue: (Int)->Unit, modifier: Modifier = Modifier, format: String? = null, formatter: ((Int)->String)? = null) {
    Column(modifier) {
        FilledTonalButton(onClick = { onValue((value + 1).coerceAtMost(max)) }, Modifier.fillMaxWidth()) { Text("+") }
        Text(formatter?.invoke(value) ?: format?.format(value) ?: value.toString(), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        FilledTonalButton(onClick = { onValue((value - 1).coerceAtLeast(min)) }, Modifier.fillMaxWidth()) { Text("−") }
    }
}

private fun priorityLabel(value: ReminderPriority, persian: Boolean): String = if (persian) when (value) {
    ReminderPriority.LOW -> "کم"
    ReminderPriority.NORMAL -> "عادی"
    ReminderPriority.HIGH -> "زیاد"
    ReminderPriority.URGENT -> "فوری"
} else value.name.lowercase().replaceFirstChar { it.uppercase() }

