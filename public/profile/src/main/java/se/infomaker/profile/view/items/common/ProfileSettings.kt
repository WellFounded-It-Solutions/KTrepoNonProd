package se.infomaker.profile.view.items.common

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ProfileSettings(
    val defaultVersionsTitle: String = "Versions",
    val defaultLicensesTitle: String = "Licenses",
    val defaultSettingsTitle: String = "Settings",
    val defaultConsentTitle: String = "Consent",
)

data class ModuleInfo(
    val id: String? = null,
)

data class SectionDecorator(val paddingTop: Dp = DEFAULT_PADDING,
                            val paddingBottom: Dp = DEFAULT_PADDING
){
    companion object {
        val DEFAULT_PADDING = 10.dp
    }
}

data class CardDecorator(
    val elevationColor: Color = DEFAULT_COLOR,
    val elevationThickness: Dp = DEFAULT_THICKNESS,
    val marginBottom: Dp = DEFAULT_MARGIN,
){
    companion object {
        val DEFAULT_MARGIN = 0.dp
        val DEFAULT_COLOR = Color(0x33000000)
        val DEFAULT_THICKNESS = 5.dp
    }
}

data class ListDecorator(
    val top: Dp = DEFAULT_MARGIN,
    val bottom: Dp = DEFAULT_MARGIN,
){
    companion object {
        val DEFAULT_MARGIN = 0.dp
    }
}

val LocalProfileSettings = compositionLocalOf { ProfileSettings() }
val LocalModuleInfo = compositionLocalOf {  ModuleInfo() }
val LocalCardDecorator = compositionLocalOf {  CardDecorator() }
val LocalSectionDecorator = compositionLocalOf {  SectionDecorator() }
val LocalListDecorator = compositionLocalOf { ListDecorator() }

