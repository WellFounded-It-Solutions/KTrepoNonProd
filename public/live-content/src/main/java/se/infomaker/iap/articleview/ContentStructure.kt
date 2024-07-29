package se.infomaker.iap.articleview

import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.preprocessor.PreprocessorConfig
import se.infomaker.iap.articleview.preprocessor.PreprocessorManager
import se.infomaker.iap.articleview.presentation.ContentPresentation
import se.infomaker.iap.articleview.presentation.ContentPresentationConfig
import se.infomaker.iap.articleview.presentation.match.matches

/**
 * Encapsulates a body and content parameters
 */
data class ContentStructure(val body: ContentViewModel = ContentViewModel(), val properties: JSONObject, val presentation: ContentPresentation = ContentPresentation()) {

    fun withContentPresentation(contentPresentationConfig: ContentPresentationConfig?, presentationContext: JSONObject?): ContentStructure {
        contentPresentationConfig?.articles?.firstOrNull { it.provide().matches(properties, presentationContext) }?.let {
            return copy(presentation = ContentPresentation(mutableListOf(it)))
        }
        return this
    }

    fun preprocess(preprocessorConfigs: List<PreprocessorConfig>?, resourceManager: ResourceManager): ContentStructure {
        val preprocessors = mutableListOf<List<PreprocessorConfig>>()
        preprocessors.add(presentation.enrichments.mapNotNull { it.beforePreprocessors }.flatten())
        preprocessorConfigs?.let { preprocessors.add(it) }
        preprocessors.add(presentation.enrichments.mapNotNull { it.afterPreprocessors }.flatten())
        preprocessors.add(PreprocessorManager.DEFAULT_PREPROCESSOR_CONFIGURATION)

        preprocessors.forEach {
            PreprocessorManager.preprocess(this, it, resourceManager)
        }
        return this
    }
}