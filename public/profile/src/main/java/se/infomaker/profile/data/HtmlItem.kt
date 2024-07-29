package se.infomaker.profile.data

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class HtmlItem private constructor(
    val config: HtmlItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), ProfileItem {

    override val name: String
        get() = "html"

    override val text: String
        get() = config.parameters!!.text!!

    init {
        super.configure(config)
    }

    companion object {
        operator fun invoke(
            config: HtmlItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): HtmlItem? {
            if (config.parameters == null || config.parameters.text.isNullOrEmpty()) {
                return null
            }
            return HtmlItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
        }
    }
}