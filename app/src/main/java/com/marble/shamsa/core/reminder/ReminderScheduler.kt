package com.marble.shamsa.core.reminder

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.marble.shamsa.MainActivity
import com.marble.shamsa.R
import com.marble.shamsa.core.model.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        const val REMINDER_CHANNEL = "reminders"
        const val EXTRA_ID = "reminder_id"
    }

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    fun schedule(reminder: Reminder) {
        if (reminder.dueAtMillis <= System.currentTimeMillis()) return

        val pi = alarmPendingIntent(reminder.id)
        val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (exactAllowed) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.dueAtMillis, pi)
                // If this reminder previously used WorkManager while exact-alarm access was denied,
                // remove that fallback so the same reminder cannot fire twice after permission grant.
                WorkManager.getInstance(context).cancelUniqueWork(workName(reminder.id))
                return
            } catch (_: SecurityException) {
                // Special app access can change between the capability check and the API call.
                // Fall through to WorkManager instead of crashing or silently losing the reminder.
            }
        }

        enqueueFallback(reminder)
    }

    fun cancel(id: String) {
        alarmManager.cancel(alarmPendingIntent(id))
        WorkManager.getInstance(context).cancelUniqueWork(workName(id))
        NotificationManagerCompat.from(context).cancel(id.hashCode())
    }

    fun showNotification(reminder: Reminder) {
        if (!canPostNotifications()) return

        val open = PendingIntent.getActivity(
            context, reminder.id.hashCode(),
            Intent(context, MainActivity::class.java).putExtra(EXTRA_ID, reminder.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val fullScreen = PendingIntent.getActivity(
            context, reminder.id.hashCode() xor 0xABCD,
            Intent(context, ReminderRingActivity::class.java).putExtra(EXTRA_ID, reminder.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val complete = PendingIntent.getBroadcast(
            context, reminder.id.hashCode() xor 0x1111,
            Intent(context, ReminderAlarmReceiver::class.java).setAction("complete").putExtra(EXTRA_ID, reminder.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snooze = PendingIntent.getBroadcast(
            context, reminder.id.hashCode() xor 0x2222,
            Intent(context, ReminderAlarmReceiver::class.java).setAction("snooze").putExtra(EXTRA_ID, reminder.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(reminder.title)
            .setContentText(reminder.notes.ifBlank { context.getString(R.string.ring_title) })
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(open)
            .addAction(0, context.getString(R.string.complete), complete)
            .addAction(0, context.getString(R.string.snooze), snooze)

        if (reminder.popupEnabled && canUseFullScreen()) {
            builder.setFullScreenIntent(fullScreen, true)
        }

        try {
            NotificationManagerCompat.from(context).notify(reminder.id.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS can be revoked between our check and notify().
            // The reminder remains safely stored and the app does not crash.
        }
    }

    fun exactAlarmSettingsIntent(): Intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))

    fun fullScreenSettingsIntent(): Intent? = if (Build.VERSION.SDK_INT >= 34) {
        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${context.packageName}"))
    } else null

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun enqueueFallback(reminder: Reminder) {
        // Ensure an old exact PendingIntent cannot survive a switch to fallback scheduling.
        alarmManager.cancel(alarmPendingIntent(reminder.id))
        val delay = (reminder.dueAtMillis - System.currentTimeMillis()).coerceAtLeast(0)
        val work = OneTimeWorkRequestBuilder<ReminderFallbackWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(EXTRA_ID to reminder.id))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(reminder.id), ExistingWorkPolicy.REPLACE, work)
    }

    private fun canUseFullScreen(): Boolean {
        if (Build.VERSION.SDK_INT < 34) return true
        return context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
    }

    private fun workName(id: String) = "reminder-$id"

    private fun alarmPendingIntent(id: String) = PendingIntent.getBroadcast(
        context, id.hashCode(),
        Intent(context, ReminderAlarmReceiver::class.java).setAction("fire").putExtra(EXTRA_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
