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
import com.marble.shamsa.core.model.CountdownStyle
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
    onCountdownStyle: (CountdownStyle) -> Unit,
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
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 20.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("⚙️", style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                if (persian)
                    "شمسا را دقیقاً برای خودت تنظیم کن."
                else
                    "Make Shamsa feel like yours.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            SettingsCard(
                Icons.Rounded.Language,
                "🌐",
                stringResource(R.string.language)
            ) {
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
            SettingsCard(
                Icons.Rounded.Palette,
                "🎨",
                stringResource(R.string.appearance)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = mode == settings.themeMode,
                            onClick = { onTheme(mode) },
                            label = { Text(themeLabel(mode)) },
                            leadingIcon = if (mode == settings.themeMode) {
                                {
                                    Icon(
                                        Icons.Rounded.Done,
                                        null,
                                        Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DisplayMode.entries.forEach { mode ->
                        AssistChip(
                            onClick = { onDisplay(mode) },
                            label = { Text(displayLabel(mode)) },
                            leadingIcon = {
                                Icon(
                                    if (mode == settings.displayMode)
                                        Icons.Rounded.CheckCircle
                                    else
                                        Icons.Rounded.ViewAgenda,
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
            SettingsCard(
                Icons.Rounded.Timer,
                "⏳",
                if (persian)
                    "نمایش زمان باقی‌مانده"
                else
                    "Countdown style"
            ) {
                Text(
                    if (persian)
                        "یکی از چهار نمایش حرفه‌ای را انتخاب کن؛ همان سبک در ویجت هم استفاده می‌شود."
                    else
                        "Choose one of four professional countdown layouts; the widget follows the same style.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CountdownStyle.entries.forEach { style ->
                        FilterChip(
                            selected = style == settings.countdownStyle,
                            onClick = { onCountdownStyle(style) },
                            label = {
                                Text(
                                    countdownStyleLabel(
                                        style,
                                        persian
                                    )
                                )
                            },
                            leadingIcon = {
                                Text(
                                    countdownStyleEmoji(style)
                                )
                            }
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color =
                        MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = .55f
                        )
                ) {
                    Text(
                        countdownStylePreview(
                            settings.countdownStyle,
                            persian
                        ),
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            SettingsCard(
                Icons.Rounded.NotificationsActive,
                "🔔",
                stringResource(R.string.permissions)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (persian)
                                "یادآور تمام‌صفحه"
                            else
                                "Full-screen reminders",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            if (persian)
                                "برای هشدارهای مهم و فوری"
                            else
                                "For important, time-sensitive alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        settings.popupReminders,
                        onPopup
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onExact) {
                        Text(
                            stringResource(
                                R.string.allow_exact_alarms
                            )
                        )
                    }
                    TextButton(onClick = onFullScreen) {
                        Text(
                            stringResource(
                                R.string.allow_fullscreen
                            )
                        )
                    }
                }
            }
        }

        item {
            SettingsCard(
                Icons.Rounded.CloudSync,
                "☁️",
                stringResource(R.string.drive_sync)
            ) {
                Text(
                    if (settings.driveConnected)
                        stringResource(R.string.drive_connected)
                    else
                        stringResource(R.string.cloud_explain),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (driveUi.working) {
                    LinearProgressIndicator(
                        Modifier.fillMaxWidth()
                    )
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
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDrive,
                        enabled = !driveUi.working
                    ) {
                        Icon(
                            Icons.Rounded.Link,
                            null,
                            Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (settings.driveConnected) {
                                if (persian)
                                    "اتصال دوباره"
                                else
                                    "Reconnect"
                            } else {
                                stringResource(
                                    R.string.drive_connect
                                )
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = onSync,
                        enabled =
                            settings.driveConnected &&
                                !driveUi.working
                    ) {
                        Icon(
                            Icons.Rounded.Sync,
                            null,
                            Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(
                                R.string.sync_now
                            )
                        )
                    }

                    if (settings.driveConnected) {
                        TextButton(
                            onClick = onDisconnect,
                            enabled = !driveUi.working
                        ) {
                            Text(
                                if (persian)
                                    "قطع اتصال"
                                else
                                    "Disconnect"
                            )
                        }
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color =
                        MaterialTheme.colorScheme.secondaryContainer.copy(
                            alpha = .42f
                        )
                ) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            if (persian)
                                "شناسه واقعی OAuth همین نسخه"
                            else
                                "OAuth identity of this installed build",
                            style =
                                MaterialTheme.typography.labelLarge
                        )
                        Text(
                            driveUi.oauthIdentity,
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    if (persian)
                        "اگر اتصال فوراً بسته شود، شمسا حالا خطای واقعی Google را از Intent برمی‌گرداند؛ status 10 یعنی Android OAuth Client باید دقیقاً با پکیج و SHA-1 بالا ساخته شود و Drive API فعال باشد."
                    else
                        "If the consent screen closes immediately, Shamsa now decodes Google's real error. Status 10 means the Android OAuth client must exactly match the package and SHA-1 above, with Drive API enabled.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        item {
            SettingsCard(
                Icons.Rounded.Info,
                "✨",
                if (persian) "درباره شمسا" else "About Shamsa"
            ) {
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
private fun themeLabel(mode: ThemeMode): String =
    when (mode) {
        ThemeMode.SYSTEM ->
            stringResource(R.string.theme_system)

        ThemeMode.LIGHT ->
            stringResource(R.string.theme_light)

        ThemeMode.DARK ->
            stringResource(R.string.theme_dark)
    }

@Composable
private fun displayLabel(mode: DisplayMode): String =
    when (mode) {
        DisplayMode.COMPACT ->
            stringResource(R.string.display_compact)

        DisplayMode.CARDS ->
            stringResource(R.string.display_cards)

        DisplayMode.FOCUS ->
            stringResource(R.string.display_focus)
    }

private fun countdownStyleLabel(
    style: CountdownStyle,
    persian: Boolean
): String =
    if (persian) {
        when (style) {
            CountdownStyle.COMPACT -> "جمع‌وجور"
            CountdownStyle.DIGITAL -> "دیجیتال"
            CountdownStyle.SEGMENTS -> "چهاربخشی"
            CountdownStyle.FOCUS -> "تمرکزی"
        }
    } else {
        when (style) {
            CountdownStyle.COMPACT -> "Compact"
            CountdownStyle.DIGITAL -> "Digital"
            CountdownStyle.SEGMENTS -> "Segments"
            CountdownStyle.FOCUS -> "Focus"
        }
    }

private fun countdownStyleEmoji(style: CountdownStyle): String =
    when (style) {
        CountdownStyle.COMPACT -> "⚡"
        CountdownStyle.DIGITAL -> "🕒"
        CountdownStyle.SEGMENTS -> "▦"
        CountdownStyle.FOCUS -> "🎯"
    }

private fun countdownStylePreview(
    style: CountdownStyle,
    persian: Boolean
): String =
    if (persian) {
        when (style) {
            CountdownStyle.COMPACT -> "۲ روز ۰۴ ساعت"
            CountdownStyle.DIGITAL -> "۰۲:۰۴:۳۱:۰۸"
            CountdownStyle.SEGMENTS -> "۰۲ر • ۰۴س • ۳۱د • ۰۸ث"
            CountdownStyle.FOCUS -> "۲ روز  •  ۰۴ ساعت و ۳۱ دقیقه"
        }
    } else {
        when (style) {
            CountdownStyle.COMPACT -> "2d 04h"
            CountdownStyle.DIGITAL -> "02:04:31:08"
            CountdownStyle.SEGMENTS -> "02d • 04h • 31m • 08s"
            CountdownStyle.FOCUS -> "2 days  •  04h 31m"
        }
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
        elevation =
            CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = MaterialTheme.shapes.medium,
                    color =
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(11.dp))

                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            body()
        }
    }
}
