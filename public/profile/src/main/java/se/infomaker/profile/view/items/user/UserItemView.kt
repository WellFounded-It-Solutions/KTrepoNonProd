package se.infomaker.profile.view.items.user

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.data.UserItem
import se.infomaker.profile.view.items.authentication.data.AuthenticationItemViewModel
import se.infomaker.profile.view.items.common.AnimatedItem
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.rowPadding

@FlowPreview
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun UserItemView(
    userItem: UserItem
) {
    val viewModel: AuthenticationItemViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    AnimatedItem(userItem.visibility.value) {
        ProfileCard(backgroundColor = userItem.backgroundColor,
            position = userItem.position.value) {
            RowContainer {
                Text(
                    text = viewModel.usernameState.collectAsState().value,
                    style = userItem.textStyle,
                    modifier = Modifier.rowPadding(start = profileViewModel.indent)
                )
            }
        }
    }
}