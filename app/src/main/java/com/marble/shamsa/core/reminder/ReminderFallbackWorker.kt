package com.marble.shamsa.core.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marble.shamsa.core.data.ReminderRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderFallbackWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getString(ReminderScheduler.EXTRA_ID) ?: return Result.failure()
        repository.get(id)?.let(scheduler::showNotification)
        return Result.success()
    }
}
