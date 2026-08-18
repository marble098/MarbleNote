package com.marble.shamsa.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marble.shamsa.R
import com.marble.shamsa.ui.viewmodel.DriveUiState
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    currentLanguage: String,
    driveUi: DriveUiState,
    onLanguage: (String) -> Unit,
    onNotifications: () -> Unit,
    onExactAlarm: () -> Unit,
    onFullScreen: () -> Unit,
    onDrive: () -> Unit,
    onFinish: () -> Unit
) {
    val pager = rememberPagerState { 4 }
    val scope = rememberCoroutineScope()

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f),
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .28f)
                )
            )
        )
    ) {
        Column(
            Modifier.fillMaxSize().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("☀️", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(8.dp))
                Text("Shamsa", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text("${pager.currentPage + 1}/4", style = MaterialTheme.typography.labelLarge)
            }

            LinearProgressIndicator(
                progress = { (pager.currentPage + 1) / 4f },
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                HorizontalPager(
                    state = pager,
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> IntroPage(
                            Icons.Rounded.AutoAwesome,
                            "✨",
                            stringResource(R.string.onboarding_welcome),
                            stringResource(R.string.onboarding_subtitle)
                        )

                        1 -> Column(
                            Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            IntroPage(
                                Icons.Rounded.Language,
                                "🌐",
                                stringResource(R.string.choose_language),
                                "فارسی • English",
                                fill = false
                            )
                            Spacer(Modifier.height(20.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilterChip(
                                    selected = currentLanguage == "fa",
                                    onClick = { onLanguage("fa") },
                                    label = { Text("فارسی") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = currentLanguage == "en",
                                    onClick = { onLanguage("en") },
                                    label = { Text("English") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        2 -> Column(
                            Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IntroPage(
                                Icons.Rounded.NotificationsActive,
                                "🔔",
                                stringResource(R.string.permissions),
                                stringResource(R.string.permission_explain),
                                fill = false
                            )
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = onNotifications, Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.allow_notifications))
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(onClick = onExactAlarm, Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.allow_exact_alarms))
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(onClick = onFullScreen, Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.allow_fullscreen))
                            }
                        }

                        3 -> Column(
                            Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IntroPage(
                                Icons.Rounded.CloudSync,
                                "☁️",
                                stringResource(R.string.drive_sync),
                                stringResource(R.string.cloud_explain),
                                fill = false
                            )
                            Spacer(Modifier.height(20.dp))

                            if (driveUi.working) {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                                Spacer(Modifier.height(12.dp))
                            }

                            driveUi.message?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = if (driveUi.successful)
                                        MaterialTheme.colorScheme.tertiary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                            }

                            Button(
                                onClick = onDrive,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !driveUi.working
                            ) {
                                Icon(Icons.Rounded.Link, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.drive_connect))
                            }
                            TextButton(onClick = onFinish) { Text(stringResource(R.string.skip)) }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (pager.currentPage == 3) onFinish()
                    else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (pager.currentPage == 3)
                        stringResource(R.string.get_started)
                    else
                        stringResource(R.string.continue_label)
                )
                Spacer(Modifier.width(6.dp))
                Text("→")
            }
        }
    }
}

@Composable
private fun IntroPage(
    icon: ImageVector,
    emoji: String,
    title: String,
    subtitle: String,
    fill: Boolean = true
) {
    Column(
        modifier = (if (fill) Modifier.fillMaxSize() else Modifier.fillMaxWidth()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                icon,
                null,
                Modifier.padding(20.dp).size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
