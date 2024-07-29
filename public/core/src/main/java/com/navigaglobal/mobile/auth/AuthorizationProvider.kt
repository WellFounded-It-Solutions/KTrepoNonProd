package com.navigaglobal.mobile.auth

/**
 * Provides authorization that can be sent to
 */
interface AuthorizationProvider {
    /**
     * Get the value for an Authorization header
     */
    fun getAuthorization(): String?
}