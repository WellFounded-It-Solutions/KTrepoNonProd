package se.infomaker.profile.view.items.text

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.data.TextItem
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.rowPadding

@FlowPreview
@ExperimentalMaterialApi
@Composable
fun TextItemView(textItem: TextItem) {
    val profileViewModel: ProfileViewModel = viewModel()
    ProfileCard(
        backgroundColor = textItem.backgroundColor, position = textItem.position.value,
    ) {
        RowContainer {
            Text(
                modifier = Modifier.rowPadding(start = profileViewModel.indent),
                text = textItem.text,
                style = textItem.textStyle,
            )

        }
    }
}