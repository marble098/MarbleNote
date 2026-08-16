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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var scheduler: ReminderScheduler

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: MainViewModel = hiltViewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            val driveResolution = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    vm.authorizationFromIntent(this, result.data)?.let(vm::acceptAuthorization)
                }
            }

            fun connectDrive() {
                scope.launch {
                    runCatching { vm.beginDriveAuthorization(this@MainActivity) }.onSuccess { result ->
                        val pending = result.pendingIntent
                        if (pending != null) driveResolution.launch(IntentSenderRequest.Builder(pending).build()) else vm.acceptAuthorization(result)
                    }
                }
            }

            LaunchedEffect(settings.language) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(settings.language))
            }

            LaunchedEffect(settings.driveConnected) {
                if (settings.driveConnected) {
                    runCatching { vm.beginDriveAuthorization(this@MainActivity) }.getOrNull()?.let { result ->
                        if (result.pendingIntent == null && result.accessToken != null) vm.acceptAuthorization(result)
                    }
                }
            }

            ShamsaTheme(settings.themeMode) {
                if (!settings.onboardingComplete) {
                    OnboardingScreen(
                        currentLanguage = settings.language,
                        onLanguage = vm::setLanguage,
                        onNotifications = { if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        onExactAlarm = { runCatching { startActivity(scheduler.exactAlarmSettingsIntent()) } },
                        onFullScreen = { scheduler.fullScreenSettingsIntent()?.let { runCatching { startActivity(it) } } },
                        onDrive = ::connectDrive,
                        onFinish = vm::finishOnboarding
                    )
                } else {
                    ShamsaAppNav(
                        settings = settings,
                        viewModel = vm,
                        onDrive = ::connectDrive,
                        onExact = { runCatching { startActivity(scheduler.exactAlarmSettingsIntent()) } },
                        onFullScreen = { scheduler.fullScreenSettingsIntent()?.let { runCatching { startActivity(it) } } },
                        initialReminderId = intent.getStringExtra(ReminderScheduler.EXTRA_ID)
                    )
                }
            }
        }
    }
}
