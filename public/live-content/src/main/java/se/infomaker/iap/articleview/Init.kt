package se.infomaker.iap.articleview

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.follow.FollowPropertyObjectPreprocessor
import se.infomaker.iap.articleview.follow.FollowPropertyObjectPreprocessorConfig
import se.infomaker.iap.articleview.follow.author.AuthorPreprocessor
import se.infomaker.iap.articleview.follow.author.AuthorPreprocessorConfig
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.item.ItemViewFactoryFactory
import se.infomaker.iap.articleview.item.ItemViewFactoryProviderFactory
import se.infomaker.iap.articleview.preprocessor.PreprocessorManager
import se.infomaker.streamviewer.di.PropertyObjectFollowItemViewFactoryFactory

class Init : AbstractInitContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InitEntryPoint {
        fun propertyObjectFollowItemViewFactoryFactory(): PropertyObjectFollowItemViewFactoryFactory
    }

    override fun init(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(context, InitEntryPoint::class.java)
        val factoryFactory = entryPoint.propertyObjectFollowItemViewFactoryFactory()
        ItemViewFactoryFactory.registerViewFactory(FOLLOW_PROPERTY_OBJECT_KEY, object : ItemViewFactoryProviderFactory {
            override fun create(resourceProvider: ResourceProvider, configJson: JsonObject): List<ItemViewFactory> {
                val config = GSON.fromJson(configJson, FollowPropertyObjectPreprocessorConfig::class.java)
                return listOf(factoryFactory.create(config.template))
            }
        })
        PreprocessorManager.registerPreprocessor(FOLLOW_PROPERTY_OBJECT_KEY, FollowPropertyObjectPreprocessor())

        ItemViewFactoryFactory.registerViewFactory(AUTHOR_KEY, object : ItemViewFactoryProviderFactory {
            override fun create(resourceProvider: ResourceProvider, config: JsonObject): List<ItemViewFactory> {
                val authorConfig = GSON.fromJson(config, AuthorPreprocessorConfig::class.java)
                return listOf(factoryFactory.create(authorConfig.template))
            }
        })
        PreprocessorManager.registerPreprocessor(AUTHOR_KEY, AuthorPreprocessor())
    }

    companion object {
        private const val FOLLOW_PROPERTY_OBJECT_KEY = "followPropertyObject"
        private const val AUTHOR_KEY = "author"

        private val GSON = Gson()
    }
}