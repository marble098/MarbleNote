package com.marble.shamsa.core.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marble.shamsa.core.model.CountdownStyle
import com.marble.shamsa.core.model.DisplayMode
import com.marble.shamsa.core.model.Reminder
import com.marble.shamsa.core.model.ReminderStatus
import com.marble.shamsa.core.time.CountdownFormatter
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.roundToInt

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
    val urgent =
        reminder.status == ReminderStatus.ACTIVE &&
            remaining in 1L..60_000L
    val pulseTarget = if (urgent) 1.012f else 1f
    val scale by animateFloatAsState(
        targetValue = pulseTarget,
        label = "urgentPulse"
    )
    val accent = Color(reminder.colorArgb)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .animateContentSize(),
        shape = RoundedCornerShape(
            if (focus) 22.dp else 18.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            accent.copy(alpha = if (focus) .10f else .065f),
                            Color.Transparent
                        )
                    )
                )
                .padding(
                    if (compact) 11.dp
                    else if (focus) 15.dp
                    else 13.dp
                ),
            verticalArrangement = Arrangement.spacedBy(
                if (compact) 7.dp else 9.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        if (focus) 16.dp else 13.dp
                    ),
                    color = accent.copy(alpha = .11f),
                    modifier = Modifier.size(
                        if (compact) 38.dp
                        else if (focus) 48.dp
                        else 42.dp
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            IconCatalog.icon(reminder.icon),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(
                                if (focus) 25.dp else 21.dp
                            )
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        reminder.title,
                        style = if (focus)
                            MaterialTheme.typography.titleLarge
                        else
                            MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )

                    if (!compact && reminder.notes.isNotBlank()) {
                        Text(
                            reminder.notes,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = if (focus) 2 else 1,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (reminder.status == ReminderStatus.ACTIVE) {
                    IconButton(
                        onClick = onComplete,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription =
                                if (persian) "انجام شد" else "Done",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription =
                                if (persian) "میانبرها"
                                else "Quick actions",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        if (reminder.status == ReminderStatus.ACTIVE) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (persian)
                                            "انجام شد"
                                        else
                                            "Mark done"
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.DoneAll, null)
                                },
                                onClick = {
                                    menuOpen = false
                                    onComplete()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (persian)
                                            "لغو یادآور"
                                        else
                                            "Cancel task"
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Block, null)
                                },
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
                                    color =
                                        MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    null,
                                    tint =
                                        MaterialTheme.colorScheme.error
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
            title = {
                Text(
                    if (persian)
                        "حذف یادآور؟"
                    else
                        "Delete reminder?"
                )
            },
            text = {
                Text(
                    if (persian)
                        "این یادآور حذف می‌شود و حذف آن در همگام‌سازی بعدی نیز ثبت می‌شود."
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
                TextButton(
                    onClick = { confirmDelete = false }
                ) {
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
            text = if (persian) "انجام شده" else "Completed",
            color = MaterialTheme.colorScheme.secondary
        )

        ReminderStatus.CANCELED -> StatusPill(
            text = if (persian) "لغو شده" else "Cancelled",
            color = MaterialTheme.colorScheme.error
        )

        ReminderStatus.ACTIVE -> {
            val progress = reminderProgress(reminder, now)

            when (style) {
                CountdownStyle.COMPACT -> {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                if (persian)
                                    "تا موعد"
                                else
                                    "Remaining",
                                style =
                                    MaterialTheme.typography.labelSmall,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.weight(1f))
                            AnimatedContent(
                                targetState =
                                    CountdownFormatter.compact(
                                        reminder.dueAtMillis,
                                        now,
                                        persian
                                    ),
                                label = "compactVisual"
                            ) { value ->
                                Text(
                                    value,
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold,
                                    style =
                                        MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        VisualBar(
                            progress = progress,
                            accent = accent,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                CountdownStyle.DIGITAL -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = accent.copy(alpha = .055f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(
                                horizontal = 11.dp,
                                vertical = if (compact) 8.dp else 10.dp
                            ),
                            verticalArrangement =
                                Arrangement.spacedBy(7.dp)
                        ) {
                            AnimatedContent(
                                targetState =
                                    CountdownFormatter.digital(
                                        reminder.dueAtMillis,
                                        now,
                                        persian
                                    ),
                                label = "digitalVisual"
                            ) { value ->
                                Text(
                                    value,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = accent,
                                    fontWeight = FontWeight.Bold,
                                    style = if (compact)
                                        MaterialTheme.typography.titleMedium
                                    else
                                        MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                            }

                            TickStrip(
                                progress = progress,
                                accent = accent
                            )

                            Text(
                                CountdownFormatter.digitalLegend(
                                    reminder.dueAtMillis,
                                    now,
                                    persian
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                style =
                                    MaterialTheme.typography.labelSmall,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
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
                    val labels =
                        CountdownFormatter.unitLabels(persian)
                    val parts = CountdownFormatter.parts(
                        reminder.dueAtMillis,
                        now
                    )
                    val visual = listOf(
                        ((parts.days % 30L) / 29f)
                            .coerceIn(0f, 1f),
                        (parts.hours / 23f)
                            .coerceIn(0f, 1f),
                        (parts.minutes / 59f)
                            .coerceIn(0f, 1f),
                        (parts.seconds / 59f)
                            .coerceIn(0f, 1f)
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {
                        values.forEachIndexed { index, value ->
                            SegmentCell(
                                value = value,
                                label = labels[index],
                                progress = visual[index],
                                accent = accent,
                                highlighted = index == 0,
                                compact = compact,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                CountdownStyle.FOCUS -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = accent.copy(alpha = .055f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(
                                horizontal = 11.dp,
                                vertical = 9.dp
                            ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(
                                    if (compact) 50.dp else 58.dp
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                VisualRing(
                                    progress = progress,
                                    accent = accent,
                                    modifier =
                                        Modifier.matchParentSize()
                                )
                                Text(
                                    "${(progress * 100f).roundToInt()}%",
                                    color = accent,
                                    style =
                                        MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.width(11.dp))

                            Column(
                                Modifier.weight(1f),
                                verticalArrangement =
                                    Arrangement.spacedBy(2.dp)
                            ) {
                                AnimatedContent(
                                    targetState =
                                        CountdownFormatter.focusPrimary(
                                            reminder.dueAtMillis,
                                            now,
                                            persian
                                        ),
                                    label = "focusVisual"
                                ) { value ->
                                    Text(
                                        value,
                                        color = accent,
                                        fontWeight = FontWeight.Bold,
                                        style =
                                            MaterialTheme.typography.titleMedium
                                    )
                                }

                                Text(
                                    CountdownFormatter.focusSecondary(
                                        reminder.dueAtMillis,
                                        now,
                                        persian
                                    ),
                                    style =
                                        MaterialTheme.typography.bodySmall,
                                    color =
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
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

@Composable
private fun VisualBar(
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val track =
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .85f)

    Canvas(
        modifier = modifier.height(7.dp)
    ) {
        val radius =
            CornerRadius(size.height / 2f, size.height / 2f)

        drawRoundRect(
            color = track,
            cornerRadius = radius
        )

        if (progress > 0f) {
            drawRoundRect(
                color = accent,
                size = Size(
                    width = size.width * progress,
                    height = size.height
                ),
                cornerRadius = radius
            )
        }
    }
}

@Composable
private fun VisualRing(
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val track =
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .9f)

    Canvas(modifier) {
        val stroke = 6.dp.toPx()

        drawArc(
            color = track,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round
            )
        )

        if (progress > 0f) {
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(
                    width = stroke,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
private fun TickStrip(
    progress: Float,
    accent: Color
) {
    val segments = 12
    val filled = ceil(progress * segments)
        .toInt()
        .coerceIn(0, segments)
    val track =
        MaterialTheme.colorScheme.surfaceVariant

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(segments) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        color =
                            if (index < filled)
                                accent
                            else
                                track,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
private fun SegmentCell(
    value: String,
    label: String,
    progress: Float,
    accent: Color,
    highlighted: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        color = accent.copy(
            alpha = if (highlighted) .09f else .045f
        )
    ) {
        Column(
            Modifier.padding(
                horizontal = 6.dp,
                vertical = if (compact) 7.dp else 8.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                value,
                color = accent,
                fontWeight = FontWeight.Bold,
                style = if (compact)
                    MaterialTheme.typography.titleSmall
                else
                    MaterialTheme.typography.titleMedium
            )

            VisualBar(
                progress = progress,
                accent = accent,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = .10f)
    ) {
        Text(
            text,
            color = color,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
        )
    }
}
