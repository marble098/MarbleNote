package com.marble.shamsa.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marble.shamsa.core.cloud.DriveSyncManager
import com.marble.shamsa.core.cloud.SyncResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DriveSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val drive: DriveSyncManager
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = when (drive.syncCached()) {
        SyncResult.Success -> Result.success()
        SyncResult.NeedsAuthorization -> Result.success()
        is SyncResult.Failure -> if (runAttemptCount < 3) Result.retry() else Result.failure()
    }
}
