package org.nearbyshops.whitelabelapp.publish

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object ShoppingCartPublisher {
    fun publishPeriodically(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<EngageWorker>(24, TimeUnit.HOURS)
            .setInputData(workDataOf("PUBLISH_TYPE" to "SHOPPING_CART"))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "EngagePeriodic",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun publishOneTime(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<EngageWorker>()
            .setInputData(workDataOf("PUBLISH_TYPE" to "SHOPPING_CART"))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "EngageOneTime",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
