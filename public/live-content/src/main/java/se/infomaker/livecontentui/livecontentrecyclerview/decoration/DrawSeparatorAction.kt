package se.infomaker.livecontentui.livecontentrecyclerview.decoration

import android.graphics.Canvas
import android.view.View
import se.infomaker.iap.articleview.ktx.suffixItems
import se.infomaker.iap.provisioning.ui.dp2px
import se.infomaker.iap.theme.Theme

class DrawSeparatorAction(
    theme: Theme,
    private val themeKeyPrefix: String? = null
) {
    private val separator = ContentListDividerFactory.create(theme, themeKeyPrefix = themeKeyPrefixOrFallback())
    private val separatorInset = theme.getSize(insetThemeKeys, null)?.sizePx?.toInt() ?: 12.dp2px()
    private val separatorSize = ContentListDividerFactory.size

    private val insetThemeKeys: List<String>
        get() = listOf("${themeKeyPrefixOrFallback()}Separator", "separator").suffixItems("Inset")

    private fun themeKeyPrefixOrFallback(): String = themeKeyPrefix ?: "listItem"

    operator fun invoke(canvas: Canvas, view: View) {
        val left = view.left + separatorInset
        val right = view.right - separatorInset
        val bottom = view.top + separatorSize
        separator.setBounds(left, view.top, right, bottom)
        separator.draw(canvas)
    }
}