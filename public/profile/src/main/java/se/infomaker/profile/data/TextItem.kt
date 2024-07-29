package se.infomaker.profile.data

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import java.util.Locale

class TextItem private constructor(
    val config: TextItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager) {

    override val name: String
        get() = "text"

    override val text: String
        get() = resources.getString(config.parameters?.text?.lowercase(Locale.getDefault()), config.parameters?.text)

    init {
        super.configure(config)
    }

    companion object {
        operator fun invoke(
            config: TextItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): TextItem? {
            if (config.parameters == null || config.parameters.text.isNullOrEmpty()) {
                return null
            }
            return TextItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
        }
    }
}