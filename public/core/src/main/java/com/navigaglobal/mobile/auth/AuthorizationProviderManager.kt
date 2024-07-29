package com.navigaglobal.mobile.auth


/**
 * Allows other to register auth providers used when communicating with services like open content
 */
object AuthorizationProviderManager {
    private val providers = mutableMapOf<String, AuthorizationProvider>()

    /**
     * Set an auth provider to use with a specific service
     */
    fun set(url: String, authProvider: AuthorizationProvider?) {
        if (authProvider == null) {
            providers.remove(url)
        }
        else {
            providers[url] = authProvider
        }
    }

    /**
     * Get auth provider for a url
     */
    fun get(url: String): AuthorizationProvider? {
        return providers[url]
    }
}