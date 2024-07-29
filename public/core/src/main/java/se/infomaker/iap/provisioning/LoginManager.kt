package se.infomaker.iap.provisioning

import android.app.Activity
import io.reactivex.Observable
import io.reactivex.Single
import se.infomaker.frt.statistics.UserInfomationProvider
import se.infomaker.iap.provisioning.backend.LoginType
import se.infomaker.iap.provisioning.backend.ProductValidity

interface LoginManager : UserInfomationProvider {

    /**
     * true if the user has opted out of logging in
     */
    var userHasOptedOut: Boolean

    /**
     * true if the user is logged in and the account is linked to a subscription
     */
    var isLinked: Boolean

    /**
     * Start monitoring user login status and available subscriptions
     */
    fun start()

    /**
     * Stop monitoring user login status and available subscriptions
     */
    fun stop()

    /**
     * Login to account
     * @param authCode email to use when logging in
     * @param onSuccess run on successful login
     * @param onError run on login failure
     */
    fun login(authCode: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    /**
     * Login to account
     * @param email email to use when logging in
     * @param password password to use when logging in
     * @param onSuccess run on successful login
     * @param onError run on login failure
     */
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    /**
     * Logout the user
     */
    fun logout(activity: Activity, onDone: (() -> Unit)?)

    /**
     * Used to complete a logout that has been delayed by a remote login request
     */
    fun completeLogout()

    /**
     * Update current subscriptions info with current user
     * @param onSuccess called when update complete successfully
     * @param onError called when update fails
     */
    fun updateSubscriptionInfo(onSuccess: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null)

    /**
     * Observe current products available
     */
    fun currentProducts(): Observable<List<ProductValidity>>

    /**
     * Make an online check of current products
     */
    fun checkCurrentProducts(onSuccess: ((List<ProductValidity>) -> Unit), onError: ((Throwable) -> Unit))

    /**
     *  Create account with provided params
     *  @param email to create account with
     *  @param password to create account with
     *  @param onAccountCreated called when account has been created successfully
     *  @param onError called if an error occurs when trying to create account
     */
    fun createAccount(email: String, password: String, onAccountCreated: (String) -> Unit, onError: (Throwable) -> Unit)

    fun loginType(): Observable<LoginType>

    /**
     * Observe login status
     */
    fun loginStatus(): Observable<LoginStatus>

    /**
     * Simple getter for current login status
     */
    fun getLoginStatus(): LoginStatus

    /**
     * Observe if the manager is in fetching status
     */
    fun fetchingProducts(): Observable<Boolean>

    /**
     * Simple getter to get fetching status
     */
    fun isFetchingProducts(): Boolean

    /**
     * Display login from activity
     */
    fun showLogin(activity: Activity)

    /**
     * Returns a va lid auth token
     */
    fun getAuthToken(): Single<String>


    fun userInfo(): Single<UserInfo>


    fun getUserInfo(): UserInfo?
}