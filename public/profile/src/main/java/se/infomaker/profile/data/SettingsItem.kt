package se.infomaker.profile.data

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class SettingsItem private constructor(
    val config: SettingsItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), ProfileItem {

    override val name: String
        get() = "settings"

    override val text: String?
        get() = config.parameters?.text ?: run {
            resources.getString("profile_settings", "Settings")
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
            config: SettingsItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): SettingsItem = SettingsItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
    }
}