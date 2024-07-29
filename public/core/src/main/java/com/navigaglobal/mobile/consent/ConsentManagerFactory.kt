package com.navigaglobal.mobile.consent

import android.content.Context
import org.json.JSONObject

interface ConsentManagerFactory {
    /**
     * Name of the consent manager the factory can provide
     */
    fun provides() : String

    /**
     * Create a consent manager with provided configuration
     */
    fun createConsentManager(context: Context, configuration: JSONObject?): ConsentManager
}
