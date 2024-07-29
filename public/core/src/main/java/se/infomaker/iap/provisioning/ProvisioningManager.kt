package se.infomaker.iap.provisioning

import android.app.Activity
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.Single
import se.infomaker.iap.provisioning.backend.FunctionResult
import se.infomaker.iap.provisioning.backend.LinkAccountResponse
import se.infomaker.iap.provisioning.billing.BillingManager

/**
 * Provides convenience methods to handle the users
 */
interface ProvisioningManager {
    /**
     * @return true if the app has a paywall before starting the app
     */
    fun hasAppStartPaywall(): Boolean

    /**
     * @return true if the app could be started with any of the products
     */
    fun canStartAppWithProducts(products: Collection<String>?): Boolean

    /**
     * @return a set of available products
     */
    fun availableProducts(): Observable<Set<String>>

    /**
     * Async function to check if the user has permission to pass paywall
     */
    fun checkPermissionToPassPaywall(onResult: (Boolean) -> Unit, onError:(Throwable) -> Unit)

    /**
     * Present the app start paywall
     */
    fun presentAppStartPaywall(from: Activity, onComplete: () -> Unit)

    /**
     * Create a fragment that can be displayed on top of other content as a paywall
     */
    fun createPaywallFragment(from: Activity, headerLayout: Int?): Fragment

    /**
     * manager to handle login if available
     */
    fun loginManager(): LoginManager?

    /**
     * manager to handle login if available
     */
    fun billingManager(): BillingManager?

    /**
     * link account
     */
    fun linkAccount(): Single<FunctionResult<LinkAccountResponse>>

    /**
     * Determines whether a user can access protected content based on configured permissions
     */
    fun canDisplayContentWithPermissions(permission:List<String>):Observable<Boolean>

    /**
     * Determines whether a user can access protected content
     */
    fun canDisplayContentWithPermission(permission:String):Observable<Boolean>

    /**
     * Notified if the users permission is revoked
     */
    var onAppStartPermissionRevokedListener: (() -> Unit)?

    /**
     * Returns a an auth token
     */
    fun getAuthToken(): Single<String>

    /**
     *  Determines whether a client can move beyond an inline paywall
     */
    fun canPassInlinePaywall(permission: String?, products: Collection<String>?): Boolean

    /**
     * Flag to denote whether login is enabled
     */
    fun loginEnabled(): Boolean

    /**
     * Flag to denote whether in-app purchase is enabled
     */
    fun purchasesEnabled(): Boolean
}
