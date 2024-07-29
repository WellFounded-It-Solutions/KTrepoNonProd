package se.infomaker.profile.view.items.link

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import se.infomaker.profile.data.LinkItemConfig
import se.infomaker.profile.data.Parameters

class LinkItemProvider : PreviewParameterProvider<LinkItemConfig> {
    override val values: Sequence<LinkItemConfig> =
        sequenceOf(
            LinkItemConfig(
                parameters = Parameters(text = "Link Name", url = "https://www.google.com")
            ),
            LinkItemConfig(
                parameters = Parameters(text = "Naviga Global", url = "https://www.navigaglobal.com")
            ),
        )
}