package org.nearbyshops.whitelabelapp.publish

import android.net.Uri
import com.google.android.engage.common.datamodel.Image
import com.google.android.engage.shopping.datamodel.ShoppingCart
import com.google.android.engage.shopping.datamodel.ShoppingOrderTrackingCluster
import com.google.android.engage.shopping.datamodel.ShoppingOrderType
import org.nearbyshops.whitelabelapp.Model.ModelCartOrder.Order
import org.nearbyshops.whitelabelapp.Model.ModelStats.CartStats
import org.nearbyshops.whitelabelapp.MyApplication
import org.nearbyshops.whitelabelapp.Preferences.PrefGeneral
import org.nearbyshops.whitelabelapp.Model.ModelStatusCodes.OrderStatusHomeDelivery

object ItemToEntityConverter {
    fun convert(cartStats: CartStats): ShoppingCart {
        val shop = cartStats.shop
        val serverUrl = PrefGeneral.getServerURL(MyApplication.getAppContext())
        val logoUrl = if (shop?.logoImagePath != null) {
            "$serverUrl/images/${shop.logoImagePath}"
        } else {
            null
        }

        val builder = ShoppingCart.Builder()
            .setTitle(shop?.shopName ?: "Shopping Cart")
            .setNumberOfItems(cartStats.itemsInCart)
            .setActionText("View Cart")
            .setActionLinkUri(Uri.parse("nearbyshops://cart/${cartStats.shopID}"))

        if (logoUrl != null) {
            builder.addPosterImage(
                Image.Builder()
                    .setImageUri(Uri.parse(logoUrl))
                    .setAccessibilityText(shop?.shopName ?: "Shop Logo")
                    .build()
            )
        }

        return builder.build()
    }

    fun convert(order: Order): ShoppingOrderTrackingCluster {
        val shop = order.shop
        val serverUrl = PrefGeneral.getServerURL(MyApplication.getAppContext())
        val logoUrl = if (shop?.logoImagePath != null) {
            "$serverUrl/images/${shop.logoImagePath}"
        } else {
            null
        }

        val statusString = OrderStatusHomeDelivery.getStatusString(order.statusCurrent)
        val orderType = if (order.deliveryMode == Order.DELIVERY_MODE_PICKUP_FROM_SHOP) {
            ShoppingOrderType.TYPE_IN_STORE_PICKUP
        } else {
            ShoppingOrderType.TYPE_SAME_DAY_DELIVERY
        }

        val builder = ShoppingOrderTrackingCluster.Builder()
            .setTitle(shop?.shopName ?: "Order #${order.orderID}")
            .setStatus(statusString)
            .setOrderTime(order.dateTimePlaced?.time ?: System.currentTimeMillis())
            .setShoppingOrderType(orderType)
            .setNumberOfItems(order.itemCount)
            .setOrderDescription("${order.itemCount} items from ${shop?.shopName}")
            .setActionLinkUri(Uri.parse("nearbyshops://order_detail/${order.orderID}"))

        if (logoUrl != null) {
            builder.addPosterImage(
                Image.Builder()
                    .setImageUri(Uri.parse(logoUrl))
                    .setAccessibilityText(shop?.shopName ?: "Shop Logo")
                    .build()
            )
        }

        return builder.build()
    }
}
