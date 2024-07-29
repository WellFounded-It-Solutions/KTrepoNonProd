package se.infomaker.iap.articleview.preprocessor.text

import se.infomaker.iap.articleview.preprocessor.select.SelectorConfig

data class DropCapPreprocessorConfig(
    val themeKey: String?,
    val select: SelectorConfig
)
