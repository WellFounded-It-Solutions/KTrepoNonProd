package se.infomaker.iap.articleview.transformer

import se.infomaker.iap.articleview.ArticleConfig
import se.infomaker.iap.articleview.transformer.newsml.NewsMLTransformerManager

object TransformerProvider {

    private const val NEWS_ML_PROPERTY = "newsMLProperty"

    fun getTransformer(config: ArticleConfig): Transformer {
        val newsMLProperty = config.articleTransformerConfig?.get(NEWS_ML_PROPERTY)?.asString ?: "newsML"
        return NewsMLTransformerManager.createTransformer(newsMLProperty)
    }
}

