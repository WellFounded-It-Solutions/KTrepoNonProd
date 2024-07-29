package se.infomaker.frt.statistics

/**
 * Provide user information needed by various statistics
 * services.
 */
interface UserInfomationProvider {
    /**
     * @return true if the user is logged in
     */
    fun isUserLoggedIn(): Boolean

    /**
     * @return user id or null if not logged in
     */
    fun getUserId(): String?
}