package se.infomaker.profile.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.ActionItem
import se.infomaker.profile.data.AppLinkItem
import se.infomaker.profile.data.AuthenticationItem
import se.infomaker.profile.data.ConsentItem
import se.infomaker.profile.data.HtmlItem
import se.infomaker.profile.data.LicensesItem
import se.infomaker.profile.data.LinkItem
import se.infomaker.profile.data.MailItem
import se.infomaker.profile.data.ProfileItem
import se.infomaker.profile.data.SectionFooterItem
import se.infomaker.profile.data.SectionHeaderItem
import se.infomaker.profile.data.SettingsItem
import se.infomaker.profile.data.TextItem
import se.infomaker.profile.data.UserItem
import se.infomaker.profile.data.VersionItem
import se.infomaker.profile.view.items.action.ActionItemView
import se.infomaker.profile.view.items.authentication.AuthenticationItemView
import se.infomaker.profile.view.items.authentication.data.AuthenticationItemViewModel
import se.infomaker.profile.view.items.consent.ConsentItemView
import se.infomaker.profile.view.items.html.HtmlItemView
import se.infomaker.profile.view.items.licenses.LicensesItemView
import se.infomaker.profile.view.items.link.AppLinkItemView
import se.infomaker.profile.view.items.link.LinkItemView
import se.infomaker.profile.view.items.mail.MailItemView
import se.infomaker.profile.view.items.section.SectionFooterItemView
import se.infomaker.profile.view.items.section.SectionHeaderItemView
import se.infomaker.profile.view.items.settings.SettingsItemView
import se.infomaker.profile.view.items.text.TextItemView
import se.infomaker.profile.view.items.user.UserItemView
import se.infomaker.profile.view.items.versions.VersionItemView


@FlowPreview
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun BuildViewForItem(profileItem: ProfileItem) {
    val authenticationItemViewModel: AuthenticationItemViewModel = viewModel()
    when (profileItem) {
        is ActionItem -> ActionItemView(profileItem)
        is AppLinkItem -> AppLinkItemView(profileItem)
        is AuthenticationItem -> AuthenticationItemView(profileItem, authenticationItemViewModel::onTriggerEvent)
        is ConsentItem -> ConsentItemView(profileItem)
        is HtmlItem -> HtmlItemView(profileItem)
        is LicensesItem -> LicensesItemView(profileItem)
        is LinkItem -> LinkItemView(profileItem)
        is MailItem -> MailItemView(profileItem)
        is SectionFooterItem -> SectionFooterItemView(profileItem)
        is SectionHeaderItem -> SectionHeaderItemView(profileItem)
        is SettingsItem -> SettingsItemView(profileItem)
        is TextItem -> TextItemView(profileItem)
        is UserItem -> UserItemView(profileItem)
        is VersionItem -> VersionItemView(profileItem)
    }
}

