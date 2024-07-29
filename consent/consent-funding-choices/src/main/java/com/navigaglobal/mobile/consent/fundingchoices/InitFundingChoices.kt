package com.navigaglobal.mobile.consent.fundingchoices

import android.content.Context
import com.navigaglobal.mobile.consent.ConsentManagerProvider
import se.infomaker.frtutilities.AbstractInitContentProvider

class InitFundingChoices  : AbstractInitContentProvider() {
    override fun init(context: Context) {
        ConsentManagerProvider.registerFactory(FundingChoicesConsentManagerFactory())
    }
}
