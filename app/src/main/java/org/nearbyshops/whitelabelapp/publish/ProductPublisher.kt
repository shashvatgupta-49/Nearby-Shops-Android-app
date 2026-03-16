package org.nearbyshops.whitelabelapp.publish

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object ProductPublisher {
    private const val WORK_NAME_PERIODIC = "EngageProductPeriodic"
    private const val WORK_NAME_ONE_TIME = "EngageProductOneTime"

    fun publishPeriodically(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<EngageWorker>(24, TimeUnit.HOURS)
            .setInputData(workDataOf("PUBLISH_TYPE" to "RECOMMENDATIONS"))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun publishOneTime(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<EngageWorker>()
            .setInputData(workDataOf("PUBLISH_TYPE" to "RECOMMENDATIONS"))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
