package se.infomaker.profile.data

import androidx.compose.runtime.mutableStateOf
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import se.infomaker.iap.action.display.flow.condition.ShowIfDefinition

data class MyProfileConfig(
    val sections: List<Section>?,
)

data class Section(
    val title: String? = null,
    @SerializedName("views")
    val profileItemConfigs: MutableList<ProfileItemConfig>?,
    var id: String? = null,
)

interface ProfileItemConfig {
    val type: String?
}

data class SectionHeaderItemConfig(
    override val type: String? = null,
    val title: String? = null,
) : ProfileItemConfig

data class SectionFooterItemConfig(
    override val type: String? = null,
) : ProfileItemConfig

data class AuthenticationItemConfig(
    val title: String? = null,
    override val showIf: ShowIf? = null,
    override val type: String? = null,
) : ProfileItemConfig, MutableVisibility {
    override val visibility = mutableStateOf(true)
}

data class UserItemConfig(
    val name: String? = null,
    override val showIf: ShowIf? = null,
    override val type: String? = null,
) : ProfileItemConfig, MutableVisibility {
    override val visibility = mutableStateOf(true)
}

data class LinkItemConfig(
    val parameters: Parameters? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class AppLinkItemConfig(
    val parameters: AppLinkParameters? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class TextItemConfig(
    val parameters: Parameters? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class HtmlItemConfig(
    val parameters: Parameters? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class MailItemConfig(
    val parameters: MailParameters? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class ConsentItemConfig(
    val parameters: Parameters? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class ActionItemConfig(
    val parameters: JSONObject? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class LicenseItemConfig(
    val parameters: Parameters? = null,
    override val type: String? = null,
) : ProfileItemConfig

enum class SectionPosition {
    START, MIDDLE, END, BOTH
}

data class VersionItemConfig(
    val parameters: Parameters? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class SettingsItemConfig(
    val parameters: Parameters? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class UnknownItemConfigConfig(
    val title: String? = null,
    override val type: String? = null,
) : ProfileItemConfig

data class ShowIf(
    override val key: String? = null,
    override val operator: String? = null,
    override val value: String? = null,
) : ShowIfDefinition

data class Parameters(
    val text: String? = null,
    val url: String? = null,
    val image: String? = null,
)

data class AppLinkParameters(
    val text: String? = null,
    val image: String? = null,
    val androidPackageName: String? = null,
    val androidFallbackUrl: String? = null,
)

data class MailParameters(
    val text: String? = null,
    val subject: String? = null,
    val body: String? = null,
    val recipient: String? = null,
    val image: String? = null
)

data class ProfileIndentWrapper (val skipIndentingProfileViews: Boolean = false)