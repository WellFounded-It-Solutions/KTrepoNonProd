package se.infomaker.iap.articleview.preprocessor.template

import org.json.JSONObject
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.template.TemplateInjectorItem
import se.infomaker.iap.articleview.preprocessor.select.move.Insert
import se.infomaker.iap.articleview.preprocessor.select.move.Insert.Companion.getInsertPosition
import se.infomaker.livecontentmanager.parser.PropertyObject
import java.util.UUID

class TemplateInjectorPreprocessor : Preprocessor {

    private val gson by lazy { Insert.registerInsertTypeAdapter().create() }

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val templateInjectorConfig = gson.fromJson(config, TemplateInjectorPreprocessorConfig::class.java)
        return content.apply {
            val item = createItem(content.properties, templateInjectorConfig)
            templateInjectorConfig.insert?.let {
                val insertPosition = body.items.getInsertPosition(emptyList(), it)
                body.items.add(insertPosition, item)
            } ?: run {
                body.items.add(item)
            }
        }
    }

    private fun createItem(properties: JSONObject, templateInjectorConfig: TemplateInjectorPreprocessorConfig): TemplateInjectorItem {
        val contentId = properties.optJSONArray("contentId")?.let { it[0] as? String }
        val id = contentId ?: "generated" + UUID.randomUUID().toString()
        return TemplateInjectorItem(PropertyObject(id = id, properties = properties), templateInjectorConfig.template, templateInjectorConfig.selectorType)
    }
}