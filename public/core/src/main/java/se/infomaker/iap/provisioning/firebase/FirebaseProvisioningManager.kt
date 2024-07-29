package se.infomaker.iap.provisioning.firebase

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ForegroundDetector
import se.infomaker.frtutilities.GlobalValueManager
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.Cancellable
import se.infomaker.iap.provisioning.LoginManager
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.ProductChecker
import se.infomaker.iap.provisioning.ProvisioningManager
import se.infomaker.iap.provisioning.backend.Backend
import se.infomaker.iap.provisioning.backend.BackendProvider
import se.infomaker.iap.provisioning.backend.FunctionResult
import se.infomaker.iap.provisioning.backend.LinkAccountResponse
import se.infomaker.iap.provisioning.backend.ProductValidity
import se.infomaker.iap.provisioning.billing.BillingManager
import se.infomaker.iap.provisioning.config.ProvisioningProviderConfig
import se.infomaker.iap.provisioning.credentials.CredentialManager
import se.infomaker.iap.provisioning.firebase.auth.FirebaseAuthorizationProvider
import se.infomaker.iap.provisioning.firebase.auth.LiveContentConfigDeserializer
import se.infomaker.iap.provisioning.firebase.auth.OpenContentUrlWrapper
import se.infomaker.iap.provisioning.permission.PermissionManager
import se.infomaker.iap.provisioning.store.SharedPreferenceStore
import se.infomaker.iap.provisioning.ui.PaywallActivity
import se.infomaker.iap.provisioning.ui.PaywallFragment
import se.infomaker.iap.provisioning.validity.ProductValidityManager
import com.navigaglobal.mobile.auth.AuthorizationProviderManager
import se.infomaker.frtutilities.ktx.isDebuggable
import timber.log.Timber
import java.util.Date
import android.util.Pair as UtilPair

class FirebaseProvisioningManager(context: Context, providerConfig: ProvisioningProviderConfig) : ProvisioningManager, Cancellable {

    private val loginManager: FirebaseLoginManager
    private var billingManager: BillingManager? = null
    private val permissionManager: PermissionManager
    private val backend: Backend
    private val provisioningManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val config: FirebaseProvisioningConfig = providerConfig.getConfig(FirebaseProvisioningConfig::class.java)
    private val garbage = CompositeDisposable()
    private val jobs = mutableListOf<Job>()

    val handler = Handler()
    var delayedStop: Runnable? = null

    override var onAppStartPermissionRevokedListener: (() -> Unit)? = null

    init {
        val useFirebaseEmulators = config.useLocalEmulators && context.isDebuggable
        backend = BackendProvider.get(config.appId, config.region, useFirebaseEmulators)

        loginManager = FirebaseLoginManager(
            customScheme = context.applicationContext.packageName + "://",
            store = SharedPreferenceStore(context.applicationContext.getSharedPreferences("login", Context.MODE_PRIVATE)),
            backend = backend,
            trackUserId = config.enableUserIdTracking,
            useFirebaseEmulators = useFirebaseEmulators
        )
        setupStateDistribution()

        if (config.enablePurchases == true) {
            val billingStore = SharedPreferenceStore(context.applicationContext.getSharedPreferences("billing", Context.MODE_PRIVATE))
            billingManager =  BillingManager(context.applicationContext, billingStore, backend).also {
                garbage.add(it.purchases()
                    // TODO after updates to billing version sku is not longer supported with 4 and above
                    // for now i am just using 1st element returned, cann't test it right now must be changes in future.
                        .withLatestFrom(it.skuDetails(), BiFunction<Purchase, List<SkuDetails>, StatisticsEvent> { purchase, skuDetails ->
                            val title = skuDetails.firstOrNull {
                                    details -> details.sku == purchase.skus[0]
                            }?.title
                            return@BiFunction StatisticsEvent.Builder().event("productBuy")
                                    .attribute("userIsLoggedIn", loginManager.getLoginStatus() == LoginStatus.LOGGED_IN)
                                    .attribute("productTitle", title)
                                    .attribute("productId", purchase.skus[0])
                                    .build()
                        }).subscribe { event ->
                            StatisticsManager.getInstance().logEvent(event)
                        })
            }
        }
        if (config.provideAccessTokens == true) {
            val gson = GsonBuilder().registerTypeAdapter(OpenContentUrlWrapper::class.java, LiveContentConfigDeserializer()).create()
            val openContentUrlWrapper = ConfigManager.getInstance(context).getConfig("shared", "ContentList", OpenContentUrlWrapper::class.java, gson)
            openContentUrlWrapper?.openContentUrl?.let { url ->
                AuthorizationProviderManager.set(url, FirebaseAuthorizationProvider(backend, billingManager, loginManager))
            }
        }

        permissionManager = PermissionManager(ResourceManager(context, "shared"))

        val validityManager = ProductValidityManager(this, loginManager, billingManager, permissionManager, config.appStartPaywallPermission)

        garbage.add(ForegroundDetector.observable().subscribe { isInForeground ->
            if (isInForeground) {
                delayedStop?.let {
                    handler.removeCallbacks(it)
                    delayedStop = null
                } ?: run {
                    Timber.d("Start monitoring provisioning")
                    loginManager.start()
                    billingManager?.start()
                    validityManager.start()
                }
            }
            else {
                delayedStop = Runnable {
                    Timber.d("Stopped monitoring provisioning")
                    delayedStop = null
                    loginManager.stop()
                    billingManager?.stop()
                    validityManager.stop()
                }.also {
                    handler.postDelayed(it, 5000)
                }
            }
        })
    }

    /**
     * Distribute all value changes to the global value manager
     */
    private fun setupStateDistribution() {
        garbage.add(loginManager.loginStatus().distinctUntilChanged().subscribe {
            val state = JSONObject()
            state.put("isLoggedIn", it == LoginStatus.LOGGED_IN)
            loginManager.getUserInfo()?.let { userInfo ->
                state.put("user", JSONObject().also { user ->
                    user.put("id", userInfo.userId)
                    user.put("displayName", userInfo.displayName)
                })
            }
            GlobalValueManager.put("PROVISIONING", state)
        })
    }

    override fun availableProducts(): Observable<Set<String>> {
        return billingManager?.let { billingManager ->
            Observable.combineLatest(billingManager.currentProducts(), loginManager.currentProducts()
                    , BiFunction<List<ProductValidity>, List<ProductValidity>, Set<String>> { first, second ->
                return@BiFunction validProductsForValidity(first + second)
            })
        } ?: loginManager.currentProducts()
                .map {
                    validProductsForValidity(it)
                }
    }

    private fun validProductsForValidity(productValidityList: List<ProductValidity>): Set<String> {
        val products = mutableSetOf<String>()
        val now = Date()
        productValidityList.filter { it.validTo.after(now) }.forEach {
            products.add(it.name)
        }
        return products
    }

    override fun canDisplayContentWithPermission(permission: String): Observable<Boolean> {
        val requiredProducts = permissionManager.productsForPermission(permission)
        return availableProducts().map { products ->
            return@map products.containsAny(requiredProducts)
        }
    }

    override fun canDisplayContentWithPermissions(permissions: List<String>): Observable<Boolean> {
        val requiredProducts = permissions.map {
            permissionManager.productsForPermission(it) ?: emptyList()
        }.flatten()
        return availableProducts().map { products ->
            return@map products.containsAny(requiredProducts)
        }
    }

    override fun getAuthToken(): Single<String> {
        return if (loginManager.getLoginStatus() == LoginStatus.LOGGED_IN) {
            loginManager.getAuthToken().flatMap { idToken ->

                Single.just(CombinedAuthToken(config.appId, idToken, billingManager?.getLastPurchase()).asToken())
            }
        }
        else {
            Single.just(CombinedAuthToken(config.appId, null, billingManager?.getLastPurchase()).asToken())
        }
    }
    // TODO after updates to billing version sku is not longer supported with 4 and above
    // for now i am just using 1st element returned, cann't test it right now must be changes in future.
    override fun linkAccount(): Single<FunctionResult<LinkAccountResponse>> {
        val purchase = billingManager?.getLastPurchase()
                ?: return Single.error(Exception("No purchase available"))
        return backend.linkAccount(purchase.purchaseToken, purchase.skus[0])
    }

    override fun canPassInlinePaywall(permission: String?, products: Collection<String>?): Boolean {

        val inlinePaywallPermission = permission ?: return true
        val requiredProducts = permissionManager.productsForPermission(inlinePaywallPermission)
        if (products == null) {
            return requiredProducts?.isEmpty() == true
        }
        return products.containsAny(requiredProducts)
    }

    override fun canStartAppWithProducts(products: Collection<String>?): Boolean {

        val appStartPaywallPermission = config.appStartPaywallPermission ?: return true
        val requiredProducts = permissionManager.productsForPermission(appStartPaywallPermission)
        if (products == null) {
            return requiredProducts?.isEmpty() == true
        }
        return products.containsAny(requiredProducts)
    }

    override fun checkPermissionToPassPaywall(onResult: (Boolean) -> Unit, onError: (Throwable) -> Unit) {

        val appStartPaywallPermission = config.appStartPaywallPermission
        if (appStartPaywallPermission == null) {
            onResult.invoke(true)
            return
        }

        val requiredProducts = permissionManager.productsForPermission(appStartPaywallPermission)

        if (requiredProducts.isNullOrEmpty()) {
            onResult.invoke(false)
            return
        }

        mutableListOf<ProductValidity>().let { list ->
            billingManager?.currentProducts()?.blockingLatest()?.let {
                list.addAll(it.first())
            }

            loginManager.currentProducts().blockingLatest()?.let {
                list.addAll(it.first())
            }
            if (list.filter { it.validTo.after(Date()) }.map { it.name }.containsAny(requiredProducts)) {
                Timber.d("Using cached result")
                onResult.invoke(true)
                return
            }
        }

        provisioningManagerScope.launch {
            ProductChecker(loginManager, billingManager).checkFor(requiredProducts, onResult, onError)
        }
    }

    override fun presentAppStartPaywall(from: Activity, onComplete: () -> Unit) {
        val onDone = {
            val shouldTransition = from.intent.getBooleanExtra("isRestart", false) or from.isTaskRoot
            if (shouldTransition) {
                CredentialManager.forceRefresh(from) {
                    val intent = Intent(from, PaywallActivity::class.java)
                    intent.putExtra("usingTransition", true)
                    val sharedElements = getSharedElements(from)
                    if (sharedElements.isNotEmpty()) {
                        val options = ActivityOptions.makeSceneTransitionAnimation(from, *sharedElements)
                        from.startActivity(intent, options.toBundle())
                        Handler().postDelayed({
                            onComplete()
                        }, 3000)
                    }
                    else {
                        displaySimpleAppStartPaywall(from, onComplete)
                    }
                }
            }
            else {
                displaySimpleAppStartPaywall(from, onComplete)
            }
        }

        jobs.add(provisioningManagerScope.launch {
            try {
                billingManager?.suspendStart()
                billingManager?.suspendUpdateAvailableProducts()
                if (isActive) {
                    onDone.invoke()
                }
                else {
                    Timber.d("Cancelled, will not show paywall.")
                }
            }
            catch (t: Throwable) {
                if (t !is CancellationException) {
                    onDone.invoke()
                }
            }
        })
    }

    private fun getSharedElements(from: Activity): Array<UtilPair<View, String>> {
        val parent = from.findViewById<FrameLayout>(android.R.id.content)
        val sharedElementChildren = getSharedElementChildren(parent)

        val array = arrayOfNulls<UtilPair<View, String>>(sharedElementChildren.size)
        return sharedElementChildren.toArray(array)
    }

    private fun getSharedElementChildren(from: ViewGroup): ArrayList<UtilPair<View, String>> {
        val children = arrayListOf<UtilPair<View, String>>()
        for (i in 0 until from.childCount) {
            val view = from.getChildAt(i)
            if (view is ViewGroup) {
                children.addAll(getSharedElementChildren(view))
            }
            else {
                ViewCompat.getTransitionName(view)?.let { transitionName ->
                    children.add(UtilPair.create(view, transitionName))
                }
            }
        }
        return children
    }

    private fun displaySimpleAppStartPaywall(from: Activity, onComplete: () -> Unit) {
        val intent = Intent(from, PaywallActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        from.startActivity(intent)
        onComplete()
    }

    override fun createPaywallFragment(from: Activity, headerLayout: Int?): Fragment {
        return PaywallFragment().apply {
            headerLayout?.let {
                arguments = Bundle().apply {
                    putInt(PaywallFragment.HEADER_LAYOUT, it)
                }
            }
        }
    }

    override fun cancel() {
        jobs.forEach {
            it.cancel()
        }
        jobs.clear()
    }

    override fun billingManager(): BillingManager? = billingManager
    override fun loginManager(): LoginManager? = loginManager
    override fun hasAppStartPaywall(): Boolean = config.appStartPaywallPermission != null
    override fun loginEnabled(): Boolean = config.enableLogin ?: false
    override fun purchasesEnabled(): Boolean = config.enablePurchases ?: false

    companion object {
        const val name = "firebase"
        const val checkPermissionTimeout = 10000L
        const val SHARED_TRANSITION_TIME = 200L
    }
}

internal fun <T> Collection<T>.containsAny(collection: Collection<T>?): Boolean {
    if (collection == null) {
        return false
    }
    for (item in this) {
        if (collection.contains(item)) {
            return true
        }
    }
    return false
}
