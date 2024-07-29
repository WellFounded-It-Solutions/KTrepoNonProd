package se.infomaker.iap.articleview.preprocessor

import se.infomaker.iap.articleview.item.ItemViewFactory

interface ViewFactoryProvidingPreprocessor {
    fun create(config: String): List<ItemViewFactory>
}