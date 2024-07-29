package se.infomaker.iap.articleview.presentation

import se.infomaker.iap.articleview.preprocessor.PreprocessorConfig

data class ContentPresentation(val enrichments: MutableList<ArticleEnrichment> = mutableListOf()) {
    val preprocessors: List<PreprocessorConfig>
        get() = mutableListOf<PreprocessorConfig>().also { out ->
            out.addAll(enrichments.mapNotNull { it.beforePreprocessors }.flatten())
            out.addAll(enrichments.mapNotNull { it.afterPreprocessors }.flatten())
        }
}