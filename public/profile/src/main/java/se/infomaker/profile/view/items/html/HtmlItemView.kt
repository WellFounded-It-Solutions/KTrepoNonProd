package se.infomaker.profile.view.items.html

import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.profile.data.HtmlItem
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.view.items.common.ProfileCard
import se.infomaker.profile.view.items.common.RowContainer
import se.infomaker.profile.view.items.common.rowPadding

@ExperimentalMaterialApi
@Composable
fun HtmlItemView(htmlItem: HtmlItem) {
    val theme by LocalContext.current.theme()
    val profileViewModel: ProfileViewModel = viewModel()
    ProfileCard(backgroundColor = htmlItem.backgroundColor, position = htmlItem.position.value) {
        RowContainer {
            AndroidView(
                factory = { context ->
                    ThemeableTextView(context).apply {
                        text = Html.fromHtml(htmlItem.text)
                        movementMethod = (LinkMovementMethod.getInstance())
                        apply(theme)
                    }
                },
                modifier = Modifier.rowPadding(start = profileViewModel.indent),
            )
        }
    }
}