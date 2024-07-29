package se.infomaker.profile.data

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class LinkItem private constructor(
    val config: LinkItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), ProfileItem {

    override val text: String
        get() = config.parameters?.text.orEmpty()

    val url: String
        get() = config.parameters?.url.orEmpty()

    override val name: String
        get() = "link"

    override val image: Int
        get() = config.parameters?.image?.let {
            theme.getImage(null, it)?.resourceId ?: resources.getDrawableIdentifier(it)
        } ?: -1

    init {
        super.configure(config)
    }

    companion object {
        operator fun invoke(
            config: LinkItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): LinkItem? {
            if (config.parameters == null || config.parameters.text.isNullOrEmpty() || config.parameters.url.isNullOrEmpty()) return null
            return LinkItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
        }
    }
}