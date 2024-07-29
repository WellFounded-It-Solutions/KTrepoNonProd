package se.infomaker.iap.provisioning.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import se.infomaker.iap.provisioning.LoginManager
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import se.infomaker.iap.provisioning.backend.FunctionResult
import se.infomaker.iap.provisioning.backend.LinkAccountResponse
import se.infomaker.iap.provisioning.backend.ProductValidity
import se.infomaker.iap.provisioning.billing.BillingManager
import timber.log.Timber
import java.net.SocketTimeoutException
import java.util.*


class InlinePaywallViewModel(application: Application) : AndroidViewModel(application) {
    var provisioningManager = ProvisioningManagerProvider.provide(application)

    private var billingManager : BillingManager? = null
    private var loginManager: LoginManager? = null
    private val skuDetails = MutableLiveData<List<SkuDetails>>()

    private val loginStatus = MutableLiveData<LoginStatus>()
    private val hasValidProduct = BehaviorRelay.createDefault(false)

    private val viewState = MutableLiveData<ViewState>()
    private val isLoading = MutableLiveData<Boolean>()

    private var isLinkingAccount = false
    private val garbage: CompositeDisposable = CompositeDisposable()
    private val permission = "premium" // TODO

    init {
        loginManager = provisioningManager.loginManager()
        billingManager = provisioningManager.billingManager()
        viewState.value = ViewState.PURCHASE
        val productProviders = mutableListOf<Observable<List<ProductValidity>>>()
        billingManager?.let {
            productProviders.add(it.currentProducts())
        }
        loginManager?.let {
            productProviders.add(it.currentProducts())
        }

        garbage.add(Observable.combineLatest(productProviders) { combined ->
            var result = mutableSetOf<String>()
            combined.forEach { any ->
                val list = any as List<ProductValidity>
                result = result.union(list.filter { it.validTo.after(Date()) }.map { it.name }).toMutableSet()
            }
            return@combineLatest result
        }.subscribe {
            hasValidProduct.accept(provisioningManager.canPassInlinePaywall(permission, it))
        })

        loginManager?.apply {
            garbage.add(loginStatus().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                loginStatus.value = it
                updateUI()
            })
        }

        val uiTriggers = mutableListOf<Observable<Boolean>>().apply{
            add(hasValidProduct)
            billingManager?.let {
                add(it.hasLoadedCurrentProducts())
                add(it.waitingForAvailableSkuDetails())
            }
            loginManager?.let {
                add(it.fetchingProducts())
            }
        }

        garbage.add(Observable.merge(uiTriggers).subscribe {
            updateUI()
        })
        billingManager?.apply {
            garbage.add(skuDetails().subscribe {
                skuDetails.value = it
            })
        }
    }

    private fun updateProgressViewVisibility() {
        var visible = false
        visible = visible || loginManager?.getLoginStatus() == LoginStatus.IN_PROGRESS
        billingManager?.apply {
            visible = visible || !currentProductsLoaded()
            visible = visible ||isWaitingForAvailableSkuDetails()
        }
        loginManager?.apply {
            visible = visible || isFetchingProducts()
        }

        visible = visible || isLinkingAccount

        isLoading.value = visible
    }

    fun skuDetails() : LiveData<List<SkuDetails>> = skuDetails

    override fun onCleared() {
        super.onCleared()
        garbage.clear()
    }

    fun startPurchaseFlow(activity: Activity, skuDetail: SkuDetails) {
        billingManager?.startPurchase(activity, skuDetail)
    }

    fun loginEnabled(): Boolean = true//provisioningManager.loginEnabled()
    fun purchasesEnabled(): Boolean = false//provisioningManager.purchasesEnabled()

    private fun updateUI() {
        if (hasValidProduct.value == true) {
            loginManager?.let {
                val needsLinking = needsLinking()

                if (it.getLoginStatus() == LoginStatus.LOGGED_IN && !needsLinking) {
                    switchState(ViewState.SHOW_CONTENT)
                }
                else if (it.getLoginStatus() == LoginStatus.LOGGED_OUT && needsLinking) {
                    switchState(ViewState.CREATE_ACCOUNT)
                }
                else if (it.getLoginStatus() == LoginStatus.IN_PROGRESS && needsLinking) {
                    // Chill, you'll get there soon ;)
                }
                else if (it.userHasOptedOut) {
                    switchState(ViewState.SHOW_CONTENT)
                }
                else if (needsLinking){
                    if (!isLinkingAccount) {
                        isLinkingAccount = true
                        switchState(ViewState.LINK_SUBSCRIPTION)
                        linkCurrentAccount({
                            isLinkingAccount = false
                            Timber.d("Linked account successfully")
                            updateUI()
                        }, {
                            isLinkingAccount = false
                            Timber.w(it,"Failed to link account")
                            updateUI()
                        })
                    }
                }
                else {
                    switchState(ViewState.SHOW_CONTENT)
                }
            }
            if (loginManager == null) {
                switchState(ViewState.SHOW_CONTENT)
            }
        }
        else if (loginManager?.getLoginStatus() == LoginStatus.LOGGED_IN) {
            switchState(ViewState.PURCHASE)
        }
        updateProgressViewVisibility()
    }

    private fun needsLinking(): Boolean {
        if (!provisioningManager.purchasesEnabled()) {
            return false
        }
        val billingProducts = billingManager?.currentProducts()?.blockingFirst()
        val availableProducts = billingProducts?.asSequence()?.filter { it.validTo.after(Date()) }?.map { it.name }?.toList()
        if (billingProducts?.isNotEmpty() == true && provisioningManager.canPassInlinePaywall(permission, availableProducts)) {
            return loginManager?.isLinked != true
        }
        return false
    }

    fun loginStatus() : LiveData<LoginStatus> = loginStatus

    private fun <T> Collection<T>.containsAny(collection: Collection<T>): Boolean {
        for (item in this) {
            if (collection.contains(item)) {
                return true
            }
        }
        return false
    }

    private fun switchState(state: ViewState) {
        if (viewState.value != state) {
            viewState.value = state
        }
    }

    fun linkCurrentAccount(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val purchase = billingManager?.getLastPurchase()
        if (purchase == null) {
            viewState.value = ViewState.PURCHASE
            onError.invoke(Exception("No purchase token available"))
            return
        }

        else if (loginManager?.getLoginStatus() != LoginStatus.LOGGED_IN){
            viewState.value = ViewState.CREATE_ACCOUNT
            onError.invoke(Exception("User is not logged in"))
            return
        }
        linkAccountToPurchase(purchase, onSuccess, onError)
    }

    private fun linkAccountToPurchase(purchase: Purchase, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        var retryCount = 3
        var link: (() -> Unit)? = null
        isLinkingAccount = true
        updateProgressViewVisibility()
        link = {
            provisioningManager.linkAccount().subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe { _: FunctionResult<LinkAccountResponse>?, error: Throwable? ->
                if (error is SocketTimeoutException) {
                    Timber.d("Retrying linking")
                    if (retryCount-- > 0) {
                        link?.invoke()
                        return@subscribe
                    }
                }
                when {
                    error != null -> onError.invoke(error)
                    else -> {
                        loginManager?.isLinked = true
                        onSuccess.invoke()
                        updateUI()
                    }
                }
                isLinkingAccount = false
                updateProgressViewVisibility()
            }
        }
        link.invoke()
    }

    fun createAndLinkAccount(email: String, password: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val purchase = billingManager?.getLastPurchase()
        if (purchase == null) {
            viewState.value = ViewState.PURCHASE
            onError.invoke(Exception("No purchase token available"))

            return
        }
        isLinkingAccount = true

        updateProgressViewVisibility()
        loginManager?.createAccount(email, password, onAccountCreated = { _ ->
            var loginRetryCount = 3
            var login : (()-> Unit)? = null
            login = {
                loginManager?.login(email, password, {
                    linkAccountToPurchase(purchase, onSuccess, onError)
                }, onError = {
                    if (it is SocketTimeoutException) {
                        Timber.d("Retrying login")
                        if (loginRetryCount-- > 0) {
                            login?.invoke()
                            return@login
                        }
                    }
                    isLinkingAccount = false
                    updateProgressViewVisibility()
                    onError.invoke(it)
                })
            }
            login.invoke()
        }, onError = {
            isLinkingAccount = false
            updateProgressViewVisibility()
            onError.invoke(it)
        })
    }

    fun setUserOptOutFromLogin(value: Boolean) {
        loginManager?.userHasOptedOut = value
    }

    fun logout(activity: Activity) = loginManager?.logout(activity) {}

    fun viewState() : LiveData<ViewState> = viewState
    fun isLoading() : LiveData<Boolean> = isLoading
    fun startLogin(activity: Activity) {
        loginManager?.showLogin(activity)
    }
}