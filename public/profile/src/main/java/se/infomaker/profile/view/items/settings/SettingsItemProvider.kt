package se.infomaker.profile.view.items.settings

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import se.infomaker.profile.data.Parameters
import se.infomaker.profile.data.SettingsItemConfig

class SettingsItemProvider : PreviewParameterProvider<SettingsItemConfig> {
    override val values: Sequence<SettingsItemConfig> =
        sequenceOf(
            SettingsItemConfig(
                parameters = Parameters(text = "Custom String")
            ),
            SettingsItemConfig(
                parameters = Parameters()
            )
        )
}