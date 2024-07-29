package se.infomaker.iap.articleview.offline

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.ArticleConfig
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.image.ImageGalleryItem
import se.infomaker.iap.articleview.item.image.ImageItem
import se.infomaker.iap.articleview.preprocessor.PreprocessorManager
import se.infomaker.iap.articleview.transformer.TransformerProvider
import timber.log.Timber
import kotlin.reflect.KClass

/**
 * Loads article content for offline use without displaying any content
 */
class HeadlessArticleLoader(private val config: ArticleConfig) {
    val transformer = TransformerProvider.getTransformer(config)
    private val itemLoaders = mutableMapOf<KClass<*>, ItemLoader<*>>()

    init {
        itemLoaders[ImageItem::class] = ImageItemLoader()
        itemLoaders[ImageGalleryItem::class] = ImageGalleryItemLoader()
        itemLoaders[TemplateItemLoader::class] = TemplateItemLoader(this)
    }

    /**
     * Load all preloadable content for the article
     */
    suspend fun load(context: Context, moduleId: String, presentationContext: JSONObject, properties: JSONObject) {
        coroutineScope{
            val resourceManager = ResourceManager(context, moduleId)
            val article = prepareArticle(properties, presentationContext, resourceManager)
            article.body.items.filter {
                itemLoaders[it::class] != null
            }.forEach { item ->
                launch(Dispatchers.IO) {
                    Timber.d("Preloading $item")
                    itemLoaders[item::class]?.load(context, item, resourceManager)
                }
            }
        }
    }

    suspend fun load(context: Context, item: Item, resourceManager: ResourceManager) {
        itemLoaders[item::class]?.load(context, item, resourceManager)
    }

    /**
     * Run all preprocessors for the article to prepare the content
     */
    private fun prepareArticle(properties: JSONObject, presentationContext: JSONObject, resourceManager: ResourceManager): ContentStructure {
        var article = transformer.transform(properties).withContentPresentation(config.contentPresentation, presentationContext)
        article.presentation.enrichments.mapNotNull { it.beforePreprocessors }.flatten().let {
            article = PreprocessorManager.preprocess(article, it, resourceManager)
        }
        article = PreprocessorManager.preprocess(article, PreprocessorManager.DEFAULT_PREPROCESSOR_CONFIGURATION, resourceManager)
        article = config.preprocessors?.let { PreprocessorManager.preprocess(article, it, resourceManager) }
                ?: article
        article.presentation.enrichments.mapNotNull { it.afterPreprocessors }.flatten().let {
            PreprocessorManager.preprocess(article, it, resourceManager)
        }
        return article
    }
}