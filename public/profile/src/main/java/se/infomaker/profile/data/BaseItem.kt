package se.infomaker.profile.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import se.infomaker.utilities.toComposeColor
import se.infomaker.utilities.toComposeTextStyle
import java.util.UUID

abstract class BaseItem(
    sectionPosition: SectionPosition,
    override val sectionIdentifier: String,
    val theme: Theme, val resources: ResourceManager,
) : ProfileItem {

    override val id: String = UUID.randomUUID().toString()

    override val position = mutableStateOf(sectionPosition)

    override val image: Int
        get() = -1

    override val textStyleThemeKeys = mutableListOf("profileSectionItemText")

    override val textStyle: TextStyle
        get() = theme.getText(textStyleThemeKeys, null)?.toComposeTextStyle(theme)
            ?: TextStyle.Default

    override val backgroundColorThemeKeys = mutableListOf("profileSectionItemBackground")

    override val altTextStyle: TextStyle
        get() = theme.getText(altTextStyleThemeKeys, null)?.toComposeTextStyle(theme) ?: TextStyle(color = Color(0xFF808080), fontSize = 14.sp)

    override val altTextStyleThemeKeys = mutableListOf("profileSectionItemSecondaryText")

    override val backgroundColor: Color
        get() = theme.getColor(backgroundColorThemeKeys, null)?.toComposeColor ?: Color.White

    override val trailingDrawable
        get() = resources.getDrawableIdentifier("trailing_icon")

    override val trailingDrawableTint: Color
        get() = theme.getColor(
            listOf(
                "${name}ProfileTrailingIcon",
                "profileTrailingIcon",
                "${name}ProfileSectionItemSecondaryText",
                "profileSectionItemSecondaryText"
            ), null
        )?.toComposeColor ?: Color(0x61000000)

    override fun configure(config: ProfileItemConfig) {
        addBackgroundThemeKey("${name}ProfileSectionItemBackground")
        addTextStyleThemeKey("${name}ProfileSectionItemText")
        addAltTextStyleThemeKey("${name}ProfileSectionItemSecondaryText")
    }

    fun addBackgroundThemeKey(key: String) = backgroundColorThemeKeys.add(0, key)

    fun addTextStyleThemeKey(key: String) = textStyleThemeKeys.add(0, key)

    fun addAltTextStyleThemeKey(key: String) = altTextStyleThemeKeys.add(0, key)

}
