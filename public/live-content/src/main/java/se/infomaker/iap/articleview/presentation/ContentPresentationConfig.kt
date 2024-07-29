package se.infomaker.iap.articleview.presentation

import se.infomaker.iap.articleview.preprocessor.PreprocessorConfig
import se.infomaker.iap.articleview.presentation.match.MatchMap
import se.infomaker.iap.articleview.util.Provider

data class ContentPresentationConfig(
        val articles: List<ArticleEnrichment>?,
        val themes: List<ThemeContentPresentation>?,
        val extraThemes: List<ThemeContentPresentation>?
)

data class ArticleEnrichment(
        val match: MatchMap?,
        val beforePreprocessors: List<PreprocessorConfig>?,
        val afterPreprocessors: List<PreprocessorConfig>?
) : Provider<MatchMap> {
    override fun provide() = match ?: emptyMap()
}

data class ThemeContentPresentation(
        val match: MatchMap?,
        val themes: List<String>
) : Provider<MatchMap> {
    override fun provide() = match ?: emptyMap()
}