package org.nearbyshops.whitelabelapp.publish

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object OrderPublisher {
    private const val WORK_NAME_PERIODIC = "EngageOrderPeriodic"
    private const val WORK_NAME_ONE_TIME = "EngageOrderOneTime"

    fun publishPeriodically(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<EngageWorker>(24, TimeUnit.HOURS)
            .setInputData(workDataOf("PUBLISH_TYPE" to "ORDER_TRACKING"))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun publishOneTime(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<EngageWorker>()
            .setInputData(workDataOf("PUBLISH_TYPE" to "ORDER_TRACKING"))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
