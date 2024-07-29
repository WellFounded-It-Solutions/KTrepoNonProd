package se.infomaker.iap.articleview.preprocessor.fallback

import com.google.gson.Gson
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.MustacheException
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.FallbackTemplatePreprocessorConfig
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.fallback.FallbackItem
import se.infomaker.iap.articleview.preprocessor.contentexplanation.TemplateMap
import timber.log.Timber

/**
 * Assigns template to fallback items depending on mapped attributes
 */
class FallbackTemplatePreprocessor() : Preprocessor {
    override fun process(content: ContentStructure, jsonConfig: String, resourceProvider: ResourceProvider): ContentStructure {
        val gson = Gson()
        val config = gson.fromJson(jsonConfig, FallbackTemplatePreprocessorConfig::class.java)

        val templateMap: TemplateMap? = config.getFullTemplateMap(resourceProvider)
        val mustache = Mustache.compiler().compile(config.keyTemplate)
        content.body.items.mapNotNull {
            it as? FallbackItem
        }.forEach { fallbackItem ->
                    try {
                        val key = mustache.execute(fallbackItem.allAttributes)
                        templateMap?.get(key)?.let {
                            fallbackItem.typeIdentifier = "Fallback-$it"
                            fallbackItem.template = it
                        }
                    }
                    catch (e: MustacheException) {
                        // This is normal as attributes might not contain values
                    }
                }
        return content
    }


}