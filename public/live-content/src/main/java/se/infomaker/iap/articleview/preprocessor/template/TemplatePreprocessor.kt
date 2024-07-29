package se.infomaker.iap.articleview.preprocessor.template

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.template.TemplateItem
import se.infomaker.iap.articleview.preprocessor.select.Selector

class TemplatePreprocessor : Preprocessor {
    private val gson = Gson()

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val coverConfig = gson.fromJson(config, TemplatePreprocessorConfig::class.java)
        val items = coverConfig.items.mapNotNull {
            Selector.getIndexes(content.body.items, it.selector).firstOrNull()?.let { index ->
                it.name to content.body.items.removeAt(index)
            }
        }.toMap()


        val id = items.values.joinToString(separator = ":") { it.uuid }
        val boundViews = coverConfig.items.map { it.name }
        content.body.items.add(TemplateItem(id, coverConfig.template, items, boundViews))
        return content
    }
}