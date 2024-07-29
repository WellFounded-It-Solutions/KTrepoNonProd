package se.infomaker.profile.data

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class ConsentItem private constructor(
    val config: ConsentItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager) {

    override val name: String
        get() = "consent"

    init {
        super.configure(config)
    }

    override val text:String
        get() = config.parameters?.text ?: run {
            resources.getString("change_consent_button", "Consent")
    }

    override val image = config.parameters?.image?.let { theme.getImage(it, null)?.resourceId ?: resources.getDrawableIdentifier(it) } ?: -1

    companion object {
        operator fun invoke(
            config: ConsentItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): ConsentItem? {
            return ConsentItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
        }
    }
}