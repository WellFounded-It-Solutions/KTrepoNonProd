package se.infomaker.profile.view.items.link

import android.content.Intent
import android.net.Uri
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.AppLinkItem
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.view.items.common.ImageColumn
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.WrappableTextWithChevron

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun AppLinkItemView(appLinkItem: AppLinkItem) {

    val context = LocalContext.current
    val intent = remember {
        context.packageManager.getLaunchIntentForPackage(appLinkItem.packageName)
            ?: Intent(Intent.ACTION_VIEW, Uri.parse(appLinkItem.fallbackUrl))
    }
    val profileViewModel: ProfileViewModel = viewModel()

    ProfileCard(
        backgroundColor = appLinkItem.backgroundColor,
        action = { context.startActivity(intent) }, position = appLinkItem.position.value
    ) {
        RowContainer {
            ImageColumn(profileItem = appLinkItem)
            WrappableTextWithChevron(appLinkItem, profileViewModel.indent)
        }
    }
}