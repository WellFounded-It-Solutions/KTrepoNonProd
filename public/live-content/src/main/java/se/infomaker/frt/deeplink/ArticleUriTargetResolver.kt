package se.infomaker.frt.deeplink

import android.net.Uri
import se.infomaker.frt.moduleinterface.deeplink.UriTarget
import se.infomaker.frt.moduleinterface.deeplink.UriTargetResolver
import se.infomaker.frt.ui.fragment.TabbedModuleConfig
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.livecontentui.sharing.SharingService
import timber.log.Timber
import javax.inject.Inject

class ArticleUriTargetResolver @Inject constructor(
    private val configManager: ConfigManager,
    private val sharingService: SharingService
) : UriTargetResolver {

    companion object {
        val supportedModules = setOf("ContentList", "SectionContentList", "TabbedModule")
    }

    override fun resolve(uri: Uri): UriTarget? {

        configManager.mainMenuConfig?.let {
            var moduleId: String? = null
            var targetModuleId: String? = null
            for (mainMenuItem in it.mainMenuItems) {
                if (supportedModules.contains(mainMenuItem.moduleName) && mainMenuItem.isDefaultSelected || moduleId == null) {
                    if (mainMenuItem.moduleName == "TabbedModule") {
                        val tabsConfig = configManager.getConfig(mainMenuItem.id, TabbedModuleConfig::class.java)
                        tabsConfig?.tabs?.firstOrNull()?.id?.let { firstTabModuleId ->
                            targetModuleId = firstTabModuleId
                        }
                    }
                    moduleId = mainMenuItem.id
                }
            }
            moduleId?.let { id ->
                val config = ConfigManager.getInstance().getConfig(moduleId, ArticleUuidProviderConfig::class.java)
                val shareUrl = config.articleUuidProviderUrl
                if (shareUrl != null) {
                    try {
                        val response = sharingService.getUuid(shareUrl, uri.toString()).blockingFirst()
                        if (response.uuid.isNotEmpty()) {
                            val values = HashMap<String, String>()
                            values["uuid"] = response.uuid
                            values["context"] = targetModuleId ?: id
                            values["message"] = "foo"
                            return UriTarget(id, values)
                        }
                    }
                    catch (e: Exception) {
                        Timber.e(e, "Failed to resolve response")
                    }
                }
            }
        }
        return null
    }
}