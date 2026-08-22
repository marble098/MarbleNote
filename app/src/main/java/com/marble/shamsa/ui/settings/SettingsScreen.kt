package com.marble.shamsa.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    var showDriveDetails by rememberSaveable {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 92.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    if (persian)
                        "تنظیمات ساده، واضح و بدون شلوغی."
                    else
                        "Simple controls without visual clutter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SettingsCard(
                icon = Icons.Rounded.Language,
                title = stringResource(R.string.language)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    FilterChip(
                        selected = settings.language == "fa",
                        onClick = { onLanguage("fa") },
                        label = { Text("فارسی") }
                    )
                    FilterChip(
                        selected = settings.language == "en",
                        onClick = { onLanguage("en") },
                        label = { Text("English") }
                    )
                }
            }
        }

        item {
            SettingsCard(
                icon = Icons.Rounded.Palette,
                title = stringResource(R.string.appearance)
            ) {
                Text(
                    if (persian)
                        "پوسته"
                    else
                        "Theme",
                    style = MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = mode == settings.themeMode,
                            onClick = { onTheme(mode) },
                            label = { Text(themeLabel(mode)) }
                        )
                    }
                }

                Text(
                    if (persian)
                        "چیدمان کارت‌ها"
                    else
                        "Card density",
                    style = MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    DisplayMode.entries.forEach { mode ->
                        FilterChip(
                            selected = mode == settings.displayMode,
                            onClick = { onDisplay(mode) },
                            label = { Text(displayLabel(mode)) }
                        )
                    }
                }
            }
        }

        item {
            SettingsCard(
                icon = Icons.Rounded.Timer,
                title =
                    if (persian)
                        "نمایش زمان باقی‌مانده"
                    else
                        "Countdown visualization"
            ) {
                Text(
                    if (persian)
                        "این چهار حالت حالا فقط عدد نیستند؛ هرکدام یک نمایش بصری متفاوت دارند."
                    else
                        "Each mode now has a distinct visual timeline, not just numbers.",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    CountdownStyle.entries.forEach { style ->
                        CountdownStyleCard(
                            style = style,
                            persian = persian,
                            selected =
                                style == settings.countdownStyle,
                            onClick = {
                                onCountdownStyle(style)
                            }
                        )
                    }
                }
            }
        }

        item {
            SettingsCard(
                icon = Icons.Rounded.NotificationsActive,
                title = stringResource(R.string.permissions)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            if (persian)
                                "یادآور تمام‌صفحه"
                            else
                                "Full-screen reminder",
                            style =
                                MaterialTheme.typography.titleSmall
                        )
                        Text(
                            if (persian)
                                "برای هشدارهای فوری"
                            else
                                "For time-sensitive alerts",
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = settings.popupReminders,
                        onCheckedChange = onPopup
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    TextButton(
                        onClick = onExact,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(
                                R.string.allow_exact_alarms
                            )
                        )
                    }

                    TextButton(
                        onClick = onFullScreen,
                        modifier = Modifier.weight(1f)
                    ) {
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
                icon = Icons.Rounded.CloudSync,
                title = stringResource(R.string.drive_sync)
            ) {
                DriveStatusLine(
                    connected = settings.driveConnected,
                    working = driveUi.working,
                    persian = persian
                )

                Text(
                    if (persian)
                        "یادآورها و یادداشت‌ها در appDataFolder خصوصی Drive همگام می‌شوند."
                    else
                        "Reminders and notes sync to the private Drive appDataFolder.",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (driveUi.working) {
                    LinearProgressIndicator(
                        Modifier.fillMaxWidth()
                    )
                }

                driveUi.message?.let { raw ->
                    val isSuccess = driveUi.successful
                    val internal =
                        raw.contains("8: INTERNAL_ERROR")
                    val developer =
                        raw.contains("10: DEVELOPER_ERROR")

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = when {
                            isSuccess ->
                                MaterialTheme.colorScheme.secondaryContainer
                            internal || developer ->
                                MaterialTheme.colorScheme.errorContainer
                            else ->
                                MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Row(
                            Modifier.padding(11.dp),
                            verticalAlignment =
                                Alignment.Top
                        ) {
                            Icon(
                                imageVector = when {
                                    isSuccess ->
                                        Icons.Rounded.CheckCircle
                                    internal || developer ->
                                        Icons.Rounded.ErrorOutline
                                    else ->
                                        Icons.Rounded.Info
                                },
                                contentDescription = null,
                                modifier = Modifier.size(19.dp),
                                tint = when {
                                    isSuccess ->
                                        MaterialTheme.colorScheme.secondary
                                    internal || developer ->
                                        MaterialTheme.colorScheme.error
                                    else ->
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                friendlyDriveMessage(
                                    raw = raw,
                                    persian = persian
                                ),
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color = when {
                                    isSuccess ->
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    internal || developer ->
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else ->
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDrive,
                        enabled = !driveUi.working,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Rounded.Link,
                            null,
                            Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (settings.driveConnected) {
                                if (persian)
                                    "اتصال دوباره"
                                else
                                    "Reconnect"
                            } else {
                                if (persian)
                                    "اتصال گوگل"
                                else
                                    "Connect Google"
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = onSync,
                        enabled =
                            settings.driveConnected &&
                                !driveUi.working,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Rounded.Sync,
                            null,
                            Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (persian)
                                "همگام‌سازی"
                            else
                                "Sync now"
                        )
                    }
                }

                if (driveUi.message != null ||
                    settings.driveConnected
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDisconnect,
                            enabled = !driveUi.working
                        ) {
                            Icon(
                                Icons.Rounded.RestartAlt,
                                null,
                                Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (persian)
                                    "بازنشانی مجوز"
                                else
                                    "Reset authorization"
                            )
                        }

                        TextButton(
                            onClick = {
                                showDriveDetails =
                                    !showDriveDetails
                            }
                        ) {
                            Text(
                                if (showDriveDetails) {
                                    if (persian)
                                        "بستن جزئیات"
                                    else
                                        "Hide details"
                                } else {
                                    if (persian)
                                        "جزئیات فنی"
                                    else
                                        "Technical details"
                                }
                            )
                        }
                    }
                }

                if (showDriveDetails) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color =
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            Modifier.padding(10.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                if (persian)
                                    "هویت OAuth این APK"
                                else
                                    "OAuth identity",
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
                            driveUi.message?.let { raw ->
                                HorizontalDivider()
                                Text(
                                    raw,
                                    style =
                                        MaterialTheme.typography.bodySmall,
                                    color =
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color =
                        MaterialTheme.colorScheme.primaryContainer
                            .copy(alpha = .58f)
                ) {
                    Row(
                        Modifier.padding(10.dp),
                        verticalAlignment =
                            Alignment.Top
                    ) {
                        Icon(
                            Icons.Rounded.Backup,
                            null,
                            Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            if (persian)
                                "لایهٔ دوم پشتیبان هم فعال می‌شود: اگر پشتیبان Google/Android دستگاه روشن باشد، دیتابیس شمسا می‌تواند هنگام نصب دوباره بازیابی شود؛ حتی اگر اتصال دستی Drive موقتاً مشکل داشته باشد."
                            else
                                "A second backup layer is enabled too: when Android/Google device backup is on, Shamsa's database can be restored after reinstall even if manual Drive authorization is temporarily unavailable.",
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            SettingsCard(
                icon = Icons.Rounded.Info,
                title =
                    if (persian)
                        "درباره شمسا"
                    else
                        "About Shamsa"
            ) {
                Text(
                    if (persian)
                        "تقویم شمسی، یادآورها، یادداشت‌ها، شمارش‌معکوس بصری و پشتیبان دو‌لایه."
                    else
                        "Solar calendar, reminders, notes, visual countdowns and two-layer backup.",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DriveStatusLine(
    connected: Boolean,
    working: Boolean,
    persian: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(9.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = when {
                working ->
                    MaterialTheme.colorScheme.tertiary
                connected ->
                    MaterialTheme.colorScheme.secondary
                else ->
                    MaterialTheme.colorScheme.outline
            }
        ) {}

        Spacer(Modifier.width(7.dp))

        Text(
            when {
                working ->
                    if (persian)
                        "در حال ارتباط با Google…"
                    else
                        "Contacting Google…"
                connected ->
                    if (persian)
                        "متصل"
                    else
                        "Connected"
                else ->
                    if (persian)
                        "هنوز متصل نشده"
                    else
                        "Not connected yet"
            },
            style = MaterialTheme.typography.labelLarge,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun friendlyDriveMessage(
    raw: String,
    persian: Boolean
): String {
    return when {
        raw.contains("8: INTERNAL_ERROR") ->
            if (persian)
                "Google Play services نتوانست مرحلهٔ مجوز را کامل کند. یک‌بار «بازنشانی مجوز» را بزن، دوباره حساب Google را انتخاب کن و تلاش کن. اگر باز هم تکرار شد، تنظیمات OAuth پروژهٔ Google Cloud باید بررسی شود."
            else
                "Google Play services could not finish authorization. Reset authorization, select the Google account again, and retry. If it persists, verify the Google Cloud OAuth configuration."

        raw.contains("10: DEVELOPER_ERROR") ->
            if (persian)
                "هویت Android OAuth در Google Cloud با این APK هماهنگ نیست. Package و SHA-1 باید دقیقاً با نسخهٔ نصب‌شده یکی باشند."
            else
                "The Android OAuth identity in Google Cloud does not match this APK. Package name and SHA-1 must match exactly."

        raw.contains("connected", ignoreCase = true) ->
            if (persian)
                "اتصال برقرار شد و اطلاعات همگام شد."
            else
                "Connected and synced successfully."

        raw.contains("cancel", ignoreCase = true) ->
            if (persian)
                "فرایند اتصال لغو شد."
            else
                "Authorization was cancelled."

        else -> raw
    }
}

@Composable
private fun CountdownStyleCard(
    style: CountdownStyle,
    persian: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(132.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor =
                if (selected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
                        .copy(alpha = .52f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color =
                if (selected)
                    MaterialTheme.colorScheme.primary
                        .copy(alpha = .38f)
                else
                    MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            Modifier.padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = countdownStyleIcon(style),
                contentDescription = null,
                tint =
                    if (selected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(23.dp)
            )

            Text(
                countdownStyleLabel(style, persian),
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                countdownStyleDescription(style, persian),
                style = MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun countdownStyleIcon(
    style: CountdownStyle
): ImageVector =
    when (style) {
        CountdownStyle.COMPACT ->
            Icons.Rounded.Timeline
        CountdownStyle.DIGITAL ->
            Icons.Rounded.Equalizer
        CountdownStyle.SEGMENTS ->
            Icons.Rounded.ViewWeek
        CountdownStyle.FOCUS ->
            Icons.Rounded.TrackChanges
    }

private fun countdownStyleLabel(
    style: CountdownStyle,
    persian: Boolean
): String =
    if (persian) {
        when (style) {
            CountdownStyle.COMPACT ->
                "نوار زمان"
            CountdownStyle.DIGITAL ->
                "نبض دیجیتال"
            CountdownStyle.SEGMENTS ->
                "چهاربخشی"
            CountdownStyle.FOCUS ->
                "حلقه تمرکز"
        }
    } else {
        when (style) {
            CountdownStyle.COMPACT ->
                "Timeline"
            CountdownStyle.DIGITAL ->
                "Digital pulse"
            CountdownStyle.SEGMENTS ->
                "Four units"
            CountdownStyle.FOCUS ->
                "Focus ring"
        }
    }

private fun countdownStyleDescription(
    style: CountdownStyle,
    persian: Boolean
): String =
    if (persian) {
        when (style) {
            CountdownStyle.COMPACT ->
                "خط پیشرفت مینیمال"
            CountdownStyle.DIGITAL ->
                "تیک‌های زنده"
            CountdownStyle.SEGMENTS ->
                "نوار هر واحد"
            CountdownStyle.FOCUS ->
                "پیشرفت حلقه‌ای"
        }
    } else {
        when (style) {
            CountdownStyle.COMPACT ->
                "Minimal progress line"
            CountdownStyle.DIGITAL ->
                "Live tick strip"
            CountdownStyle.SEGMENTS ->
                "A bar per unit"
            CountdownStyle.FOCUS ->
                "Circular progress"
        }
    }

@Composable
private fun themeLabel(
    mode: ThemeMode
): String =
    when (mode) {
        ThemeMode.SYSTEM ->
            stringResource(R.string.theme_system)
        ThemeMode.LIGHT ->
            stringResource(R.string.theme_light)
        ThemeMode.DARK ->
            stringResource(R.string.theme_dark)
    }

@Composable
private fun displayLabel(
    mode: DisplayMode
): String =
    when (mode) {
        DisplayMode.COMPACT ->
            stringResource(R.string.display_compact)
        DisplayMode.CARDS ->
            stringResource(R.string.display_cards)
        DisplayMode.FOCUS ->
            stringResource(R.string.display_focus)
    }

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    body: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = MaterialTheme.shapes.small,
                    color =
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            null,
                            tint =
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.width(9.dp))

                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            body()
        }
    }
}
