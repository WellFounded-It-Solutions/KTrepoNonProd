package com.navigaglobal.mobile.consent

import android.content.Context
import org.json.JSONObject
import se.infomaker.frtutilities.ConfigManager

object ConsentManagerProvider {
    private var factories: MutableMap<String, ConsentManagerFactory> = mutableMapOf<String, ConsentManagerFactory>()

    private var consentManager : ConsentManager? = null
    private var notRequired = false
    private var userConsentProvider: UserConsentProvider? = null

    fun registerFactory(factory: ConsentManagerFactory) {
        factories[factory.provides()] = factory
    }

    fun provide(context: Context) : ConsentManager? {
        if (notRequired) {
            return  null
        }

        consentManager?.let {
            return it
        }
        val config = ConfigManager.getInstance(context).getConfig("core", ConsentCoreConfig::class.java)
        if (config.consentProvider == null) {
            notRequired = true
            return null
        }
        factories[config.consentProvider.provider]?.let { factory ->
            val configuration = config.consentProvider.config?.let {
                JSONObject(it.toString())
            }
            consentManager = factory.createConsentManager(context, configuration)
        }

        return consentManager
    }

    fun getUserConsentProvider(context: Context) : UserConsentProvider {
        userConsentProvider?.let {
            return it
        }
        return UserConsentProvider(context).also {
            userConsentProvider = it
        }
    }
}