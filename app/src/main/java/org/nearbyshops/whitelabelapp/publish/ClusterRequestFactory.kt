package org.nearbyshops.whitelabelapp.publish

import com.google.android.engage.shopping.service.PublishShoppingCartClusterRequest
import org.nearbyshops.whitelabelapp.Model.ModelStats.CartStats

class ClusterRequestFactory {
    fun constructShoppingCartRequest(carts: List<CartStats>): PublishShoppingCartClusterRequest {
        val builder = PublishShoppingCartClusterRequest.Builder()
        for (cart in carts) {
            builder.setShoppingCart(ItemToEntityConverter.convert(cart))
        }
        return builder.build()
    }
}
