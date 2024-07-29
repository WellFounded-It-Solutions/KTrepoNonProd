package se.infomaker.iap.provisioning.firebase

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.jakewharton.rxrelay2.BehaviorRelay
import com.navigaglobal.mobile.R
import com.netcore.android.Smartech
import io.hansel.hanselsdk.Hansel
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.iap.provisioning.LoginException
import se.infomaker.iap.provisioning.LoginManager
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.LoginTypeProvider
import se.infomaker.iap.provisioning.UserInfo
import se.infomaker.iap.provisioning.backend.Backend
import se.infomaker.iap.provisioning.backend.CreateAccountResponse
import se.infomaker.iap.provisioning.backend.FunctionResult
import se.infomaker.iap.provisioning.backend.LoginResponse
import se.infomaker.iap.provisioning.backend.LoginType
import se.infomaker.iap.provisioning.backend.ProductValidity
import se.infomaker.iap.provisioning.store.KeyValueStore
import se.infomaker.iap.provisioning.ui.LoginActivity
import se.infomaker.iap.provisioning.ui.openCustomTab
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.Date
import java.util.concurrent.TimeUnit


class FirebaseLoginManager(
    val customScheme: String?,
    val store: KeyValueStore,
    val backend: Backend,
    @Deprecated("No longer allowed to force LoginType, this will be ignored.") val forceLoginType: LoginType? = null,
    private val mainScheduler: Scheduler = AndroidSchedulers.mainThread(),
    val trackUserId: Boolean?,
    private val useFirebaseEmulators: Boolean
) : LoginManager, LoginTypeProvider {

    private val garbage = CompositeDisposable()
    private val loginStatus = BehaviorRelay.createDefault(LoginStatus.LOGGED_OUT)
    private var lastLoginType: LoginType = LoginType.UNAVAILABLE
    private val auth: FirebaseAuth = FirebaseAuth.getInstance().also {
        if (useFirebaseEmulators) {
            it.useEmulator("10.0.2.2", 9099)
        }
    }
    private val fetchingProducts = BehaviorRelay.createDefault(false)
    private val currentProducts = BehaviorRelay.createDefault(store.getValidProducts() ?: emptyList())

    private val redirectUri: String
        get() = "${customScheme}oauth2redirect"

    private var loginThrottle = 0L
    private var postponedLogoutComplete : (() -> Unit)? = null

    override var userHasOptedOut = false
        set(value) {
            field = value
            save()
        }

    override var isLinked = false
        set(value) {
            field = value
            save()
        }

    init {
        updateGlobalStats()
        load()
    }

    private fun updateGlobalStats() {
        val allowTracking = trackUserId ?: false
        val user = auth.currentUser
        if (user == null) {
            StatisticsManager.getInstance().removeGlobalAttribute("userID")
            StatisticsManager.getInstance().addGlobalAttribute("loggedIn", false)
        } else {
            if (!allowTracking) {
                // Remove the userID when user tracking is disable in config
                StatisticsManager.getInstance().removeGlobalAttribute("userID")
            } else {
                StatisticsManager.getInstance().addGlobalAttribute("userID", user.uid)
            }
            StatisticsManager.getInstance().addGlobalAttribute("loggedIn", true)
        }
    }

    private fun load() {
        store.apply {
            userHasOptedOut = getBoolean("userHasOptedOut", false)
            isLinked = getBoolean("isLinked", false)
            if (auth.currentUser != null) {
                loginStatus.accept(LoginStatus.LOGGED_IN)
            }
        }

        // Skipping first currentProducts value, since it is a BehaviorRelay and first value will always be what we create as default
        garbage.add(currentProducts.skip(1).subscribe {
            store.putProducts(it)
        })
    }

    override fun getUserId(): String? {
        return getUserInfo()?.userId
    }

    override fun isUserLoggedIn(): Boolean {
        return getLoginStatus() == LoginStatus.LOGGED_IN
    }

    override fun getUserInfo(): UserInfo? {
        auth.currentUser?.let {
            val userId = it.uid.split("^")[0]
            return UserInfo(userId, it.displayName, it.email)
        }
        return null
    }

    override fun userInfo(): Single<UserInfo> {

        // New Comment //

        val muserId = auth.currentUser?.uid!!.replace("google-oauth2|", "").split("^")[0]

        Hansel.getUser().setUserId(muserId)
        Smartech.getInstance(WeakReference(null)).login(muserId)

        Timber.d("HanselUserId: $muserId")

        // New Comment //

        auth.currentUser?.let {
            val userId = it.uid.split("^")[0]
            return Single.just(UserInfo(userId, it.displayName, it.email))
        }
        return Single.error(Exception("Failed to get current user"))
    }

    override fun getAuthToken(): Single<String> {
        return Single.create<String> { emitter ->
            val task = auth.currentUser?.getIdToken(false)
            task?.addOnCompleteListener{
                if (it.isSuccessful) {
                    it.result?.token?.let { token ->
                        emitter.onSuccess(token)
                        return@addOnCompleteListener
                    }
                }
                it.exception?.let { exception ->
                    emitter.onError(exception)
                    return@addOnCompleteListener
                }
                emitter.onError(java.lang.Exception("Unknown error for result ${it.result}"))
            }
        }
    }

    override fun showLogin(activity: Activity) {
        if (System.currentTimeMillis() - 500 < loginThrottle ) {
            return
        }
        loginThrottle = System.currentTimeMillis()
        val disposable = loginType().singleOrError().subscribeOn(Schedulers.io()).observeOn(mainScheduler)
            .flatMap { loginWithType(activity, it) }
            .subscribe({ it.invoke() }, {
                presentLoginTypeErrorDialog(activity)
                Timber.e(it, "Failed to show login")
            })
    }

    private fun presentLoginTypeErrorDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.login_type_error)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }

    private fun loginWithType(activity: Activity, type: LoginType?) : Single<() -> Unit>{
        when(type) {
            LoginType.URL -> {

                return backend.loginUrl(redirectUri).map { response ->
                    val url = response.body?.url
                    if (url != null) {
                        return@map {
                            StepStoneActivity.open(activity, url)
                        }
                    }
                    return@map {
                        Toast.makeText(activity, "Backend did not provide any login url", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            LoginType.PASSWORD -> {
                return Single.just { activity.startActivity(Intent(activity, LoginActivity::class.java)) }
            }
            LoginType.TEMPORARILY_DISABLED -> {
                return Single.just { updateSubscriptionInfo() }
            }
            else -> {
                return Single.just { Timber.e("Failed to start login with type $type") }
            }
        }
    }

    override fun start() {
        if (auth.currentUser != null) {
            loginStatus.accept(LoginStatus.LOGGED_IN)
        }
        updateSubscriptionInfo()
    }

    override fun stop() {

    }

    private fun save() {
        store.beginTransaction()
            .putBoolean("userHasOptedOut", userHasOptedOut)
            .putBoolean("isLinked", isLinked)
            .endTransaction()
    }

    /**
     * Login to the backend using auth code
     */
    override fun login(authCode: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        login(backend.loginAuthCode(authCode, redirectUri), onSuccess, onError)
    }

    /**
     * Login to the backend using email and password
     */
    override fun login(email: String, password: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        login(backend.login(email, password), onSuccess, onError)
    }

    /**
     * Login using a function result containing a login response
     */
    private fun login(result: Single<FunctionResult<LoginResponse>>, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        loginStatus.accept(LoginStatus.IN_PROGRESS)
        val disposable = result.subscribeOn(Schedulers.io()).observeOn(mainScheduler)
            .subscribe { response: FunctionResult<LoginResponse>?, error: Throwable? ->
                if (response != null) {
                    val token = response.body?.token
                    if (token != null) {
                        auth.signInWithCustomToken(token).addOnCompleteListener {

                            updateSubscriptionInfo(onSuccess = {

                                onSuccess.invoke()
                                loginStatus.accept(LoginStatus.LOGGED_IN)
                            }, onError = {
                                loginStatus.accept(LoginStatus.LOGGED_IN)
                                onError.invoke(it)
                            })
                        }
                    } else {
                        onError.invoke(error ?: LoginException("Unknown error"))
                        loginStatus.accept(LoginStatus.LOGGED_OUT)
                    }

                } else {
                    onError.invoke(LoginException(error?.message))
                    loginStatus.accept(LoginStatus.LOGGED_OUT)
                }
            }
    }

    override fun logout(activity: Activity, onDone: (() -> Unit)?) {
        loginStatus.accept(LoginStatus.IN_PROGRESS)
        val redirectUri = "${customScheme}noop"
        val disposable = backend.logoutUrl(redirectUri).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { response, _ ->
                postponedLogoutComplete = onDone
                val url = response?.body?.url
                if (url != null) {
                    activity.openCustomTab(Uri.parse(url))
                }
                else {
                    completeLogout()
                }
            }
    }

    override fun completeLogout() {
        auth.signOut()
        isLinked = false
        currentProducts.accept(listOf())
        store.clear()
        loginStatus.accept(LoginStatus.LOGGED_OUT)
        postponedLogoutComplete?.invoke()
        postponedLogoutComplete = null
        updateGlobalStats()

        // New Comment //

        Hansel.getUser().clear()

        // New Comment //

        Smartech.getInstance(WeakReference(null)).logoutAndClearUserIdentity(true)
    }

    @SuppressLint("CheckResult")
    override fun updateSubscriptionInfo(onSuccess: (() -> Unit)?, onError: ((Throwable) -> Unit)?) {

        val updateSubscriptionInfo = {
            fetchingProducts.accept(true)
            val disposable = backend.subscriptionInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { response, error ->
                    if ((error as? FirebaseFunctionsException)?.code == FirebaseFunctionsException.Code.UNAUTHENTICATED) {
                        completeLogout()
                    }
                    fetchingProducts.accept(false)
                    val products = response?.body?.products

                    when {
                        products != null -> {
                            currentProducts.accept(products)
                            onSuccess?.invoke()
                        }
                        (error as? FirebaseFunctionsException)?.code == FirebaseFunctionsException.Code.PERMISSION_DENIED -> {
                            currentProducts.accept(emptyList())
                            onSuccess?.invoke()
                        }
                        else -> {
                            store.getProducts()?.let { storedProducts ->
                                if (storedProducts.isNotEmpty()) {
                                    val revivedProducts = storedProducts.reviveExpired()
                                    currentProducts.accept(revivedProducts)
                                    onSuccess?.invoke()
                                    return@subscribe
                                }
                            }
                            onError?.invoke(error ?: Exception("Unknown error"))
                        }
                    }
                }
        }

        updateGlobalStats()
        if (auth.currentUser == null) {
            loginType()
                .subscribeOn(Schedulers.io())
                .observeOn(mainScheduler)
                .onErrorReturnItem(LoginType.UNAVAILABLE)
                .subscribe {
                    when (it) {
                        LoginType.TEMPORARILY_DISABLED -> {
                            updateSubscriptionInfo.invoke()
                        }
                        LoginType.UNAVAILABLE -> {
                            store.getProducts()?.let { storedProducts ->
                                if (storedProducts.isNotEmpty()) {
                                    val revivedProducts = storedProducts.reviveExpired(TimeUnit.MINUTES.toMillis(5))
                                    currentProducts.accept(revivedProducts)
                                    onSuccess?.invoke()
                                    return@subscribe
                                }
                            }
                            onError?.invoke(LoginException("User is not logged in"))
                        }
                        else -> {
                            onError?.invoke(LoginException("User is not logged in"))
                        }
                    }
                }
        }
        else {
            updateSubscriptionInfo.invoke()
        }
    }

    @SuppressLint("CheckResult")
    override fun checkCurrentProducts(onSuccess: (List<ProductValidity>) -> Unit, onError: (Throwable) -> Unit) {

        val checkCurrentProducts = {
            fetchingProducts.accept(true)
            backend.subscriptionInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { response, error ->
                    fetchingProducts.accept(false)
                    val products = response?.body?.products
                    if (products != null) {
                        val now = Date()
                        val filteredProducts = products.filter { it.validTo.after(now) }
                        currentProducts.accept(filteredProducts)
                        onSuccess.invoke(filteredProducts)
                    } else {
                        store.getProducts()?.let { storedProducts ->
                            if (storedProducts.isNotEmpty()) {
                                val revivedProducts = storedProducts.reviveExpired()
                                currentProducts.accept(revivedProducts)
                                onSuccess.invoke(revivedProducts)
                                return@subscribe
                            }
                        }
                        onError.invoke(error ?: Exception("Unknown error"))
                    }
                }
        }

        if (auth.currentUser == null) {
            loginType()
                .subscribeOn(Schedulers.io())
                .observeOn(mainScheduler)
                .onErrorReturnItem(LoginType.UNAVAILABLE)
                .subscribe {
                    when (it) {
                        LoginType.TEMPORARILY_DISABLED -> {
                            checkCurrentProducts.invoke()
                        }
                        LoginType.UNAVAILABLE -> {
                            store.getProducts()?.let { storedProducts ->
                                if (storedProducts.isNotEmpty()) {
                                    val revivedProducts = storedProducts.reviveExpired(TimeUnit.MINUTES.toMillis(5))
                                    currentProducts.accept(revivedProducts)
                                    onSuccess.invoke(revivedProducts)
                                    return@subscribe
                                }
                            }
                            onSuccess.invoke(emptyList())
                        }
                        else -> {
                            onSuccess.invoke(emptyList())
                        }
                    }
                }
        }
        else {
            checkCurrentProducts.invoke()
        }
    }

    override fun createAccount(email: String, password: String, onAccountCreated: (String) -> Unit, onError: (Throwable) -> Unit) {
        val disposable = backend.createAccount(email, password).subscribeOn(Schedulers.io()).observeOn(mainScheduler)
            .subscribe { response: FunctionResult<CreateAccountResponse>?, error: Throwable? ->
                val id = response?.body?.id
                if (id != null) {
                    id.let(onAccountCreated)
                } else {
                    onError.invoke(error ?: Exception("Unknown error"))
                }
            }
    }

    override fun loginType(): Observable<LoginType> {
        return backend.loginType()
            .subscribeOn(Schedulers.io())
            .observeOn(mainScheduler)
            .doOnSuccess { result ->
                result.body?.loginType?.let {
                    lastLoginType = it
                }
            }
            .map { result ->
                return@map result.body?.loginType ?: LoginType.NONE
            }.toObservable()
    }

    override fun currentProducts(): Observable<List<ProductValidity>> = currentProducts
    override fun loginStatus(): Observable<LoginStatus> = loginStatus.distinctUntilChanged().debounce(1, TimeUnit.SECONDS)
    override fun getLoginStatus(): LoginStatus = loginStatus.value ?: LoginStatus.LOGGED_OUT
    override fun getLoginType() = lastLoginType
    override fun fetchingProducts(): Observable<Boolean> = fetchingProducts
    override fun isFetchingProducts(): Boolean = fetchingProducts.value == true
}

private fun KeyValueStore.putProducts(products: List<ProductValidity>?) {
    products?.let {
        put("validProducts", productGson.toJson(it))
    }
}

private fun KeyValueStore.getProducts(): List<ProductValidity>? {
    get("validProducts")?.let {
        try {
            productGson.fromJson<List<ProductValidity>>(it, object: TypeToken<List<ProductValidity>>() {}.type)?.let { products ->
                return products
            }
        }
        catch (e: JsonSyntaxException) {
            Timber.e(e, "Could not read products, removing them.")
            putProducts(emptyList())
        }
    }
    return null
}

private fun KeyValueStore.getValidProducts(): List<ProductValidity>? {
    return getProducts()?.filter { it.validTo.after(Date()) }
}

private fun Collection<ProductValidity>.reviveExpired(extension: Long = TimeUnit.HOURS.toMillis(1)): List<ProductValidity> {
    val now = Date()
    return map { validity ->
        if (validity.validTo.before(now)) {
            validity.extend(extension)
        }
        else {
            validity
        }
    }
}

private val productGson by lazy { GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create() }