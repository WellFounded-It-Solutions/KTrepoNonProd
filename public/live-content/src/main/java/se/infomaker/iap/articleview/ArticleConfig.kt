package se.infomaker.iap.articleview

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.preprocessor.PreprocessorConfig
import se.infomaker.iap.articleview.preprocessor.contentexplanation.TemplateMap
import se.infomaker.iap.articleview.presentation.ContentPresentationConfig
import timber.log.Timber
import java.io.Serializable

data class ArticleConfig(
        val articleTransformer: String?,
        val articleTransformerConfig: JsonObject?,
        val fallbackConfig: FallbackTemplatePreprocessorConfig?,
        val contentPresentation: ContentPresentationConfig?,
        val preprocessors: List<PreprocessorConfig>?,
        val textSizeSteps: List<Float>?,
        val hideTextSizeModifier: Boolean?
) : Serializable

data class FallbackTemplatePreprocessorConfig(val keyTemplate: String?, val templateMap: JsonElement?) {
    fun getFullTemplateMap(resourceProvider: ResourceProvider): TemplateMap? {
        return templateMap?.let { templateMap ->
            try {
                when (templateMap) {
                    is JsonObject -> {
                        Gson().fromJson(templateMap, TemplateMap::class.java)
                    }
                    else -> {
                        resourceProvider.getAsset("configuration/${templateMap.asString}", TemplateMap::class.java)
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Could not create templateMap")
                null
            }
        }
    }
}