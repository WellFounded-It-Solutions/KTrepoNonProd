package se.infomaker.iap.articleview.item

import android.text.SpannableStringBuilder

abstract class TextItem(id: String) : Item(id) {
    abstract val text: SpannableStringBuilder
    abstract val themeKeys: List<String>
}