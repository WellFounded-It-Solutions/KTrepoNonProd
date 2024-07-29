package se.infomaker.profile.view.items.mail

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.MailItem
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.view.items.common.ImageColumn
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.WrappableText

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun MailItemView(mailItem: MailItem) {
    val context = LocalContext.current
    val mailHelper = MailButtonHelper(context = context, messageData = mailItem.messageData)
    val profileViewModel: ProfileViewModel = viewModel()

    ProfileCard(backgroundColor = mailItem.backgroundColor, position = mailItem.position.value,
        action = { mailHelper.createFeedbackEmail() }) {
        RowContainer {
            ImageColumn(mailItem)
            WrappableText(profileItem = mailItem, indent = profileViewModel.indent)
        }
    }
}

