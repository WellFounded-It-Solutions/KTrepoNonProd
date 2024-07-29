package se.infomaker.profile.view.items.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.data.SettingsItem
import se.infomaker.profile.view.items.common.ImageColumn
import se.infomaker.profile.view.items.common.LocalProfileSettings
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.WrappableTextWithChevron
import se.infomaker.utilities.ActionHelper
import timber.log.Timber

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun SettingsItemView(
    /*@PreviewParameter(SettingsItemProvider::class)*/
    settingsItem: SettingsItem,
) {
    val settingsModuleTitle = LocalProfileSettings.current.defaultSettingsTitle
    val context = LocalContext.current
    val actionHelper = ActionHelper()
    val operation = remember {
        actionHelper.buildModuleOperation(
            context = context,
            moduleName = settingsModuleTitle,
            moduleId = settingsItem.moduleIdentifier,
            title = settingsItem.text
        )
    }
    val profileViewModel: ProfileViewModel = viewModel()

    ProfileCard(backgroundColor = settingsItem.backgroundColor,
        position = settingsItem.position.value,
        action = {
            operation?.let {
                actionHelper.executeOperation(context = context, operation = operation)
            } ?: run { Timber.d("Unable to perform operation, invalid configuration.") }
        }) {
        RowContainer {
            ImageColumn(settingsItem)
            WrappableTextWithChevron(profileItem = settingsItem, indent = profileViewModel.indent)
        }
    }
}