package se.infomaker.iap.articleview.preprocessor.template

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.element.InPlaceTemplateItem
import se.infomaker.iap.articleview.preprocessor.select.Selector

class InPlaceTemplatePreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val inPlaceTemplatePreprocessorConfig = Gson().fromJson(config, InPlaceTemplatePreprocessorConfig::class.java)
        return process(content, inPlaceTemplatePreprocessorConfig)
    }

    private fun process(content: ContentStructure, config: InPlaceTemplatePreprocessorConfig): ContentStructure {

        val indices = Selector.getIndexes(content.body.items, config.select)
        indices.forEach {
            content.body.items[it] = InPlaceTemplateItem(
                    id = content.body.items[it].uuid,
                    template = config.template,
                    items = mapOf("defaultView" to content.body.items[it]),
                    boundViews = listOf("defaultView"))
        }
        return content
    }
}