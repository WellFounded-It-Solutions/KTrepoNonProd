package se.infomaker.iap.articleview.item.element

import android.text.SpannableStringBuilder
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.TextItem
import java.util.StringTokenizer

data class ElementItem(
    val id: String,
    override var themeKeys: List<String>,
    val attributes: Map<String, String>,
    override val text: SpannableStringBuilder,
    val type: String = attributes["type"] ?: "default",
    val variation: String? = attributes["variation"],
    val prefix: String? = null
) : TextItem(id) {
    override val typeIdentifier = ElementItem::class.java
    override val selectorType = "element"
    override val matchingQuery = mapOf(
        "type" to type,
        "variation" to variation,
        "prefix" to prefix
    ).filterNullValues()

    override fun wordCount(): Int {
        if (text.isEmpty()) {
            return 0
        }
        return StringTokenizer(text.toString()).countTokens()
    }
}

fun Map<String, String?>.filterNullValues(): Map<String, String> {
    return this.filterValues { it != null }.mapValues { it.value as String }
}