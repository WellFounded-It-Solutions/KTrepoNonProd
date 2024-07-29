package se.infomaker.iap.articleview.item

import com.google.gson.Gson
import com.google.gson.JsonObject
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.FallbackTemplatePreprocessorConfig
import se.infomaker.iap.articleview.item.fallback.FallbackItemViewFactory
import se.infomaker.iap.articleview.item.static.StaticItemPreprocessorConfig
import se.infomaker.iap.articleview.item.static.StaticItemViewFactory
import se.infomaker.iap.articleview.item.template.InPlaceTemplateItemViewFactory
import se.infomaker.iap.articleview.item.template.TemplateItemViewFactory
import se.infomaker.iap.articleview.preprocessor.PreprocessorConfig
import se.infomaker.iap.articleview.preprocessor.template.InPlaceTemplatePreprocessorConfig
import se.infomaker.iap.articleview.preprocessor.template.TemplatePreprocessorConfig
import javax.inject.Inject

class ItemViewFactoryProviderBuilder @Inject constructor(
    private val defaultItemViewFactoryProvider: DefaultItemViewFactoryProvider
) {
    private val defaultFactories = defaultFactoryFactories.mapValues { it.value.invoke() }
    private val localFactories = mutableMapOf<Any, ItemViewFactory>()

    fun reset() {
        localFactories.clear()
    }

    fun withPreprocessor(config: PreprocessorConfig, resourceProvider: ResourceProvider) {
        ItemViewFactoryFactory.factoriesOrNull(config.name, config = config.config, resourceProvider = resourceProvider)?.forEach {
            localFactories[it.typeIdentifier()] = it
        }
    }

    fun build(): ItemViewFactoryProvider {
        val factories = defaultFactories + localFactories
        if (factories.isNotEmpty()) {
            return LocalItemViewFactoryProvider(factories, defaultItemViewFactoryProvider)
        }
        return defaultItemViewFactoryProvider
    }

    companion object DefaultFactoryFactoryRegistry {
        private val defaultFactoryFactories = mutableMapOf<Any, () -> ItemViewFactory>()

        @JvmStatic
        fun registerViewFactoryFactory(type: Any, factoryFactory: () -> ItemViewFactory) {
            defaultFactoryFactories[type] = factoryFactory
        }
    }
}

/**
 * Creates one or more [ItemViewFactory] objects when lazily, using the [create] method.
 *
 * Notably this is mostly used in conjunction with preprocessor configuration of some sort, since
 * the resulting factory can present different behaviour per view/content/config.
 */
interface ItemViewFactoryProviderFactory {

    /**
     * Creates a new list of [ItemViewFactory].
     */
    fun create(resourceProvider: ResourceProvider, config: JsonObject): List<ItemViewFactory>
}

object ItemViewFactoryFactory {
    val factories = mutableMapOf<Any, ItemViewFactoryProviderFactory>()

    init {
        val gson = Gson()
        registerViewFactory("template", object : ItemViewFactoryProviderFactory {
            override fun create(resourceProvider: ResourceProvider, config: JsonObject): List<ItemViewFactory> {
                val templateConfig = gson.fromJson(config, TemplatePreprocessorConfig::class.java)
                return listOf(TemplateItemViewFactory(templateConfig.template))
            }
        })

        registerViewFactory("inPlaceTemplate", object : ItemViewFactoryProviderFactory {
            override fun create(resourceProvider: ResourceProvider, config: JsonObject): List<ItemViewFactory> {
                val elementWrapperConfig = gson.fromJson(config, InPlaceTemplatePreprocessorConfig::class.java)
                return listOf(InPlaceTemplateItemViewFactory(elementWrapperConfig.template))
            }
        })

        registerViewFactory("staticItem", object : ItemViewFactoryProviderFactory {
            override fun create(resourceProvider: ResourceProvider, config: JsonObject): List<ItemViewFactory> {
                val staticItemConfig = gson.fromJson(config, StaticItemPreprocessorConfig::class.java)
                return listOf(StaticItemViewFactory(staticItemConfig.template))
            }
        })

        registerViewFactory("fallbackTemplate", object : ItemViewFactoryProviderFactory {
            override fun create(resourceProvider: ResourceProvider, config: JsonObject): List<ItemViewFactory> {
                val fallbackConfig = gson.fromJson(config, FallbackTemplatePreprocessorConfig::class.java)
                val fullTemplateMap = fallbackConfig.getFullTemplateMap(resourceProvider)
                // TODO do not recreate every time
                return fullTemplateMap?.map { it.value }?.distinct()?.map { layout ->
                    FallbackItemViewFactory(layout, "Fallback-$layout")
                } ?: emptyList()
            }
        })
    }

    fun registerViewFactory(type: Any, factory: ItemViewFactoryProviderFactory) {
        factories[type] = factory
    }

    fun factoriesOrNull(type: Any, config: JsonObject, resourceProvider: ResourceProvider): List<ItemViewFactory>? {
        return factories[type]?.create(resourceProvider, config)
    }
}