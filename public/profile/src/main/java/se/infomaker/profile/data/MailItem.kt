package se.infomaker.profile.data

import se.infomaker.profile.view.items.mail.MessageData
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class MailItem private constructor(
    val config: MailItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), ProfileItem {

    override val name: String
        get() = "mail"

    override val text: String
        get() = config.parameters?.text.orEmpty()

    override val image: Int
        get() = config.parameters?.image?.let {
            theme.getImage(null, it)?.resourceId ?: resources.getDrawableIdentifier(it)
        } ?: -1

    val messageData: MessageData
        get() = MessageData(email = config.parameters!!.recipient!!,
            subject = config.parameters.subject,
            body = config.parameters.body)

    init {
        super.configure(config)
    }

    companion object {
        operator fun invoke(
            config: MailItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): MailItem? {
            if (config.parameters == null || config.parameters.recipient.isNullOrEmpty()) {
                return null
            }
            return MailItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
        }
    }
}