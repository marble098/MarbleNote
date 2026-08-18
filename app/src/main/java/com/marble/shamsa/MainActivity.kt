package com.marble.shamsa

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marble.shamsa.core.design.ShamsaTheme
import com.marble.shamsa.core.reminder.ReminderScheduler
import com.marble.shamsa.ui.ShamsaAppNav
import com.marble.shamsa.ui.onboarding.OnboardingScreen
import com.marble.shamsa.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var scheduler: ReminderScheduler

    private val notificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val vm: MainViewModel = hiltViewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            val driveUi by vm.driveUi.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()

            val driveResolution =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.StartIntentSenderForResult()
                ) { result ->
                    val data = result.data

                    /*
                     * IMPORTANT:
                     * Google can return an error payload with RESULT_CANCELED.
                     * Decode any returned Intent first so DEVELOPER_ERROR,
                     * network errors, etc. are not mislabeled as user cancel.
                     */
                    if (data != null) {
                        try {
                            val authorization =
                                vm.authorizationFromIntent(this, data)

                            if (authorization != null) {
                                vm.acceptAuthorization(authorization)
                            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                                vm.driveAuthorizationCancelled()
                            } else {
                                vm.driveAuthorizationFailed(
                                    IllegalStateException(
                                        "Google returned an empty authorization result."
                                    )
                                )
                            }
                        } catch (t: Throwable) {
                            vm.driveAuthorizationFailed(t)
                        }
                    } else if (result.resultCode == Activity.RESULT_CANCELED) {
                        vm.driveAuthorizationCancelled()
                    } else {
                        vm.driveAuthorizationFailed(
                            IllegalStateException(
                                "Google authorization returned no result Intent."
                            )
                        )
                    }
                }

            fun connectDrive() {
                scope.launch {
                    vm.driveAuthorizationStarted()

                    try {
                        val result =
                            vm.beginDriveAuthorization(this@MainActivity)

                        val pending = result.pendingIntent

                        if (pending != null) {
                            driveResolution.launch(
                                IntentSenderRequest.Builder(pending)
                                    .build()
                            )
                        } else if (result.accessToken != null) {
                            vm.acceptAuthorization(result)
                        } else {
                            vm.driveAuthorizationFailed(
                                IllegalStateException(
                                    "Google authorization returned neither " +
                                        "an access token nor a resolution."
                                )
                            )
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (t: Throwable) {
                        vm.driveAuthorizationFailed(t)
                    }
                }
            }

            LaunchedEffect(settings.language) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(settings.language)
                )
            }

            ShamsaTheme(settings.themeMode) {
                if (!settings.onboardingComplete) {
                    OnboardingScreen(
                        currentLanguage = settings.language,
                        driveUi = driveUi,
                        onLanguage = vm::setLanguage,
                        onNotifications = {
                            if (Build.VERSION.SDK_INT >= 33) {
                                notificationPermission.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                        },
                        onExactAlarm = {
                            runCatching {
                                startActivity(
                                    scheduler.exactAlarmSettingsIntent()
                                )
                            }
                        },
                        onFullScreen = {
                            scheduler.fullScreenSettingsIntent()?.let {
                                runCatching { startActivity(it) }
                            }
                        },
                        onDrive = ::connectDrive,
                        onFinish = vm::finishOnboarding
                    )
                } else {
                    ShamsaAppNav(
                        settings = settings,
                        viewModel = vm,
                        onDrive = ::connectDrive,
                        onExact = {
                            runCatching {
                                startActivity(
                                    scheduler.exactAlarmSettingsIntent()
                                )
                            }
                        },
                        onFullScreen = {
                            scheduler.fullScreenSettingsIntent()?.let {
                                runCatching { startActivity(it) }
                            }
                        },
                        initialReminderId =
                            intent.getStringExtra(ReminderScheduler.EXTRA_ID)
                    )
                }
            }
        }
    }
}
