package se.infomaker.iap.provisioning.backend

import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import io.reactivex.Single
import se.infomaker.iap.provisioning.firebase.auth.AccessToken
import java.util.concurrent.TimeUnit


class Backend(
    private val id: String,
    region: String,
    useLocalEmulators: Boolean
) {
    private val gson by lazy { Gson() }

    private val firebaseFunctions = FirebaseFunctions.getInstance(region).also {
        if (useLocalEmulators) {
            it.useEmulator("10.0.2.2", 5001)
        }
    }

    private val loginTypeReplay = BackendRequestTimedReplay<FunctionResult<LoginTypeResponse>, Nothing>(DEFAULT_REPLAY_TTL) {
        Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("loginType")
                .call(mapOf("appId" to id))
                .addOnFailureListener {
                    emitter.onError(it)
                }
                .addOnSuccessListener {
                    val response = gson.fromJson(gson.toJson(it.data), LoginTypeResponse::class.java)
                    emitter.onSuccess(FunctionResult(response, it))
                }
        }
    }

    private val subscriptionInfoReplay = BackendRequestTimedReplay<FunctionResult<SubscriptionInfo>, Nothing>(DEFAULT_REPLAY_TTL) {
        Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("subscriptionInfo")
                    .call(mapOf("appId" to id))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val response = gson.fromJson(gson.toJson(it.data), SubscriptionInfo::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }

    private val getAccessTokenReplay = BackendRequestTimedReplay<FunctionResult<AccessToken>, Nothing>(DEFAULT_REPLAY_TTL) {
        Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("getAccessToken")
                    .call(mapOf("appId" to id))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val response = gson.fromJson(gson.toJson(it.data), AccessToken::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }

    /**
     * Available products to buy using in app purchase
     */
    fun products(platform: String = "android"): Single<FunctionResult<ProductResponse>> {
        return Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("products")
                    .call(mapOf("appId" to id, "platform" to platform))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val json = gson.toJson(it.data)
                        val response: ProductResponse = gson.fromJson(json, ProductResponse::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }



    /**
     * Create account in third party account provider
     */
    fun createAccount(email: String, password: String): Single<FunctionResult<CreateAccountResponse>> {
        return Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("createAccount")
                    .call(mapOf("appId" to id, "email" to email, "password" to password))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val response = gson.fromJson(gson.toJson(it.data), CreateAccountResponse::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }

    fun loginType(): Single<FunctionResult<LoginTypeResponse>> {
        return loginTypeReplay.get()
    }

    fun loginUrl(redirectUri: String): Single<FunctionResult<UrlResponse>> {
        return Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("loginUrl").call(mapOf("appId" to id, "redirectUri" to redirectUri))
                .addOnFailureListener {
                    emitter.onError(it)
                }
                .addOnSuccessListener {
                    val response = gson.fromJson(gson.toJson(it.data), UrlResponse::class.java)
                    emitter.onSuccess(FunctionResult(response, it))
                }
        }
    }

    fun logoutUrl(redirectUri: String): Single<FunctionResult<UrlResponse>> {
        return Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("logoutUrl").call(mapOf("appId" to id, "returnUrl" to redirectUri, "errorUrl" to redirectUri))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val response = gson.fromJson(gson.toJson(it.data), UrlResponse::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }

    fun loginAuthCode(authCode: String, redirectUri: String): Single<FunctionResult<LoginResponse>> {
        return Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("loginAuthCode")
                    .call(mapOf("appId" to id, "authCode" to authCode, "redirectUri" to redirectUri))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val response = gson.fromJson(gson.toJson(it.data), LoginResponse::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }

    /**
     * Login to third party account provider
     */
    fun login(email: String, password: String): Single<FunctionResult<LoginResponse>> {
        return Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("login")
                    .call(mapOf("appId" to id, "email" to email, "password" to password))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val response = gson.fromJson(gson.toJson(it.data), LoginResponse::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }

    /**
     * Link third party account to in app purchase
     */
    fun linkAccount(purchaseToken: String, subscriptionId: String): Single<FunctionResult<LinkAccountResponse>> {
        return Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("linkAccount")
                    .call(mapOf("appId" to id, "purchaseToken" to purchaseToken, "subscriptionId" to subscriptionId))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val response = gson.fromJson(gson.toJson(it.data), LinkAccountResponse::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }


    fun subscriptionInfo(purchaseToken: String, subscriptionId: String): Single<FunctionResult<SubscriptionInfo>> {
        return Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("subscriptionInfo")
                    .call(mapOf("appId" to id, "purchaseToken" to purchaseToken, "subscriptionId" to subscriptionId))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val response = gson.fromJson(gson.toJson(it.data), SubscriptionInfo::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }

    fun subscriptionInfo(): Single<FunctionResult<SubscriptionInfo>> {
        return subscriptionInfoReplay.get()
    }

    fun getAccessToken() :Single<FunctionResult<AccessToken>> {
        return getAccessTokenReplay.get()
    }

    fun getAccessToken(purchaseToken: String?, subscriptionId: String): Single<FunctionResult<AccessToken>> {
        return Single.create { emitter ->
            firebaseFunctions.getHttpsCallable("getAccessToken")
                    .call(mapOf("appId" to id, "purchaseToken" to purchaseToken, "subscriptionId" to subscriptionId))
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener {
                        val response = gson.fromJson(gson.toJson(it.data), AccessToken::class.java)
                        emitter.onSuccess(FunctionResult(response, it))
                    }
        }
    }

    companion object {
        private val DEFAULT_REPLAY_TTL = TimeUnit.SECONDS.toMillis(5)
    }
}