package se.infomaker.profile.view.items.section

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.SectionFooterItem
import se.infomaker.profile.view.items.common.AnimatedItem
import se.infomaker.profile.view.items.common.LocalSectionDecorator
import se.infomaker.profile.view.items.common.ProfileCard

@FlowPreview
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun SectionFooterItemView(sectionFooterItem: SectionFooterItem) {
    AnimatedItem(sectionFooterItem.visibility.value) {
        ProfileCard(
            backgroundColor = sectionFooterItem.backgroundColor,
            position = sectionFooterItem.position.value,
        ) {
            Spacer(modifier = Modifier.height(LocalSectionDecorator.current.paddingBottom))
        }
    }
}