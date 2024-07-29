package se.infomaker.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

data class StyleData(val itemBackgroundColor: Color,
                     val dividerColor: Color,
                     val artifactTextStyle: TextStyle,
                     val groupTextStyle: TextStyle,
                     val licenseHeaderTextStyle: TextStyle = TextStyle.Default,
)