package org.nearbyshops.whitelabelapp.publish

import com.google.android.engage.shopping.service.PublishShoppingCartClusterRequest
import com.google.android.engage.shopping.service.PublishShoppingOrderTrackingClusterRequest
import org.nearbyshops.whitelabelapp.Model.ModelCartOrder.Order
import org.nearbyshops.whitelabelapp.Model.ModelStats.CartStats

class ClusterRequestFactory {
    fun constructShoppingCartRequest(carts: List<CartStats>): PublishShoppingCartClusterRequest {
        val builder = PublishShoppingCartClusterRequest.Builder()
        for (cart in carts) {
            builder.setShoppingCart(ItemToEntityConverter.convert(cart))
        }
        return builder.build()
    }

    fun constructOrderTrackingRequest(order: Order): PublishShoppingOrderTrackingClusterRequest {
        return PublishShoppingOrderTrackingClusterRequest.Builder()
            .setShoppingOrderTrackingCluster(ItemToEntityConverter.convert(order))
            .build()
    }
}
