package se.infomaker.profile.view.items.link

import android.content.Intent
import android.net.Uri
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.LinkItem
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.view.items.common.ImageColumn
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.WrappableTextWithChevron

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun LinkItemView(linkItem: LinkItem) {
    val context = LocalContext.current
    val webIntent = remember { Intent(Intent.ACTION_VIEW, Uri.parse(linkItem.url)) }
    val profileViewModel: ProfileViewModel = viewModel()

    ProfileCard(backgroundColor = linkItem.backgroundColor,
        action = { context.startActivity(webIntent) }, position = linkItem.position.value) {
        RowContainer {
            ImageColumn(linkItem)
            WrappableTextWithChevron(profileItem = linkItem, indent = profileViewModel.indent)
        }
    }
}