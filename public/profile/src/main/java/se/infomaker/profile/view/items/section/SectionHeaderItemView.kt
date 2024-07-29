package se.infomaker.profile.view.items.section

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.data.SectionHeaderItem
import se.infomaker.profile.view.items.common.AnimatedItem
import se.infomaker.profile.view.items.common.LocalSectionDecorator
import se.infomaker.profile.view.items.common.ProfileCard

@FlowPreview
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun SectionHeaderItemView(sectionHeaderItem: SectionHeaderItem) {
    val model: ProfileViewModel = viewModel()
    AnimatedItem(sectionHeaderItem.visibility.value) {
        ProfileCard(
            backgroundColor = sectionHeaderItem.backgroundColor,
            position = sectionHeaderItem.position.value,
        ) {
            Column {
                Spacer(modifier = Modifier.height(LocalSectionDecorator.current.paddingTop))
                sectionHeaderItem.text?.let {
                    Text(
                        modifier = Modifier.padding(
                            top = 4.dp,
                            bottom = 0.dp,
                            start = model.indent,
                            end = 12.dp
                        ),
                        text = it,
                        style = sectionHeaderItem.textStyle
                    )
                }
            }
        }
    }
}

