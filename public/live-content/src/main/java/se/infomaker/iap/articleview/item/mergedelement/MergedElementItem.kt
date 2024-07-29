package se.infomaker.iap.articleview.item.mergedelement

import android.text.SpannableStringBuilder
import se.infomaker.iap.articleview.item.TextItem

data class MergedElementItem(
    val items: List<TextItem>,
    val separator: String = " ",
    val lastSeparator: String = separator,
    override val themeKeys: List<String> = emptyList(),
    val separatorThemeKey: String?,
    val type: String = "mergedElement"
) : TextItem(items.joinToString(separator = ":") { it.uuid }) {

    override val typeIdentifier = MergedElementItem::class.java

    override val selectorType = "mergedElement"
    override val matchingQuery: Map<String, String> = mapOf("type" to type)

    override val text: SpannableStringBuilder
        get() = items.joinTexts(SpannableStringBuilder(), separator = separator, lastSeparator = lastSeparator) {
            it.text
        }
}

internal fun <T, A : Appendable> List<T>.joinTexts(buffer: A, separator: CharSequence, lastSeparator: CharSequence, transform: ((T) -> CharSequence)? = null): A {
    for (i in 0 until size) {
        if (i > 0 && i == size - 1) buffer.append(lastSeparator)
        else if (i > 0) buffer.append(separator)
        buffer.appendElement(get(i), transform)
    }
    return buffer
}

// This was internal in kotlin.text
private fun <T> Appendable.appendElement(element: T, transform: ((T) -> CharSequence)?) {
    when {
        transform != null -> append(transform(element))
        element is CharSequence? -> append(element)
        element is Char -> append(element)
        else -> append(element.toString())
    }
}