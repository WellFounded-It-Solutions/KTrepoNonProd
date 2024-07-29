package se.infomaker.library

import android.content.Context
import android.view.View
import org.json.JSONObject
import se.infomaker.frtutilities.ConfigManager
import com.navigaglobal.mobile.consent.ConsentManagerProvider
import com.navigaglobal.mobile.consent.UserConsentProvider

import timber.log.Timber

/**
 * Common contract for a client to retrieve an ad no matter the implementing provider.
 */
interface AdProvider {
    fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject): View?
}

/**
 * Ad providers that can take advantage of parameters should implement this interface instead
 */
interface ContentAwareAdProvider {
    fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject, content: List<JSONObject>?, env: JSONObject?): View?
}

/**
 * Ad providers that offer ability to listen to ad loading status should implement this interface
 * instead.
 */
interface ListenableAdProvider : AdProvider {
    fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject, listener: OnAdFailedListener?): View?
}

/**
 * Ad providers that offer the ability to _BOTH_ take advantage of parameters _AND_ listen for ad
 * load status should implement this interface instead.
 */
interface AdProvider2 {
    fun getView(context: Context, coreProviderConfig: JSONObject?, config: JSONObject, content: List<JSONObject>?, env: JSONObject?, listener: OnAdFailedListener?): View?
}

object AdViewFactory {
    private val providers: MutableMap<String, AdProvider> = mutableMapOf()

    private var coreConfigs: MutableMap<String, JSONObject?>? = null
    private var userConsentProvider: UserConsentProvider? = null

    init {
        Timber.d("Initializing AdViewFactory")
    }

    fun registerAdProvider(provider: String, viewProvider: AdProvider) {
        providers[provider] = viewProvider
    }

    @Deprecated(message = "Use method accepting parameters", replaceWith = ReplaceWith("getView(provider: String, context: Context, config: JSONObject, parameters: Map<String, JSONObject>): View?"))
    fun getView(provider: String, context: Context, config: JSONObject): View? {
        return getView(provider, context, config, emptyList(), JSONObject())
    }

    fun getView(provider: String, context: Context, config: JSONObject, content: List<JSONObject>?, env: JSONObject?): View? {
        return getView(provider,context, config, content, env, null)
    }

    fun getView(provider: String, context: Context, config: JSONObject, content: List<JSONObject>?, env: JSONObject?, listener: OnAdFailedListener?): View? {
        val coreProviderConfig = providerConfig(context, provider)
        if (coreProviderConfig?.optBoolean("requireConsent") == true) {
            if (userConsentProvider == null) {
                userConsentProvider = ConsentManagerProvider.getUserConsentProvider(context)
            }
            userConsentProvider?.hasUserConsent().let {
                if (it != true) {
                    // Do not serve ads when the user has not given consent and it is set to be required
                    return  null
                }
            }
        }
        providers[provider]?.let { provider ->
            return when(provider) {



                is AdProvider2 -> {
                    provider.getView(context, coreProviderConfig, config, content, env, listener)
                }
                is ContentAwareAdProvider -> {
                    provider.getView(context, coreProviderConfig, config, content, env)
                }
                is ListenableAdProvider -> {
                    provider.getView(context, coreProviderConfig, config, listener)
                }
                else -> {
                    provider.getView(context, coreProviderConfig, config)
                }
            }
        }
        return null
    }

    private fun providerConfig(context: Context, provider: String): JSONObject? {
        if (coreConfigs == null) {
            synchronized(this) {
                if (coreConfigs == null) {
                    loadCoreConfig(context)
                }
            }
        }
        return coreConfigs?.get(provider)
    }

    private fun loadCoreConfig(context: Context) {
        coreConfigs = mutableMapOf<String, JSONObject?>().also {
            val config = ConfigManager.getInstance(context).getConfig("core", AdCoreConfig::class.java)
            config?.adProviders?.forEach { provider ->
                it[provider.provider] = JSONObject(provider.config.toString())
            }
        }
    }
}