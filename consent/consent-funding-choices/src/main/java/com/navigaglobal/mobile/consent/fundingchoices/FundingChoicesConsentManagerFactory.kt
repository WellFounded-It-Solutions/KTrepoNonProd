package com.navigaglobal.mobile.consent.fundingchoices

import android.content.Context
import com.navigaglobal.mobile.consent.ConsentManager
import com.navigaglobal.mobile.consent.ConsentManagerFactory
import org.json.JSONObject

class FundingChoicesConsentManagerFactory : ConsentManagerFactory {
    override fun createConsentManager(context: Context, configuration: JSONObject?): ConsentManager {
        return FundingChoicesConsentManager()
    }

    override fun provides(): String {
        return "FundingChoices"
    }
}