package org.nearbyshops.whitelabelapp.publish

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.engage.service.AppEngageErrorCode
import com.google.android.engage.service.AppEngageException
import com.google.android.engage.service.AppEngagePublishStatusCode
import com.google.android.engage.service.PublishStatusRequest
import com.google.android.engage.shopping.service.AppEngageShoppingClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.tasks.await
import org.nearbyshops.whitelabelapp.API.CartStatsService
import org.nearbyshops.whitelabelapp.API.OrdersAPI.OrderService
import org.nearbyshops.whitelabelapp.DaggerComponentBuilder
import org.nearbyshops.whitelabelapp.Model.ModelRoles.User
import org.nearbyshops.whitelabelapp.Model.ModelStats.CartStats
import org.nearbyshops.whitelabelapp.Preferences.PrefLocation
import org.nearbyshops.whitelabelapp.Preferences.PrefLogin
import javax.inject.Inject

class EngageWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var cartStatsService: CartStatsService

    @Inject
    lateinit var orderService: OrderService

    private val client = AppEngageShoppingClient(context)
    private val clusterRequestFactory = ClusterRequestFactory()

    init {
        DaggerComponentBuilder.getInstance().netComponent.Inject(this)
    }

    override suspend fun doWork(): Result {
        if (!isServiceAvailable()) {
            Log.d("EngageWorker", "Engage Service not available")
            return Result.success()
        }

        val publishType = inputData.getString("PUBLISH_TYPE")
        return when (publishType) {
            "SHOPPING_CART" -> publishShoppingCart()
            "ORDER_TRACKING" -> publishOrderTracking()
            else -> Result.failure()
        }
    }

    private suspend fun isServiceAvailable(): Boolean {
        return try {
            client.isServiceAvailable.await()
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun publishShoppingCart(): Result {
        val user: User? = PrefLogin.getUser(applicationContext)
        if (user == null) {
            Log.d("EngageWorker", "User not logged in, skipping publish")
            return Result.success()
        }

        return try {
            val response = cartStatsService.getCartStatsList(
                user.userID, null, null, true,
                PrefLocation.getLatitudeSelected(applicationContext),
                PrefLocation.getLongitudeSelected(applicationContext)
            ).execute()

            if (response.isSuccessful && response.body() != null) {
                val carts = response.body()!!
                val publishTask: Task<Void> = client.publishShoppingCart(
                    clusterRequestFactory.constructShoppingCartRequest(carts)
                )
                publishAndProvideResult(publishTask, AppEngagePublishStatusCode.PUBLISHED)
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("EngageWorker", "Error fetching carts", e)
            Result.retry()
        }
    }

    private suspend fun publishOrderTracking(): Result {
        val user: User? = PrefLogin.getUser(applicationContext)
        if (user == null) {
            Log.d("EngageWorker", "User not logged in, skipping publish")
            return Result.success()
        }

        return try {
            val response = orderService.getOrdersForEndUser(
                PrefLogin.getAuthorizationHeader(applicationContext),
                null, user.userID, null, null, null, null,
                true, false, true, false, null, null, 10, 0, false, false
            ).execute()

            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!.results
                if (orders != null && orders.isNotEmpty()) {
                    val publishTask: Task<Void> = client.publishShoppingOrderTrackingCluster(
                        clusterRequestFactory.constructOrderTrackingRequest(orders[0])
                    )
                    publishAndProvideResult(publishTask, AppEngagePublishStatusCode.PUBLISHED)
                } else {
                    client.deleteShoppingOrderTrackingCluster().await()
                    Result.success()
                }
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("EngageWorker", "Error fetching orders", e)
            Result.retry()
        }
    }

    private suspend fun publishAndProvideResult(
        publishTask: Task<Void>,
        publishStatusCode: Int
    ): Result {
        setPublishStatusCode(publishStatusCode)

        return try {
            publishTask.await()
            Result.success()
        } catch (publishException: Exception) {
            if (publishException is AppEngageException) {
                logPublishing(publishException)
                if (isErrorRecoverable(publishException)) Result.retry() else Result.failure()
            } else {
                Result.failure()
            }
        }
    }

    private fun setPublishStatusCode(statusCode: Int) {
        client.updatePublishStatus(
            PublishStatusRequest.Builder().setStatusCode(statusCode).build()
        )
    }

    private fun logPublishing(publishingException: AppEngageException) {
        val logMessage = when (publishingException.errorCode) {
            AppEngageErrorCode.SERVICE_NOT_FOUND -> "SERVICE_NOT_FOUND"
            AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE -> "SERVICE_CALL_EXECUTION_FAILURE"
            AppEngageErrorCode.SERVICE_NOT_AVAILABLE -> "SERVICE_NOT_AVAILABLE"
            AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED -> "SERVICE_CALL_PERMISSION_DENIED"
            AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT -> "SERVICE_CALL_INVALID_ARGUMENT"
            AppEngageErrorCode.SERVICE_CALL_INTERNAL -> "SERVICE_CALL_INTERNAL"
            AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED -> "SERVICE_CALL_RESOURCE_EXHAUSTED"
            else -> "An unknown error has occurred"
        }
        Log.d("EngageWorker", logMessage)
    }

    private fun isErrorRecoverable(publishingException: AppEngageException): Boolean {
        return when (publishingException.errorCode) {
            AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
            AppEngageErrorCode.SERVICE_CALL_INTERNAL,
            AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED -> true
            else -> false
        }
    }
}
