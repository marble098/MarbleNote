package com.marble.shamsa.core.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.marble.shamsa.core.data.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: ReminderRepository
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val id = intent.getStringExtra(ReminderScheduler.EXTRA_ID) ?: return@launch
                when (intent.action) {
                    "complete" -> repository.complete(id)
                    "snooze" -> repository.snooze(id)
                    else -> repository.get(id)?.let(scheduler::showNotification)
                }
            } finally { pending.finish() }
        }
    }
}
