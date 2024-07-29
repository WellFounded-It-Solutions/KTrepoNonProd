package com.navigaglobal.mobile.consent

import android.app.Activity
import android.content.Context

interface ConsentManager {
    /**
     * Present the consent form to the user if needed
     * If there is no need the user will not be presented with a consent form
     */
    fun presentConsentForm(activity: Activity)

    /**
     * Reset user consent
     */
    fun resetConsent(context: Context)
}