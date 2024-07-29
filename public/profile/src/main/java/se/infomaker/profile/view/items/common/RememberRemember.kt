package se.infomaker.profile.view.items.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.utilities.toComposeColor
import se.infomaker.utilities.toComposeTextStyle

@Composable
fun rememberThemeColor(
    context: Context = LocalContext.current,
    themeKeys: List<String>,
    default: Color = Color.Unspecified,
): Color = remember(context) {
    val theme by context.theme()
    theme.getColor(themeKeys, null)?.toComposeColor ?: default
}

@Composable
fun rememberThemeTextStyle(
    context: Context = LocalContext.current,
    themeKeys: List<String>,
    default: TextStyle = TextStyle.Default
): TextStyle = remember(context) {
    val theme by context.theme()
    theme.getText(themeKeys, null)?.toComposeTextStyle(theme) ?: default
}