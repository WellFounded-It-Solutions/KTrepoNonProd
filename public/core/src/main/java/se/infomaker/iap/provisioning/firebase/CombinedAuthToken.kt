package se.infomaker.iap.provisioning.firebase

import android.util.Base64
import com.android.billingclient.api.Purchase
import org.json.JSONObject

data class CombinedAuthToken(val appId: String?, val idToken: String?, val purchase: Purchase?) {
    fun asToken(): String {
        return if (idToken.isNullOrEmpty() && purchase == null) {
            ""
        }
        else {
            val jsonObject = JSONObject()
            appId?.let {
                jsonObject.put("appId", appId)
            }

            idToken?.let {
                jsonObject.put("idToken", idToken)
            }
            purchase?.purchaseToken?.let { purchaseToken ->
                jsonObject.put("purchaseToken", purchaseToken)
            }
            "#" + Base64.encodeToString(jsonObject.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT)
        }
    }
}