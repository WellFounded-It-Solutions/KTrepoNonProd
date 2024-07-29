package se.infomaker.profile.data

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class AppLinkItem private constructor (
    val config: AppLinkItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), ProfileItem {

    val packageName:String
        get() = config.parameters?.androidPackageName!!

    val fallbackUrl:String
        get() = config.parameters?.androidFallbackUrl!!

    override val text:String
        get() = config.parameters?.text.orEmpty()

    override val name: String
        get() = "appLink"

    override val image: Int
        get() = config.parameters?.image?.let {
            theme.getImage(null, it)?.resourceId ?: resources.getDrawableIdentifier(it)
        } ?: -1

    companion object {
        operator fun invoke(
            config: AppLinkItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): AppLinkItem? {
            if (config.parameters == null ||
                config.parameters.androidFallbackUrl.isNullOrEmpty() ||
                config.parameters.androidPackageName.isNullOrEmpty()) {
                return null
            }
            return AppLinkItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
        }
    }
}