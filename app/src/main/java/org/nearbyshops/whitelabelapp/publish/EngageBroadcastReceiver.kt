package org.nearbyshops.whitelabelapp.publish

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.engage.shopping.service.AppEngageShoppingClient

class EngageBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        val action = intent.action
        if (action == "com.google.android.engage.action.shopping.PUBLISH_SHOPPING_CART") {
            ShoppingCartPublisher.publishOneTime(context)
        } else if (action == "com.google.android.engage.action.shopping.PUBLISH_SHOPPING_ORDER_TRACKING_CLUSTER") {
            OrderPublisher.publishOneTime(context)
        } else if (action == "com.google.android.engage.action.PUBLISH_RECOMMENDATION") {
            ProductPublisher.publishOneTime(context)
        }
    }
}
