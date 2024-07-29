package se.infomaker.profile.data

import androidx.compose.runtime.mutableStateOf
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class AuthenticationItem private constructor(
    val config: AuthenticationItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), MutableVisibility {

    override val name: String
        get() = "authentication"

    override val text: String? = null

    override val showIf: ShowIf?
        get() = config.showIf

    override val visibility = mutableStateOf(true)

    init {
        super.configure(config)
    }

    companion object {
        operator fun invoke(
            config: AuthenticationItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): AuthenticationItem = AuthenticationItem(config,
            sectionPosition,
            sectionIdentifier,
            moduleIdentifier,
            theme,
            resourceManager)
    }
}