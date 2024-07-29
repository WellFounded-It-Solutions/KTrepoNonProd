package se.infomaker.iap.articleview.requirement.itemcount

import com.google.gson.Gson
import com.google.gson.JsonObject
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.preprocessor.select.Selector
import se.infomaker.iap.articleview.requirement.RequirementValidator

class ItemCountValidator : RequirementValidator {
    override fun validate(content: ContentStructure, config: JsonObject): Boolean {
        val configuration = Gson().fromJson(config, ItemCountValidatorConfig::class.java)
        val indexes = Selector.getIndexes(content.body.items, configuration.select)
        var result = true
        if ((configuration.min != null && configuration.min > indexes.size)  ||
                (configuration.max != null && configuration.max < indexes.size) ||
                (configuration.exact != null && configuration.exact != indexes.size)){
            result = false
        }

        return result
    }
}
