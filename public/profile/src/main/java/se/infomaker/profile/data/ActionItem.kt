package se.infomaker.profile.data

import org.json.JSONObject
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class ActionItem private constructor (
    config: ActionItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), ProfileItem {

    override var text: String? = null
    var action: String? = null
    var actionParameters: JSONObject? = null

    override val name: String
        get() = "action"

    override val image: Int
        get() {
            return JSONUtil.optString(actionParameters, "image").takeIf { it != "" }
                ?.let {
                    theme.getImage(null, it)?.resourceId ?: resources.getDrawableIdentifier(it)
                }
                ?: -1
        }

    init {
        buildAction((config as? ActionItemConfig)?.parameters)
        super.configure(config)
    }

    private fun buildAction(actionConfig: JSONObject?) {
        actionParameters = actionConfig?.let { config ->
            JSONUtil.optString(config, "action")?.also { action = it }
            JSONUtil.optString(config, "text")?.also { text = it }
            config
        }
    }

    companion object {
        operator fun invoke(
            config: ActionItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): ActionItem? {
            return config.parameters?.let {
                JSONUtil.optString(it, "action") ?: return null
                JSONUtil.optString(it, "text") ?: return null
                ActionItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            } ?: return null
        }
    }
}