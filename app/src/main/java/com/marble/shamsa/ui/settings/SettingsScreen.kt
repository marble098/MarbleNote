package com.marble.shamsa.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.marble.shamsa.R
import com.marble.shamsa.core.data.AppSettings
import com.marble.shamsa.core.model.DisplayMode
import com.marble.shamsa.core.model.ThemeMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onLanguage: (String)->Unit,
    onTheme: (ThemeMode)->Unit,
    onDisplay: (DisplayMode)->Unit,
    onPopup:(Boolean)->Unit,
    onDrive:()->Unit,
    onSync:()->Unit,
    onExact:()->Unit,
    onFullScreen:()->Unit
) {
    val persian = settings.language == "fa"
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineLarge) }
        item {
            SettingsCard(Icons.Rounded.Language, stringResource(R.string.language)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(settings.language == "fa", { onLanguage("fa") }, { Text("فارسی") })
                    FilterChip(settings.language == "en", { onLanguage("en") }, { Text("English") })
                }
            }
        }
        item {
            SettingsCard(Icons.Rounded.Palette, stringResource(R.string.appearance)) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        AssistChip(
                            onClick = { onTheme(mode) },
                            label = { Text(themeLabel(mode)) },
                            leadingIcon = if (mode == settings.themeMode) { { Icon(Icons.Rounded.Done, null, Modifier.size(16.dp)) } } else null
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DisplayMode.entries.forEach { mode ->
                        AssistChip(
                            onClick = { onDisplay(mode) },
                            label = { Text(displayLabel(mode)) },
                            leadingIcon = if (mode == settings.displayMode) { { Icon(Icons.Rounded.ViewAgenda, null, Modifier.size(16.dp)) } } else null
                        )
                    }
                }
            }
        }
        item {
            SettingsCard(Icons.Rounded.NotificationsActive, stringResource(R.string.permissions)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if(persian) "یادآور تمام‌صفحه" else "Full-screen reminders")
                    Switch(settings.popupReminders, onPopup)
                }
                TextButton(onClick = onExact) { Text(stringResource(R.string.allow_exact_alarms)) }
                TextButton(onClick = onFullScreen) { Text(stringResource(R.string.allow_fullscreen)) }
            }
        }
        item {
            SettingsCard(Icons.Rounded.CloudSync, stringResource(R.string.drive_sync)) {
                Text(
                    if(settings.driveConnected) stringResource(R.string.drive_connected) else stringResource(R.string.cloud_explain),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDrive) { Text(stringResource(R.string.drive_connect)) }
                    OutlinedButton(onClick = onSync, enabled = settings.driveConnected) { Text(stringResource(R.string.sync_now)) }
                }
            }
        }
        item {
            SettingsCard(Icons.Rounded.Info, if (persian) "درباره شمسا" else "About Shamsa") {
                Text(
                    if (persian) "تقویم هجری شمسی، شمارش‌معکوس، ذخیره محلی و همگام‌سازی خصوصی در فضای appDataFolder گوگل درایو."
                    else "Solar Hijri calendar, live countdowns, local-first storage and private Google Drive appDataFolder sync.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { Spacer(Modifier.height(40.dp)) }
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
private fun SettingsCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: @Composable ColumnScope.()->Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            body()
        }
    }
}
