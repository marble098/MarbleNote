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
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var repository: ReminderRepository
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { repository.rescheduleAll() } finally { pending.finish() }
        }
    }
}
