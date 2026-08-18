package com.marble.shamsa.core.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marble.shamsa.core.model.CountdownStyle
import com.marble.shamsa.core.model.DisplayMode
import com.marble.shamsa.core.model.Reminder
import com.marble.shamsa.core.model.ReminderStatus
import com.marble.shamsa.core.time.CountdownFormatter
import kotlinx.coroutines.delay

@Composable
fun ReminderCard(
    reminder: Reminder,
    persian: Boolean,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    displayMode: DisplayMode = DisplayMode.CARDS,
    countdownStyle: CountdownStyle = CountdownStyle.SEGMENTS
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(reminder.id, reminder.dueAtMillis, reminder.status) {
        while (reminder.status == ReminderStatus.ACTIVE) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val compact = displayMode == DisplayMode.COMPACT
    val focus = displayMode == DisplayMode.FOCUS
    val remaining = reminder.dueAtMillis - now
    val urgent = reminder.status == ReminderStatus.ACTIVE && remaining in 1L..60_000L
    val pulseTarget = if (urgent) 1.018f else 1f
    val scale by animateFloatAsState(pulseTarget, label = "urgentPulse")
    val accent = Color(reminder.colorArgb)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .animateContentSize(),
        shape = RoundedCornerShape(if (focus) 30.dp else 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (focus) 5.dp else 2.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            accent.copy(alpha = if (focus) .22f else .13f),
                            accent.copy(alpha = .035f),
                            Color.Transparent
                        )
                    )
                )
                .padding(if (compact) 14.dp else if (focus) 20.dp else 17.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 11.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(if (focus) 22.dp else 18.dp),
                    color = accent.copy(alpha = .16f),
                    modifier = Modifier.size(if (compact) 48.dp else if (focus) 64.dp else 56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            IconCatalog.icon(reminder.icon),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(if (focus) 31.dp else 25.dp)
                        )
                    }
                }

                Spacer(Modifier.width(13.dp))

                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            reminder.title,
                            style = if (compact) MaterialTheme.typography.titleMedium
                            else MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        if (urgent) {
                            Spacer(Modifier.width(4.dp))
                            Text("🔥", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    if (!compact && reminder.notes.isNotBlank()) {
                        Text(
                            reminder.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (focus) 2 else 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (reminder.status == ReminderStatus.ACTIVE) {
                    IconButton(onClick = onComplete) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = if (persian) "انجام شد" else "Done",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = if (persian) "میانبرها" else "Quick actions"
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        if (reminder.status == ReminderStatus.ACTIVE) {
                            DropdownMenuItem(
                                text = { Text(if (persian) "انجام شد" else "Mark done") },
                                leadingIcon = { Icon(Icons.Rounded.DoneAll, null) },
                                onClick = {
                                    menuOpen = false
                                    onComplete()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (persian) "لغو یادآور" else "Cancel task") },
                                leadingIcon = { Icon(Icons.Rounded.Block, null) },
                                onClick = {
                                    menuOpen = false
                                    onCancel()
                                }
                            )
                            HorizontalDivider()
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (persian) "حذف" else "Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuOpen = false
                                confirmDelete = true
                            }
                        )
                    }
                }
            }

            ReminderCountdown(
                reminder = reminder,
                now = now,
                persian = persian,
                style = countdownStyle,
                accent = accent,
                compact = compact
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Text("🗑️") },
            title = { Text(if (persian) "حذف یادآور؟" else "Delete reminder?") },
            text = {
                Text(
                    if (persian)
                        "این یادآور حذف می‌شود و در همگام‌سازی بعدی نیز حذف ثبت خواهد شد."
                    else
                        "This reminder will be deleted and the deletion will be synced."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (persian) "حذف" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(if (persian) "انصراف" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReminderCountdown(
    reminder: Reminder,
    now: Long,
    persian: Boolean,
    style: CountdownStyle,
    accent: Color,
    compact: Boolean
) {
    when (reminder.status) {
        ReminderStatus.COMPLETED -> StatusPill(
            emoji = "✅",
            text = if (persian) "انجام شده" else "Completed",
            color = MaterialTheme.colorScheme.tertiary
        )

        ReminderStatus.CANCELED -> StatusPill(
            emoji = "⛔",
            text = if (persian) "لغو شده" else "Cancelled",
            color = MaterialTheme.colorScheme.error
        )

        ReminderStatus.ACTIVE -> when (style) {
            CountdownStyle.COMPACT -> {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accent.copy(alpha = .12f)
                ) {
                    AnimatedContent(
                        targetState = CountdownFormatter.compact(
                            reminder.dueAtMillis,
                            now,
                            persian
                        ),
                        label = "countdownCompact"
                    ) { value ->
                        Text(
                            value,
                            color = accent,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            CountdownStyle.DIGITAL -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = accent.copy(alpha = .10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(horizontal = 14.dp, vertical = if (compact) 9.dp else 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(
                            targetState = CountdownFormatter.digital(
                                reminder.dueAtMillis,
                                now,
                                persian
                            ),
                            label = "countdownDigital"
                        ) { value ->
                            Text(
                                value,
                                color = accent,
                                fontWeight = FontWeight.Black,
                                style = if (compact)
                                    MaterialTheme.typography.titleMedium
                                else
                                    MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            CountdownFormatter.digitalLegend(
                                reminder.dueAtMillis,
                                now,
                                persian
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            CountdownStyle.SEGMENTS -> {
                val values = CountdownFormatter.unitValues(
                    reminder.dueAtMillis,
                    now,
                    persian
                )
                val labels = CountdownFormatter.unitLabels(persian)

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    values.forEachIndexed { index, value ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(if (compact) 13.dp else 17.dp),
                            color = accent.copy(alpha = if (index == 0) .16f else .09f)
                        ) {
                            Column(
                                Modifier.padding(vertical = if (compact) 7.dp else 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AnimatedContent(
                                    targetState = value,
                                    label = "segment$index"
                                ) { animated ->
                                    Text(
                                        animated,
                                        color = accent,
                                        fontWeight = FontWeight.Black,
                                        style = if (compact)
                                            MaterialTheme.typography.titleSmall
                                        else
                                            MaterialTheme.typography.titleLarge
                                    )
                                }
                                Text(
                                    labels[index],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            CountdownStyle.FOCUS -> {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = accent.copy(alpha = .12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 15.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏳", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = CountdownFormatter.focusPrimary(
                                    reminder.dueAtMillis,
                                    now,
                                    persian
                                ),
                                label = "focusPrimary"
                            ) { value ->
                                Text(
                                    value,
                                    color = accent,
                                    fontWeight = FontWeight.Black,
                                    style = if (compact)
                                        MaterialTheme.typography.titleMedium
                                    else
                                        MaterialTheme.typography.titleLarge
                                )
                            }
                            Text(
                                CountdownFormatter.focusSecondary(
                                    reminder.dueAtMillis,
                                    now,
                                    persian
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(emoji: String, text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = .12f)
    ) {
        Text(
            "$emoji  $text",
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}
