package com.marble.shamsa.core.work

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.marble.shamsa.widget.ShamsaWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkBootstrap @Inject constructor(@ApplicationContext private val context: Context) {
    fun ensurePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<DriveSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("drive-periodic", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun enqueueCloudSync() {
        val request = OneTimeWorkRequestBuilder<DriveSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("drive-immediate", ExistingWorkPolicy.REPLACE, request)
    }

    fun updateWidgets() {
        CoroutineScope(Dispatchers.IO).launch { ShamsaWidget().updateAll(context) }
    }
}
