package se.infomaker.iap.provisioning.backend

import com.google.gson.annotations.SerializedName

data class SubscriptionInfo(@SerializedName("products") val _products: List<ProductValidity>?) {
    val products: List<ProductValidity>
    get() = _products ?: emptyList()
}
