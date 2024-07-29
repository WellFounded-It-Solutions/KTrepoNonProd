package se.infomaker.iap.provisioning.billing

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.google.gson.Gson
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.infomaker.iap.provisioning.backend.Backend
import se.infomaker.iap.provisioning.backend.FunctionResult
import se.infomaker.iap.provisioning.backend.ProductResponse
import se.infomaker.iap.provisioning.backend.ProductValidity
import se.infomaker.iap.provisioning.backend.SubscriptionInfo
import se.infomaker.iap.provisioning.store.KeyValueStore
import timber.log.Timber
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BillingManager(private val context: Context, private val store: KeyValueStore, val backend: Backend) : PurchasesUpdatedListener, BillingClientStateListener {
    private var billingClient: BillingClient? = null
    var billingReady: Boolean = false
    private val hasLoadedCurrentProducts = BehaviorRelay.createDefault(false)
    private val waitingForAvailableSkuDetails = BehaviorRelay.createDefault(true)

    private var skuDetails = BehaviorRelay.createDefault(listOf<SkuDetails>())
    private var currentSubscriptions = BehaviorRelay.createDefault(listOf<ProductValidity>())
    private var purchaseUpdated = PublishRelay.create<Purchase>()
    val notifier = PurchaseNotifier(context)

    override fun onBillingServiceDisconnected() {
        billingReady = false
    }

    init {
        store.get("subscriptionInfo")?.let {
            val subscriptionInfo = Gson().fromJson(it, SubscriptionInfo::class.java)
            val now = Date()
            currentSubscriptions.accept(subscriptionInfo.products.filter { it.validTo.after(now) })
        }

        notifier.addListener(object : PurchaseNotifier.Listener {
            override fun onAdded(purchase: Purchase) {
                purchaseUpdated.accept(purchase)
            }
        })
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Timber.d("Setup finished")
        billingReady = billingResult.responseCode == BillingClient.BillingResponseCode.OK
        if (billingReady) {
            updateCurrentSubscriptions()
            updateAvailableProducts()
        }
        else {
            Timber.w("Billing is not available code: ${billingResult.responseCode}, debug message: ${billingResult.debugMessage}")
            waitingForAvailableSkuDetails.accept(false)
            hasLoadedCurrentProducts.accept(true)
        }
    }

    fun updateAvailableProducts(onSuccess: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {
        val disposable = backend.products().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ response: FunctionResult<ProductResponse> ->
            Timber.d("Got products $response")
            val products = response.body?.products
            val subs = products?.filter { it.type.skuType == BillingClient.SkuType.SUBS }?.map { it.id }
            if (subs?.isNotEmpty() == true) {
                val skuDetailsParams = SkuDetailsParams.newBuilder().setSkusList(subs).setType(BillingClient.SkuType.SUBS).build()
                billingClient?.querySkuDetailsAsync(skuDetailsParams) { _, skuDetailsList ->
                    persistAvailableProducts(skuDetailsList)
                    onSuccess?.invoke()
                }
            } else {
                waitingForAvailableSkuDetails.accept(false)
                onSuccess?.invoke()
            }

        }, { error: Throwable ->
            Timber.e(error, "Failed to fetch products")
            waitingForAvailableSkuDetails.accept(false)
            onError?.invoke(error)
        })
    }

    suspend fun suspendUpdateAvailableProducts(): Unit = suspendCoroutine { continuation ->
        updateAvailableProducts(onSuccess = {
            continuation.resume(Unit)
        }, onError = {
            continuation.resumeWithException(it)
        })
    }

    private fun updateCurrentSubscriptions() {
        if (billingReady) {
            billingClient?.queryPurchasesAsync(BillingClient.SkuType.SUBS) { result, purchasesList ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchasesList?.let {
                        updateCurrentSubscriptions(it)
                    } ?: run {
                        billingClient?.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS) { _, historyPurchasesList ->
                            historyPurchasesList?.let { list  ->
                                updateCurrentSubscriptions(list.map { it.toPurchase() })
                            }
                        }
                    }
                } else {
                    // Handle the error
                    Timber.w("Failed to query purchases. Response code: ${result.responseCode}, debug message: ${result.debugMessage}")
                }
            }
        }
    }


    private fun updateCurrentSubscriptions(list: List<Purchase>) {
        if (list.isEmpty()) {
            persistAndNotifyCurrentSubscriptions(SubscriptionInfo(listOf()))
            hasLoadedCurrentProducts.accept(true)
            return
        }
        notifier.update(list)
        Timber.d("Update current subscriptions $list")

        ensureAcknowledged(list)
        // TODO after updates to billing version sku is not longer supported with 4 and above
        // for now i am just using 1st element returned, cann't test it right now must be changes in future.
        val disposable = Observable.fromIterable(list).subscribeOn(Schedulers.io()).flatMap { purchase: Purchase ->
            return@flatMap backend.subscriptionInfo(purchase.purchaseToken, purchase.skus[0]).toObservable()
        }.map {
            if (it.body == null) {
                throw Exception("No body available")
            }
            return@map it.body
        }.reduce { first: SubscriptionInfo, second: SubscriptionInfo ->
            val products = mutableListOf<ProductValidity>().also {
                it.addAll(first.products)
                it.addAll(second.products)
            }
            return@reduce SubscriptionInfo(products)
        }.toSingle().observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
            if (result != null) {
                persistAndNotifyCurrentSubscriptions(result)
                hasLoadedCurrentProducts.accept(true)
            } else {
                Timber.e("Finished with null result")
            }
        }, { error ->
            Timber.e(error, "Failed to update current subscriptions")
        })
    }

    private fun ensureAcknowledged(list: List<Purchase>) {
        list.forEach { ensureAcknowledged(it) }
    }

    private fun ensureAcknowledged(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
            billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build()) {
                if (it.responseCode != BillingClient.BillingResponseCode.OK) {
                    Timber.e("Failed to acknowledge purchase ${purchase.orderId}, message: ${it.debugMessage}")
                }
            }
        }
    }

    private fun persistAndNotifyCurrentSubscriptions(subscriptionInfo: SubscriptionInfo) {
        Timber.d("CurrentSubscriptions updated $subscriptionInfo")
        store.put("subscriptionInfo", Gson().toJson(subscriptionInfo))
        currentSubscriptions.accept(subscriptionInfo.products)
    }

    private fun persistAvailableProducts(list: List<SkuDetails>?) {
        Timber.d("SkuDetailsList updated $list")
        if (list == null) {
            return
        }
        skuDetails.accept(list)

        waitingForAvailableSkuDetails.accept(false)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        purchases?.let {
            hasLoadedCurrentProducts.accept(false)
            updateCurrentSubscriptions(it)
        }
    }

    fun startPurchase(activity: Activity, skuDetails: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
        billingClient?.launchBillingFlow(activity, flowParams)
    }

    fun start(onSuccess: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {
        billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build().also {
            if (!it.isReady) {
                waitingForAvailableSkuDetails.accept(true)
                it.startConnection(object: BillingClientStateListener{
                    override fun onBillingServiceDisconnected() {
                        this@BillingManager.onBillingServiceDisconnected()
                    }

                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        this@BillingManager.onBillingSetupFinished(billingResult)
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            onSuccess?.invoke()
                        }
                        else {
                            onError?.invoke(IllegalStateException("Failed to load billing client ${billingResult.responseCode}, debug message: ${billingResult.debugMessage}"))
                        }
                    }
                })
            }
            else {
                onSuccess?.invoke()
            }
        }
    }

    suspend fun suspendStart() = withContext(Dispatchers.Main) {
        suspendCoroutine<Unit> { continuation ->
            start(onSuccess = {
                continuation.resume(Unit)
            }, onError = {
                continuation.resumeWithException(it)
            })
        }
    }

    fun stop() {
        billingClient?.endConnection()
    }

    fun checkCurrentProducts(onSuccess: ((List<String>) -> Unit), onError: ((Throwable) -> Unit)) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Timber.d("Moving to main thread")
            Handler(Looper.getMainLooper()).post {
                checkCurrentProducts(onSuccess, onError)
            }
            return
        }

        // TODO return cached value

        BillingClient.newBuilder(context).enablePendingPurchases().setListener { _, _ -> Timber.d("So it was updated... ") }.build().also {
            if (!it.isReady) {
                it.startConnection(object: BillingClientStateListener{
                    override fun onBillingServiceDisconnected() {
                        Timber.d("Disconnected")
                    }

                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        checkCurrentProducts(it, onSuccess, onError)
                    }
                })
            }
            else {
                checkCurrentProducts(it, onSuccess, onError)
            }
        }
    }

    private fun checkCurrentProducts(billingClient: BillingClient, onSuccess: ((List<String>) -> Unit), onError: ((Throwable) -> Unit)) {

        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS) { _, purchaseHistoryRecords ->

            if (purchaseHistoryRecords == null || purchaseHistoryRecords.isEmpty()) {
                onSuccess.invoke(emptyList())
                return@queryPurchaseHistoryAsync
            }
            val purchasesList = purchaseHistoryRecords.map { it.toPurchase() }
            notifier.update(purchasesList)
            ensureAcknowledged(purchasesList)
            // TODO after updates to billing version sku is not longer supported with 4 and above
            // for now i am just using 1st element returned, cann't test it right now must be changes in future.
            val disposable = Observable.fromIterable(purchasesList).subscribeOn(Schedulers.io()).flatMap { purchase: Purchase ->
                return@flatMap backend.subscriptionInfo(purchase.purchaseToken, purchase.skus[0]).toObservable()
            }.map {
                if (it.body == null) {
                    throw Exception("No body available")
                }
                return@map it.body
            }.reduce { first: SubscriptionInfo, second: SubscriptionInfo ->
                val products = mutableListOf<ProductValidity>().also {
                    it.addAll(first.products)
                    it.addAll(second.products)
                }
                return@reduce SubscriptionInfo(products)
            }.toSingle().observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
                if (result != null) {
                    persistAndNotifyCurrentSubscriptions(result)
                    hasLoadedCurrentProducts.accept(true)
                    onSuccess.invoke(result.products.map { it.name })
                } else {
                    onSuccess.invoke(emptyList())
                    Timber.e("Finished with null result")
                }
            }, { error ->
                onError.invoke(error)
            })
        }
    }

    fun currentProducts() : Observable<List<ProductValidity>> = currentSubscriptions
    fun hasLoadedCurrentProducts(): Observable<Boolean> = hasLoadedCurrentProducts
    fun currentProductsLoaded(): Boolean = hasLoadedCurrentProducts.value == true

    fun skuDetails(): Observable<List<SkuDetails>> = skuDetails

    fun purchases(): Observable<Purchase> = purchaseUpdated

    fun waitingForAvailableSkuDetails(): Observable<Boolean> = waitingForAvailableSkuDetails
    fun isWaitingForAvailableSkuDetails(): Boolean = waitingForAvailableSkuDetails.value == true

    fun getLastPurchase(): Purchase? {
        var lastPurchase: Purchase? = null
        billingClient?.queryPurchasesAsync(BillingClient.SkuType.SUBS) { result, purchasesList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchasesList?.let {
                    ensureAcknowledged(it)
                    lastPurchase = it.firstOrNull()
                }
            } else {
                // Handle the error
                Timber.w("Failed to query purchases. Response code: ${result.responseCode}, debug message: ${result.debugMessage}")
            }
        }

        return lastPurchase
    }


}

private fun PurchaseHistoryRecord.toPurchase() = Purchase(originalJson, signature)