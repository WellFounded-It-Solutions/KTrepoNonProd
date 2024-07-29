package se.infomaker.profile.data

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class LicensesItem private constructor(
    val config: LicenseItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), ProfileItem {

    override val name: String
        get() = "licenses"

    override val text: String
        get() = config.parameters?.text ?: run {
            resources.getString("profile_licenses", "Licenses")
        }

    override val image: Int
        get() = config.parameters?.image?.let {
            theme.getImage(null, it)?.resourceId ?: resources.getDrawableIdentifier(it)
        } ?: -1

    init {
        super.configure(config)
    }

    companion object {
        operator fun invoke(
            config: LicenseItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): LicensesItem = LicensesItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
    }
}