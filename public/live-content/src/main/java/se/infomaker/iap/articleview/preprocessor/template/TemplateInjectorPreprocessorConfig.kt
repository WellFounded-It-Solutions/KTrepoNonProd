package se.infomaker.iap.articleview.preprocessor.template

import com.google.gson.annotations.SerializedName
import se.infomaker.iap.articleview.preprocessor.select.move.InsertConfig

data class TemplateInjectorPreprocessorConfig(
        val template: String,
        val insert: InsertConfig?,
        @SerializedName("selectorType") private val _selectorType: String? = null
) {
    val selectorType: String
        get() = _selectorType ?: "templateInjector"
}