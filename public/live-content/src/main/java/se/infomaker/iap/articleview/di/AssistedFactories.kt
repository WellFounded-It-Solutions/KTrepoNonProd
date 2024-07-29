package se.infomaker.iap.articleview.di

import dagger.assisted.AssistedFactory
import se.infomaker.frt.ui.fragment.ArticleConfig
import se.infomaker.frt.ui.fragment.ArticleViewModel
import se.infomaker.frtutilities.ResourceManager

@AssistedFactory
interface ArticleViewModelFactory {
    fun create(moduleId: String, resources: ResourceManager, config: ArticleConfig): ArticleViewModel
}