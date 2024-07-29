package se.infomaker.profile.data

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

interface ProfileItem {

    val id:String

    val name:String

    val moduleIdentifier: String?

    val textStyleThemeKeys: MutableList<String>

    val textStyle: TextStyle

    val altTextStyleThemeKeys: MutableList<String>

    val altTextStyle: TextStyle

    val backgroundColorThemeKeys: MutableList<String>

    val backgroundColor: Color

    val position: MutableState<SectionPosition>

    val sectionIdentifier: String

    val trailingDrawable: Int

    val trailingDrawableTint: Color

    val image: Int

    val text: String?

    fun configure(config: ProfileItemConfig)
}

interface MutableVisibility {
    val visibility: MutableState<Boolean>
    val showIf: ShowIf?
}

internal val ProfileItem.invisible: Boolean
    get() = this is MutableVisibility && !this.visibility.value