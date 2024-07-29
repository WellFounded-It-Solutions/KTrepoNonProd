package se.infomaker.profile.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ktx.brandColor
import se.infomaker.utilities.toComposeColor
import se.infomaker.utilities.toComposeTextStyle
import java.util.Locale

class SectionHeaderItem private constructor (
    val config: SectionHeaderItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), MutableVisibility {

    override val name: String = "sectionHeader"
    override val backgroundColorThemeKeys: MutableList<String> = mutableListOf("profileSectionHeaderBackground", "profileSectionItemBackground", "profileSectionBackground")
    override val textStyleThemeKeys: MutableList<String> = mutableListOf("profileSectionHeader")
    override val visibility: MutableState<Boolean> = mutableStateOf(true)
    override val showIf: ShowIf? = null
    override val textStyle: TextStyle
        get() = theme.getText(textStyleThemeKeys, null)?.toComposeTextStyle(theme) ?: TextStyle(color = theme.brandColor.toComposeColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)

    override val text:String?
        get() = config.title?.let { resources.getString(it.lowercase(Locale.getDefault()), it) }

    companion object {
        operator fun invoke(
            config: SectionHeaderItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): SectionHeaderItem {
            return SectionHeaderItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
        }
    }
}