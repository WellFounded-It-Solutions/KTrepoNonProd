package se.infomaker.iap.articleview.item.livecontent

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.PropertyObjectPreprocessor
import se.infomaker.iap.articleview.PropertyObjectPreprocessorConfig
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.item.ItemViewFactoryFactory
import se.infomaker.iap.articleview.item.ItemViewFactoryProviderBuilder
import se.infomaker.iap.articleview.item.ItemViewFactoryProviderFactory
import se.infomaker.iap.articleview.item.ad.AdItemViewFactory
import se.infomaker.iap.articleview.item.ad.AdViewProvider
import se.infomaker.iap.articleview.item.template.TemplateInjectorItemViewFactory
import se.infomaker.iap.articleview.item.template.TemplateInjectorItemViewFactoryConfig
import se.infomaker.iap.articleview.preprocessor.PreprocessorManager
import se.infomaker.iap.articleview.preprocessor.ad.AdPreprocessor
import se.infomaker.iap.articleview.preprocessor.propertyelement.PropertyElementPreprocessor
import se.infomaker.iap.articleview.preprocessor.template.TemplateInjectorPreprocessor
import se.infomaker.iap.articleview.view.PropertyObjectItemViewFactory

class Init : AbstractInitContentProvider() {

    override fun init(context: Context) {

        PreprocessorManager.registerPreprocessor(NAME, PropertyObjectPreprocessor())
        PreprocessorManager.registerPreprocessor(TEMPLATE_INJECTOR_NAME, TemplateInjectorPreprocessor())
        PreprocessorManager.registerPreprocessor(PROPERTY_ELEMENT_INJECTOR_NAME, PropertyElementPreprocessor())
        PreprocessorManager.registerPreprocessor(AD_INJECTOR_NAME, AdPreprocessor())

        ItemViewFactoryFactory.registerViewFactory(NAME, object : ItemViewFactoryProviderFactory {
            private val gson = Gson()
            override fun create(resourceProvider: ResourceProvider, config: JsonObject): List<ItemViewFactory> {
                val propertyObjectConfig = gson.fromJson(config, PropertyObjectPreprocessorConfig::class.java)
                return listOf(PropertyObjectItemViewFactory(propertyObjectConfig.template))
            }
        })
        ItemViewFactoryFactory.registerViewFactory(TEMPLATE_INJECTOR_NAME, object : ItemViewFactoryProviderFactory {
            private val gson by lazy { Gson() }
            override fun create(resourceProvider: ResourceProvider, config: JsonObject): List<ItemViewFactory> {
                val templateInjectorConfig = gson.fromJson(config, TemplateInjectorItemViewFactoryConfig::class.java)
                return listOf(TemplateInjectorItemViewFactory(templateInjectorConfig.template))
            }
        })

        ItemViewFactoryProviderBuilder.DefaultFactoryFactoryRegistry.registerViewFactoryFactory(AdItemViewFactory.TYPE_IDENTIFIER) {
            AdItemViewFactory(AdViewProvider())
        }
    }

    companion object {
        const val NAME = "propertyObject"
        private const val TEMPLATE_INJECTOR_NAME = "templateInjector"
        private const val PROPERTY_ELEMENT_INJECTOR_NAME = "propertyElement"
        private const val AD_INJECTOR_NAME = "ad"
    }
}