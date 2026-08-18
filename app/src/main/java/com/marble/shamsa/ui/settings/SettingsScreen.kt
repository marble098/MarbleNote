package com.marble.shamsa.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.marble.shamsa.R
import com.marble.shamsa.core.data.AppSettings
import com.marble.shamsa.core.model.DisplayMode
import com.marble.shamsa.core.model.ThemeMode
import com.marble.shamsa.ui.viewmodel.DriveUiState

@Composable
fun SettingsScreen(
    settings: AppSettings,
    driveUi: DriveUiState,
    onLanguage: (String) -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onDisplay: (DisplayMode) -> Unit,
    onPopup: (Boolean) -> Unit,
    onDrive: () -> Unit,
    onSync: () -> Unit,
    onDisconnect: () -> Unit,
    onExact: () -> Unit,
    onFullScreen: () -> Unit
) {
    val persian = settings.language == "fa"

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("⚙️", style = MaterialTheme.typography.displaySmall)
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineLarge)
            Text(
                if (persian) "شمسا را دقیقاً برای خودت تنظیم کن." else "Make Shamsa feel like yours.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            SettingsCard(Icons.Rounded.Language, "🌐", stringResource(R.string.language)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        settings.language == "fa",
                        { onLanguage("fa") },
                        { Text("فارسی") }
                    )
                    FilterChip(
                        settings.language == "en",
                        { onLanguage("en") },
                        { Text("English") }
                    )
                }
            }
        }

        item {
            SettingsCard(Icons.Rounded.Palette, "🎨", stringResource(R.string.appearance)) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = mode == settings.themeMode,
                            onClick = { onTheme(mode) },
                            label = { Text(themeLabel(mode)) },
                            leadingIcon = if (mode == settings.themeMode) {
                                { Icon(Icons.Rounded.Done, null, Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DisplayMode.entries.forEach { mode ->
                        AssistChip(
                            onClick = { onDisplay(mode) },
                            label = { Text(displayLabel(mode)) },
                            leadingIcon = {
                                Icon(
                                    if (mode == settings.displayMode) Icons.Rounded.CheckCircle else Icons.Rounded.ViewAgenda,
                                    null,
                                    Modifier.size(17.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        item {
            SettingsCard(Icons.Rounded.NotificationsActive, "🔔", stringResource(R.string.permissions)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (persian) "یادآور تمام‌صفحه" else "Full-screen reminders",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            if (persian) "برای هشدارهای مهم و فوری" else "For important, time-sensitive alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(settings.popupReminders, onPopup)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onExact) { Text(stringResource(R.string.allow_exact_alarms)) }
                    TextButton(onClick = onFullScreen) { Text(stringResource(R.string.allow_fullscreen)) }
                }
            }
        }

        item {
            SettingsCard(Icons.Rounded.CloudSync, "☁️", stringResource(R.string.drive_sync)) {
                Text(
                    if (settings.driveConnected)
                        stringResource(R.string.drive_connected)
                    else
                        stringResource(R.string.cloud_explain),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (driveUi.working) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                driveUi.message?.let { message ->
                    Surface(
                        color = if (driveUi.successful)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            message,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (driveUi.successful)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onDrive, enabled = !driveUi.working) {
                        Icon(Icons.Rounded.Link, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (settings.driveConnected)
                                if (persian) "اتصال دوباره" else "Reconnect"
                            else
                                stringResource(R.string.drive_connect)
                        )
                    }

                    OutlinedButton(
                        onClick = onSync,
                        enabled = settings.driveConnected && !driveUi.working
                    ) {
                        Icon(Icons.Rounded.Sync, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.sync_now))
                    }

                    if (settings.driveConnected) {
                        TextButton(onClick = onDisconnect, enabled = !driveUi.working) {
                            Text(if (persian) "قطع اتصال" else "Disconnect")
                        }
                    }
                }

                Text(
                    if (persian)
                        "اگر خطای OAuth status 10 دیدی، SHA-1 نسخه Release و پکیج com.marble.shamsa را در Google Cloud بررسی کن."
                    else
                        "If OAuth reports status 10, verify the Release SHA-1 and package com.marble.shamsa in Google Cloud.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        item {
            SettingsCard(Icons.Rounded.Info, "✨", if (persian) "درباره شمسا" else "About Shamsa") {
                Text(
                    if (persian)
                        "تقویم هجری شمسی، شمارش‌معکوس، ذخیره محلی و همگام‌سازی خصوصی در appDataFolder گوگل درایو."
                    else
                        "Solar Hijri calendar, live countdowns, local-first storage and private Google Drive appDataFolder sync.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
    ThemeMode.DARK -> stringResource(R.string.theme_dark)
}

@Composable
private fun displayLabel(mode: DisplayMode): String = when (mode) {
    DisplayMode.COMPACT -> stringResource(R.string.display_compact)
    DisplayMode.CARDS -> stringResource(R.string.display_cards)
    DisplayMode.FOCUS -> stringResource(R.string.display_focus)
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    emoji: String,
    title: String,
    body: @Composable ColumnScope.() -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.width(11.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(emoji, style = MaterialTheme.typography.titleLarge)
            }
            body()
        }
    }
}
