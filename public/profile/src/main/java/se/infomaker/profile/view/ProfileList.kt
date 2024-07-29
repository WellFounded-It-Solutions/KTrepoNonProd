package se.infomaker.profile.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.view.items.common.LocalListDecorator
import se.infomaker.profile.view.items.common.LocalModuleInfo
import se.infomaker.profile.view.items.common.rememberThemeColor


@FlowPreview
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun ProfileList(
    viewModel: ProfileViewModel,
) {
    val moduleId = LocalModuleInfo.current.id
    val backgroundColor =
        rememberThemeColor(themeKeys = listOf("${moduleId}ProfileSectionBackground", "sectionBackground"))

    val profileItems = viewModel.state
    val listState = rememberLazyListState()
    LazyColumn(state = listState, modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)) {
        itemsIndexed(items = profileItems, key = { _, item -> item.id }) { index, item ->
            if (index == 0) {
                Spacer(modifier = Modifier.height(LocalListDecorator.current.top))
            }
            BuildViewForItem(profileItem = item)

            if (index == profileItems.size - 1) {
                Spacer(modifier = Modifier.height(LocalListDecorator.current.bottom))
            }
        }
    }
}