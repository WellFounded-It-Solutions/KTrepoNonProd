package se.infomaker.frt.ui.fragment

import se.infomaker.iap.articleview.preprocessor.PreprocessorConfig
import se.infomaker.iap.articleview.presentation.ContentPresentationConfig
import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentui.config.AdsConfig

data class ArticleConfig(
    val liveContent: LiveContentConfig,
    val contentPresentation: ContentPresentationConfig?,
    val preprocessors: List<PreprocessorConfig>?,
    val ads: AdsConfig?
)
