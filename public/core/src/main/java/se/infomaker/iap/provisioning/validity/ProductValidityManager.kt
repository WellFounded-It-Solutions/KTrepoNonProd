package se.infomaker.iap.provisioning.validity

import se.infomaker.iap.provisioning.LoginManager
import se.infomaker.iap.provisioning.ProvisioningManager
import se.infomaker.iap.provisioning.billing.BillingManager
import se.infomaker.iap.provisioning.firebase.ProductValidityWatcher
import se.infomaker.iap.provisioning.permission.PermissionManager
import timber.log.Timber

class ProductValidityManager(
        private val provisioningManager: ProvisioningManager,
        private val loginManager: LoginManager,
        private val billingManager: BillingManager?,
        private val permissionManager: PermissionManager,
        appStartPaywallPermission: String?
) {
    private val watchers = mutableListOf<ProductValidityWatcher>()

    init {
        val appStartProducts = appStartPaywallPermission?.let { permissionManager.productsForPermission(it) } ?: emptyList()

        if (appStartProducts.isNotEmpty()) {
            val checkAndNotify = {
                Timber.d("Watcher triggered check")
                provisioningManager.checkPermissionToPassPaywall({ hasAccess ->
                    if (hasAccess) {
                        Timber.d("User still has access")
                    }
                    else {
                        Timber.d("App start permission was revoked")
                        provisioningManager.onAppStartPermissionRevokedListener?.invoke()
                    }
                }, {
                    Timber.e(it, "Failed to check permission to pass paywall")
                })
            }
            addWatchers(appStartProducts, checkAndNotify)
        }

        (permissionManager.allProducts - appStartProducts).let {
            if (it.isNotEmpty()) {
                val check = {
                    Timber.d("Watcher triggered check")
                    billingManager?.checkCurrentProducts({}, {})
                    loginManager.checkCurrentProducts({}, {})
                }
                addWatchers(it, check)
            }
        }
    }

    private fun addWatchers(products: List<String>, onNoLongerValid: () -> Unit) {
        billingManager?.let {
            watchers.add(ProductValidityWatcher(it.currentProducts(), products, onNoLongerValid))
        }
        watchers.add(ProductValidityWatcher(loginManager.currentProducts(), products, onNoLongerValid))
    }

    fun start() = watchers.forEach { it.start() }

    fun stop() = watchers.forEach { it.stop() }
}