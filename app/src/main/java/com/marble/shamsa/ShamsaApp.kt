package com.marble.shamsa

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.marble.shamsa.core.reminder.ReminderScheduler
import com.marble.shamsa.core.work.WorkBootstrap
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShamsaApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var workBootstrap: WorkBootstrap
    @Inject lateinit var scheduler: ReminderScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        createChannels()
        workBootstrap.ensurePeriodicSync()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    ReminderScheduler.REMINDER_CHANNEL,
                    getString(R.string.notification_channel_reminders),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.notification_channel_reminders_desc)
                    enableVibration(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    "cloud_sync",
                    getString(R.string.notification_channel_sync),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }
}
