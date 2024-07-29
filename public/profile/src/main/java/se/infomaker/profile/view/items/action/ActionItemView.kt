package se.infomaker.profile.view.items.action

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.frt.moduleinterface.action.GlobalActionHandler
import se.infomaker.profile.data.ActionItem
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.view.items.common.ImageColumn
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.WrappableTextWithChevron
import se.infomaker.utilities.ActionHelper


/*@Preview*/
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun ActionItemView(actionItem: ActionItem) {
    val context = LocalContext.current
    val operation = remember {
        actionItem.action?.let {
            val actionHelper = ActionHelper()
            actionHelper.buildModuleOperationFromJson(
                context = context,
                action = it,
                parameters = actionItem.actionParameters
            )
        }
    }
    val profileViewModel: ProfileViewModel = viewModel()

    ProfileCard(backgroundColor = actionItem.backgroundColor, position = actionItem.position.value,
        action = { GlobalActionHandler.getInstance().perform(context, operation) }) {
        RowContainer {
            ImageColumn(profileItem = actionItem)
            WrappableTextWithChevron(actionItem, profileViewModel.indent)
        }
    }
}
