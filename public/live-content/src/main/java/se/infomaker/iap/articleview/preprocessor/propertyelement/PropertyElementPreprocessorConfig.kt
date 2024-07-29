package se.infomaker.iap.articleview.preprocessor.propertyelement


data class PropertyElementConfig(val property: String, val type: String)
class PropertyElementPreprocessorConfig {
    var properties: List<PropertyElementConfig>? = listOf()
}