package se.infomaker.profile.view.items.versions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.data.VersionItem
import se.infomaker.profile.view.items.common.ImageColumn
import se.infomaker.profile.view.items.common.LocalProfileSettings
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.ProfileIcon
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.rowPadding
import se.infomaker.utilities.ActionHelper
import timber.log.Timber

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun VersionItemView(
/*@PreviewParameter(SettingsItemProvider::class)*/
    versionItem: VersionItem,
) {
    val versionModuleTitle = LocalProfileSettings.current.defaultVersionsTitle
    val context = LocalContext.current
    val actionHelper = ActionHelper()
    val operation = remember {
        actionHelper.buildModuleOperation(
            context = context,
            moduleName = versionModuleTitle,
            moduleId = versionItem.moduleIdentifier,
            title = versionItem.text
        )
    }
    val profileViewModel: ProfileViewModel = viewModel()
    ProfileCard(backgroundColor = versionItem.backgroundColor,
        position = versionItem.position.value,
        action = {
            operation?.let {
                actionHelper.executeOperation(context = context, operation = operation)
            } ?: run { Timber.d("Unable to perform operation, invalid configuration.") }
        }) {

        RowContainer {
            ImageColumn(versionItem)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rowPadding(if (versionItem.image != -1) 0.dp else profileViewModel.indent),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = versionItem.text,
                            style = versionItem.textStyle,
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = versionItem.version(LocalContext.current),
                                style = versionItem.altTextStyle,
                            )
                            ProfileIcon(
                                identifier = versionItem.trailingDrawable,
                                color = versionItem.trailingDrawableTint
                            )


                        }
                    }
                }
            }
        }
    }
}

