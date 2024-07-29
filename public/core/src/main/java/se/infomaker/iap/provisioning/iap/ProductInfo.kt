package se.infomaker.iap.provisioning.iap

import com.android.billingclient.api.BillingClient
import com.google.gson.annotations.SerializedName

enum class ProductType(val value: String, val skuType: String) {
    @SerializedName("subscription")
    SUBSCRIPTION("subscription", BillingClient.SkuType.SUBS)
}

data class ProductInfo(val id: String, val name: String, val type: ProductType)
