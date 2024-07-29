package se.infomaker.profile.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class UserItem private constructor(
    val config: UserItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), MutableVisibility {

    override val name: String
        get() = "user"
    override val text: String? = null

    override val visibility: MutableState<Boolean> = mutableStateOf(true)

    override val showIf: ShowIf?
        get() = config.showIf

    val userName:String?
        get() = config.name

    init {
        super.configure(config)
    }

    companion object {
        operator fun invoke(
            config: UserItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): UserItem? {
            if (config.showIf == null ||
                config.showIf.key.isNullOrEmpty() ||
                config.showIf.operator.isNullOrEmpty() ||
                config.showIf.value.isNullOrEmpty()
            ) {
                return null
            }
            return UserItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
        }
    }
}