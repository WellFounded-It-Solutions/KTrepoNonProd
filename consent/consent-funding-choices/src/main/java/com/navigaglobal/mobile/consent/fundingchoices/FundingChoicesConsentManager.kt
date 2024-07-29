package com.navigaglobal.mobile.consent.fundingchoices

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.navigaglobal.mobile.consent.ConsentManager
import timber.log.Timber


class FundingChoicesConsentManager : ConsentManager {

    override fun presentConsentForm(activity: Activity) {
        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()
        // Set tag for under age of consent. Here false means users are not under age

        UserMessagingPlatform.getConsentInformation(activity)?.requestConsentInfoUpdate(activity, params,
                {
                    if (UserMessagingPlatform.getConsentInformation(activity).isConsentFormAvailable && UserMessagingPlatform.getConsentInformation(activity).consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                        loadForm(activity) {
                            presentConsentForm(it, activity)
                        }
                    }
                },
                {
                    it.errorCode
                    Timber.e("Failed to request consent form: ${it.message} - ${it.errorCode}")
                })
    }

    override fun resetConsent(context: Context) {
        UserMessagingPlatform.getConsentInformation(context).reset()
    }

    private fun loadForm(activity: Activity, onSuccess:  UserMessagingPlatform.OnConsentFormLoadSuccessListener ) {
        UserMessagingPlatform.loadConsentForm(activity, onSuccess,
                {
                    Timber.e("Failed to request consent form: ${it.message} - ${it.errorCode}")
                }
        )
    }

    private fun presentConsentForm(consentForm: ConsentForm, activity: Activity) {
        consentForm.show(activity) {
            Timber.d("Form dismissed")
            // Handle dismissal by reloading form.
            loadForm(activity, ) {}
        }
    }
}