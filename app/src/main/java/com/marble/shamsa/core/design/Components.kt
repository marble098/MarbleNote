package com.marble.shamsa.core.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marble.shamsa.core.model.DisplayMode
import com.marble.shamsa.core.model.Reminder
import com.marble.shamsa.core.time.CountdownFormatter
import kotlinx.coroutines.delay

@Composable
fun ReminderCard(
    reminder: Reminder,
    persian: Boolean,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    displayMode: DisplayMode = DisplayMode.CARDS
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(reminder.id, reminder.dueAtMillis) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val compact = displayMode == DisplayMode.COMPACT
    val focus = displayMode == DisplayMode.FOCUS
    val pulseTarget = if ((reminder.dueAtMillis - now) in 1L..60_000L) 1.025f else 1f
    val scale by animateFloatAsState(pulseTarget, label = "urgentPulse")
    val accent = Color(reminder.colorArgb)
    val secondAccent = accent.copy(alpha = if (focus) .28f else .08f)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().scale(scale).animateContentSize(),
        shape = RoundedCornerShape(if (focus) 30.dp else 24.dp)
    ) {
        Row(
            Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(accent.copy(alpha = if (focus) .24f else .16f), secondAccent, Color.Transparent)))
                .padding(if (compact) 14.dp else if (focus) 22.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(if (focus) 22.dp else 18.dp),
                color = accent.copy(alpha = .18f),
                modifier = Modifier.size(if (compact) 48.dp else if (focus) 68.dp else 58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(IconCatalog.icon(reminder.icon), null, tint = accent, modifier = Modifier.size(if (focus) 32.dp else 24.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(reminder.title, style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge, maxLines = 1)
                if (!compact && reminder.notes.isNotBlank()) {
                    Text(reminder.notes, style = MaterialTheme.typography.bodyMedium, maxLines = if (focus) 2 else 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(if (focus) 9.dp else 5.dp))
                AnimatedContent(targetState = CountdownFormatter.compact(reminder.dueAtMillis, now, persian), label = "countdown") { value ->
                    Text(value, color = accent, fontWeight = FontWeight.Bold, style = if (focus) MaterialTheme.typography.titleMedium else LocalTextStyle.current)
                }
            }
            IconButton(onClick = onComplete) { Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) }
        }
    }
}
