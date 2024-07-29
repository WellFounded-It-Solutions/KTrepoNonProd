package se.infomaker.iap.provisioning

import android.content.Context
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.iap.provisioning.config.CoreProvisioningConfig
import se.infomaker.iap.provisioning.config.ProvisioningProviderConfig
import se.infomaker.iap.provisioning.dummy.DummyProvisioningManager
import se.infomaker.iap.provisioning.firebase.FirebaseProvisioningManager

object ProvisioningManagerProvider {

    private var manager: ProvisioningManager? = null
    private val providerFactories = mutableMapOf<String, (Context, ProvisioningProviderConfig) -> ProvisioningManager>()

    init {
        register(FirebaseProvisioningManager.name) { context, config -> FirebaseProvisioningManager(context, config) }
        register(NoProvisioningProvisioningManager.NAME) { _, _ -> NoProvisioningProvisioningManager() }
    }

    /**
     * Register external provisioning manager factories
     */
    fun register(name: String, creator: (Context, ProvisioningProviderConfig) -> ProvisioningManager) {
        providerFactories[name.toLowerCase()] = creator
    }

    fun provide(context: Context): ProvisioningManager {
        manager?.let {
            return it
        }

        val config = ConfigManager.getInstance(context).getConfig("core", CoreProvisioningConfig::class.java)
        config.provisioningProvider?.let { provider ->
            provider.provider?.let { type ->
                manager = providerFactories[type.toLowerCase()]?.invoke(context, config.provisioningProvider)
            }
        }
        return manager ?: DummyProvisioningManager(context)
    }
}