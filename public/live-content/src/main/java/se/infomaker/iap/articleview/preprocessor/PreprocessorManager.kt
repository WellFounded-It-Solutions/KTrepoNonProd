package se.infomaker.iap.articleview.preprocessor

import com.google.gson.JsonObject
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.embed.HtmlEmbedItemPreprocessor
import se.infomaker.iap.articleview.item.flowplayer.FlowPlayerItemPreprocessor
import se.infomaker.iap.articleview.item.image.ImageGalleryItemPreprocessor
import se.infomaker.iap.articleview.item.image.SlideshowImageObjectAccumulatorPreprocessor
import se.infomaker.iap.articleview.item.image.ImageItemPreprocessor
import se.infomaker.iap.articleview.item.map.MapItemPreprocessor
import se.infomaker.iap.articleview.item.mergedelement.ElementMergerPreprocessor
import se.infomaker.iap.articleview.item.screen9.Screen9ItemPreprocessor
import se.infomaker.iap.articleview.item.static.StaticPreprocessor
import se.infomaker.iap.articleview.item.youplay.YouplayItemPreprocessor
import se.infomaker.iap.articleview.preprocessor.contentexplanation.ElementTemplatePreprocessor
import se.infomaker.iap.articleview.preprocessor.divider.DividerPreprocessor
import se.infomaker.iap.articleview.preprocessor.fallback.FallbackTemplatePreprocessor
import se.infomaker.iap.articleview.preprocessor.links.LinkCleanerPreprocessor
import se.infomaker.iap.articleview.preprocessor.links.LinkPropertiesPreprocessor
import se.infomaker.iap.articleview.preprocessor.select.apply.ApplyPreprocessor
import se.infomaker.iap.articleview.preprocessor.select.delete.DeletePreprocessor
import se.infomaker.iap.articleview.preprocessor.select.move.MovePreprocessor
import se.infomaker.iap.articleview.preprocessor.sticky.StickyPreprocessor
import se.infomaker.iap.articleview.preprocessor.template.InPlaceTemplatePreprocessor
import se.infomaker.iap.articleview.preprocessor.template.TemplatePreprocessor
import se.infomaker.iap.articleview.preprocessor.text.DropCapPreprocessor
import timber.log.Timber

object PreprocessorManager {
    private val PREPROCESSORS = mutableMapOf<String, Preprocessor>()
    private val EMPTY_CONFIGURATION = JsonObject()
    val DEFAULT_PREPROCESSOR_CONFIGURATION = listOf(
        PreprocessorConfig("linkCleaner", EMPTY_CONFIGURATION),
        PreprocessorConfig("linkProperties", EMPTY_CONFIGURATION),
        PreprocessorConfig("slideshowImageObjectAccumulator", EMPTY_CONFIGURATION)
    )

    init {
        registerPreprocessor("map", MapItemPreprocessor())
        registerPreprocessor("youplay", YouplayItemPreprocessor())
        registerPreprocessor("flowplayer", FlowPlayerItemPreprocessor())
        registerPreprocessor("imagegallery", ImageGalleryItemPreprocessor())
        registerPreprocessor("htmlEmbed", HtmlEmbedItemPreprocessor())
        registerPreprocessor("image", ImageItemPreprocessor())
        registerPreprocessor("move", MovePreprocessor())
        registerPreprocessor("applyPreprocessors", ApplyPreprocessor())
        registerPreprocessor("elementMerger", ElementMergerPreprocessor())
        registerPreprocessor("delete", DeletePreprocessor())
        registerPreprocessor("template", TemplatePreprocessor())
        registerPreprocessor("staticItem", StaticPreprocessor())
        registerPreprocessor("divider", DividerPreprocessor())
        registerPreprocessor("elementTemplate", ElementTemplatePreprocessor())
        registerPreprocessor("inPlaceTemplate", InPlaceTemplatePreprocessor())
        registerPreprocessor("fallbackTemplate", FallbackTemplatePreprocessor())
        registerPreprocessor("screen9", Screen9ItemPreprocessor())
        registerPreprocessor("linkCleaner", LinkCleanerPreprocessor())
        registerPreprocessor("sticky", StickyPreprocessor())
        registerPreprocessor("dropCap", DropCapPreprocessor())
        registerPreprocessor("slideshowImageObjectAccumulator", SlideshowImageObjectAccumulatorPreprocessor())
        registerPreprocessor("linkProperties", LinkPropertiesPreprocessor())
    }

    fun registerPreprocessor(name: String, preprocessor: Preprocessor) {
        PREPROCESSORS[name] = preprocessor
    }

    fun preprocess(
        content: ContentStructure,
        configuration: List<PreprocessorConfig>,
        resourceProvider: ResourceProvider,
    ): ContentStructure {
        return configuration.fold(content) { out, config ->
            try {
                return@fold PREPROCESSORS[config.name]?.process(out,
                    config.config.toString(),
                    resourceProvider)
                    ?: out
            } catch (e: Throwable) {
                Timber.e(e, "Failed to apply preprocessor with config: $config")
                return@fold out
            }
        }
    }
}