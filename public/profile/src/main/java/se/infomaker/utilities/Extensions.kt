package se.infomaker.utilities

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.font.ThemeFont
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.style.text.ThemeTextStyle

fun ThemeTextStyle.toComposeTextStyle(theme: Theme): TextStyle {
    //this.getAlignment(theme.getAlignment())
    val size = this.getSize(theme).size
    val color: Color = this.getColor(theme).toComposeColor
    val font = this.getFont(theme)
    return TextStyle(color = color, fontSize = size.sp, fontFamily = font.toComposeFont())
}

val ThemeColor.toComposeColor: Color
    get() = Color(this.get())

val ThemeSize.toDp: Dp
    get() = this.size.dp

fun ThemeFont.toComposeFont(): FontFamily = FontFamily(this.typeface)

internal val String.mustachio: String
    get() = this.replace("{{", "").replace("}}", "")

