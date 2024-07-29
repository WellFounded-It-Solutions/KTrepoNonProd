package se.infomaker.livecontentui.config

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

import se.infomaker.livecontentmanager.config.LiveContentConfig
import java.io.Serializable

open class LiveContentUIConfig(
    @SerializedName("liveContent") private val _liveContent: LiveContentConfig? = null,
    @SerializedName("templates") private val _templates: Map<String, TemplateConfig>? = null,
    @SerializedName("article") private val _article: ArticleConfig? = null,
    @SerializedName("pagerEffect") private val _pagerEffect: String? = null,
    @SerializedName("endOfContentsText") private val _endOfContentsText: String? = null,
    @SerializedName("liveContentNewEventsTitle") private val _liveContentNewEventsTitle: String? = null,
    @SerializedName("imageBaseUrl") private val _imageBaseUrl: String? = null,
    @SerializedName("media") private val _media: MediaConfig? = null,
    @SerializedName("gridLayout") private val _gridLayout: GridLayoutConfig? = null,
    @SerializedName("showBookmarkTeaserOverlay") private val _showBookmarkTeaserOverlay: Boolean? = null,
) : Serializable {

    var themeOverlayMapping: ThemeOverlayConfig? = null
    val liveContent: LiveContentConfig
        get() = _liveContent ?: throw InvalidConfigurationException("With liveContent there is not much to do..")
    val templates: Map<String, TemplateConfig>
        get() = _templates ?: emptyMap()
    var contentTypeTemplates: Map<String, ContentTypeTemplateConfig>? = null
    var translucentToolbar: Boolean = false
    var imageProvider: String? = null
    val imageBaseUrl: String
        get() = _imageBaseUrl ?: throw InvalidConfigurationException("Need imageBaseUrl to know how to load images.")
    val liveContentNewEventsTitle: String
        get() = _liveContentNewEventsTitle ?: ""
    val endOfContentsText: String
        get() = _endOfContentsText ?: ""
    val article: ArticleConfig
        get() = _article ?: ArticleConfig()
    var sharing: SharingConfig? = null
    var linkModuleId: String? = null
    val media: MediaConfig
        get() = _media ?: MediaConfig()
    var contentViewConfig: ContentViewConfig? = null
    var ads: AdsConfig? = null
    var contentView: String? = null
    var articletransformer: String? = null
    var articleHeadlineProperty: String? = null
    var articletransformerconfig: JsonObject? = null
    var errorConfiguration: ErrorConfiguration? = null

    val pagerEffect: String
        get() = _pagerEffect ?: "depth"

    var featureConfiguration = mapOf<String, FeatureConfiguration>()

    val gridLayout: GridLayoutConfig? get() = _gridLayout

    var contentViewConfiguration: JsonObject? = null

    var contentPresentation: ContentPresentationConfig? = null

    val showBookmarkTeaserOverlay: Boolean
        get() = _showBookmarkTeaserOverlay ?: true

    constructor(from: LiveContentUIConfig) : this(
        from.liveContent,
        from.templates,
        from.article,
        from.pagerEffect,
        from.endOfContentsText,
        from.liveContentNewEventsTitle,
        from.imageBaseUrl,
        from.media,
        from.gridLayout
    ) {
        this.themeOverlayMapping = from.themeOverlayMapping
        this.translucentToolbar = from.translucentToolbar
        this.imageProvider = from.imageProvider
        this.sharing = from.sharing
        this.contentViewConfig = from.contentViewConfig
        this.ads = from.ads
        this.contentView = from.contentView
        this.articletransformer = from.articletransformer
        this.articleHeadlineProperty = from.articleHeadlineProperty
        this.articletransformerconfig = from.articletransformerconfig
        this.featureConfiguration = from.featureConfiguration
        this.contentViewConfiguration = from.contentViewConfiguration
        this.errorConfiguration = from.errorConfiguration
        this.contentPresentation = from.contentPresentation
    }

    fun getListProperties(type: String?): List<String> {
        return liveContent.getListProperties(type ?: "")
    }

    fun getProperties(type: String?): String = liveContent.getProperties(type ?: "")

    fun getProperties(): String = getProperties(liveContent.defaultPropertyMap)

    fun getListProperties(): List<String> {
        return getListProperties(liveContent.defaultPropertyMap)
    }
}

data class ContentViewConfig(var articleLinkModuleId: String?)
