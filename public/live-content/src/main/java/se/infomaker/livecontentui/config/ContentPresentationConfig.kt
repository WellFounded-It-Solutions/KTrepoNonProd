package se.infomaker.livecontentui.config

import se.infomaker.iap.articleview.presentation.ThemeContentPresentation
import se.infomaker.iap.articleview.presentation.match.MatchMap
import se.infomaker.iap.articleview.util.Provider

data class ContentPresentationConfig(
        val teasers: List<TeaserSpecification>?,
        val themes: List<ThemeContentPresentation>?,
        val extraThemes: List<ThemeContentPresentation>?
) {
    companion object {
        const val DEFAULT_TEASER = "standard_default_teaser"
    }
}

data class TeaserSpecification(
        val layout: String,
        val match: MatchMap?,
        val require: List<String>?,
        val bindingOverrides: List<BindingOverride>?
) : Provider<MatchMap> {
    override fun provide() = match ?: emptyMap()
}