package se.infomaker.iap.provisioning

import android.app.Activity
import io.reactivex.Observable
import io.reactivex.Single
import se.infomaker.iap.provisioning.billing.BillingManager

class NoProvisioningProvisioningManager : ProvisioningManager {

    override var onAppStartPermissionRevokedListener: (() -> Unit)? = null

    override fun hasAppStartPaywall() = false

    override fun canStartAppWithProducts(products: Collection<String>?) = true

    override fun availableProducts(): Observable<Set<String>> = Observable.empty()

    override fun checkPermissionToPassPaywall(onResult: (Boolean) -> Unit, onError: (Throwable) -> Unit) { onResult.invoke(true) }

    override fun presentAppStartPaywall(from: Activity, onComplete: () -> Unit) =
        throw UnsupportedOperationException("There are no paywalls when no provisioning is active.")

    override fun createPaywallFragment(from: Activity, headerLayout: Int?) =
        throw UnsupportedOperationException("There are no paywalls when no provisioning is active.")

    override fun loginManager(): LoginManager? = null

    override fun billingManager(): BillingManager? = null

    override fun linkAccount() =
        throw UnsupportedOperationException("No accounts when no provisioning is active.")

    override fun canDisplayContentWithPermissions(permission: List<String>) = Observable.just(true)

    override fun canDisplayContentWithPermission(permission: String) = Observable.just(true)

    override fun getAuthToken() = Single.just("NoProvisioning")

    override fun canPassInlinePaywall(permission: String?, products: Collection<String>?) = true

    override fun loginEnabled() = false

    override fun purchasesEnabled() = false

    companion object {
        const val NAME = "noprovisioning"
    }
}