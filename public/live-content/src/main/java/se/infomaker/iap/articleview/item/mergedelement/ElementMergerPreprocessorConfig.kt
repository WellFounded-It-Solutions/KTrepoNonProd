package se.infomaker.iap.articleview.item.mergedelement

import com.google.gson.annotations.SerializedName
import se.infomaker.iap.articleview.preprocessor.select.SelectorConfig
import se.infomaker.iap.articleview.preprocessor.select.move.InsertConfig
import se.infomaker.iap.articleview.preprocessor.select.move.InsertRelativeConfig

class ElementMergerPreprocessorConfig(
    val selectors: List<SelectorConfig> = listOf(),
    val insert: InsertConfig = InsertRelativeConfig(position = "after"),
    val themeKey: String? = null,
    val separatorThemeKey: String? = null,
    val separator: String = " ",
    @SerializedName("lastSeparator") private val _lastSeparator: String? = null,
    val type: String = "mergedElement"
) {
    val themeKeys: List<String>
        get() = mutableListOf<String>().apply {
            themeKey?.let { add(it) }
        }.toList()

    val lastSeparator: String
        get() = _lastSeparator ?: separator
}