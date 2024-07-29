package se.infomaker.profile.data

import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme

class ProfileItemFactory(val theme: Theme, val resourceManager: ResourceManager) {

    fun buildItemForConfig(
        config: ProfileItemConfig,
        sectionPosition: SectionPosition,
        sectionIdentifier: String,
        moduleIdentifier: String?
    ): ProfileItem? {
        return when (config) {
            is ActionItemConfig -> ActionItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is AppLinkItemConfig -> AppLinkItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is AuthenticationItemConfig -> AuthenticationItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is ConsentItemConfig -> ConsentItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is HtmlItemConfig -> HtmlItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is LicenseItemConfig -> LicensesItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is LinkItemConfig -> LinkItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is MailItemConfig -> MailItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is SettingsItemConfig -> SettingsItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is SectionFooterItemConfig -> SectionFooterItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is SectionHeaderItemConfig -> SectionHeaderItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is TextItemConfig -> TextItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is UserItemConfig -> UserItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            is VersionItemConfig -> VersionItem(config, sectionPosition, sectionIdentifier, moduleIdentifier, theme, resourceManager)
            else -> null
        }
    }
}