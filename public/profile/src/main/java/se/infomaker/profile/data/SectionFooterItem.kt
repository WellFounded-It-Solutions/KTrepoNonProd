package se.infomaker.profile.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class SectionFooterItem private constructor (
    val config: SectionFooterItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), MutableVisibility {

    override val name: String = "sectionFooter"
    override val text: String? = null
    override val visibility: MutableState<Boolean> = mutableStateOf(true)
    override val showIf: ShowIf? = null
    override val backgroundColorThemeKeys: MutableList<String> = mutableListOf("profileSectionFooterBackground", "profileSectionItemBackground", "profileSectionBackground")

    companion object {
        operator fun invoke(
            config: SectionFooterItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): SectionFooterItem {
            return SectionFooterItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
        }
    }
}