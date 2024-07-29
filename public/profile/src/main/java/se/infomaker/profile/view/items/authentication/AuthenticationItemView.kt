package se.infomaker.profile.view.items.authentication

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.profile.data.AuthenticationItem
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.data.SectionPosition
import se.infomaker.profile.view.items.authentication.data.AuthenticationItemEvent
import se.infomaker.profile.view.items.authentication.data.AuthenticationItemViewModel
import se.infomaker.profile.view.items.common.AnimatedItem
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.rowPadding

@FlowPreview
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun AuthenticationItemView(
    authenticationItem: AuthenticationItem,
    onTriggerEvent: (AuthenticationItemEvent) -> Unit,
) {
    val viewModel: AuthenticationItemViewModel = viewModel()
    AnimatedItem(authenticationItem.visibility.value) {
        RenderForState(
            authenticationItem = authenticationItem,
            authenticationState = viewModel.loginStatusState.collectAsState().value,
            onTriggerEvent = onTriggerEvent,
            position = authenticationItem.position.value
        )
    }
    AuthenticationDialog(dialogState = viewModel.authenticationDialogState.value)
}

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun RenderForState(
    authenticationItem: AuthenticationItem,
    authenticationState: LoginStatus,
    onTriggerEvent: (AuthenticationItemEvent) -> Unit,
    position: SectionPosition,
) {
    val viewModel: AuthenticationItemViewModel = viewModel()
    val context = LocalContext.current
    val resourceManager by context.resources()

    when (authenticationState) {
        LoginStatus.LOGGED_IN -> AuthButton(
            backgroundColor = authenticationItem.backgroundColor,
            text = resourceManager.getString("user_logout_prompt", null),
            textStyle = authenticationItem.textStyle,
            position = position
        ) {
            viewModel.authenticationDialogState.value =
                viewModel.authenticationDialogState.value.copy(
                    visible = true,
                    dismissAction = {
                        viewModel.authenticationDialogState.value =
                            viewModel.authenticationDialogState.value.copy(visible = false)
                    },
                    authAction = {
                        viewModel.authenticationDialogState.value =
                            viewModel.authenticationDialogState.value.copy(visible = false)
                        onTriggerEvent(AuthenticationItemEvent.Logout(context = context))
                    },
                )
        }
        LoginStatus.LOGGED_OUT -> AuthButton(
            text = resourceManager.getString("user_login_prompt", null),
            backgroundColor = authenticationItem.backgroundColor,
            textStyle = authenticationItem.textStyle,
            position = position,
        ) {
            onTriggerEvent(AuthenticationItemEvent.Login(context = context))
        }

        LoginStatus.IN_PROGRESS -> LoadingIndicator(backgroundColor = authenticationItem.backgroundColor,
            position = position)
        LoginStatus.UNSUPPORTED -> Text("Login not supported")
        LoginStatus.NEGOTIATING -> LoadingIndicator(backgroundColor = authenticationItem.backgroundColor,
            position = position)
    }
}

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun AuthButton(
    text: String,
    textStyle: TextStyle,
    backgroundColor: Color,
    position: SectionPosition,
    action: (() -> Unit)? = null,
) {
    val profileViewModel: ProfileViewModel = viewModel()
    ProfileCard(backgroundColor = backgroundColor, position = position, action = action) {
        RowContainer {

        //Row(modifier = Modifier.padding(top = 6.dp, bottom = 6.dp, end = 6.dp)) {
            Text(
                modifier = Modifier.rowPadding(start = profileViewModel.indent),
                text = text,
                style = textStyle,
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun LoadingIndicator(position: SectionPosition, backgroundColor: Color) {
    ProfileCard(backgroundColor = backgroundColor, position = position) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp, bottom = 6.dp)) {
            CircularProgressIndicator(color = MaterialTheme.colors.primary)
        }
    }
}

/*
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
@Preview
fun AuthenticationItemViewPreview(@PreviewParameter(AuthenticationItemProvider::class) authPreviewParams: AuthPreviewParams) {
    AuthenticationItemView(
        authenticationItem = authPreviewParams.authenticationItem,
        authenticationState = authPreviewParams.authenticationState
    )
}

data class AuthPreviewParams(
    val authenticationItem: AuthenticationItemConfig,
    val authenticationState: AuthenticationState
)*/
