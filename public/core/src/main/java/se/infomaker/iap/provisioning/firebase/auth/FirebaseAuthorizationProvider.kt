package se.infomaker.iap.provisioning.firebase.auth

import se.infomaker.iap.provisioning.backend.Backend
import se.infomaker.iap.provisioning.billing.BillingManager
import se.infomaker.iap.provisioning.firebase.FirebaseLoginManager
import com.navigaglobal.mobile.auth.AuthorizationProvider
import timber.log.Timber

class FirebaseAuthorizationProvider(val backend: Backend, val billingManager: BillingManager?, val loginManager: FirebaseLoginManager) : AuthorizationProvider {
    private var accessToken: AccessToken? = null

    override fun getAuthorization(): String? {
        accessToken?.let { token ->
            if (token.isValid()) {
                return token.authHeader()
            }
        }
        // TODO after updates to billing version sku is not longer supported with 4 and above
        // for now i am just using 1st element returned, cann't test it right now must be changes in future.
        val request = billingManager?.getLastPurchase()?.let {purchase ->
            backend.getAccessToken(purchase.purchaseToken, purchase.skus[0])
        } ?: backend.getAccessToken()

        try {
            val result = request.blockingGet()
            result?.body?.let { token ->
                accessToken = token
                return token.authHeader()
            }

        } catch (e: Exception) {
            Timber.w(e, "Failed to get token")
        }
        return null
    }
}