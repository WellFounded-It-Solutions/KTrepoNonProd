package se.infomaker.iap.articleview.item.static

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.preprocessor.reproducibleUuid
import se.infomaker.iap.articleview.requirement.RequirementChecker

class StaticPreprocessor : Preprocessor {

    private val gson = Gson()

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val configuration = gson.fromJson(config, StaticItemPreprocessorConfig::class.java)
        if (RequirementChecker.validate(content, configuration.require)) {
            val item = StaticItem(configuration.id ?: configuration?.reproducibleUuid.toString(), configuration.template, configuration.selectorType ?: configuration.template)
            content.body.items.add(item)
        }
        return content
    }
}