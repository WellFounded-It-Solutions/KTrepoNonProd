package se.infomaker.iap.articleview.requirement

import com.google.gson.JsonObject
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.requirement.itemcount.ItemCountValidator
import se.infomaker.iap.articleview.requirement.property.PropertyValidator

object RequirementChecker {
    private val validators = mutableMapOf<String, RequirementValidator>()
    init {
        validators["itemCount"] = ItemCountValidator()
        validators["property"] = PropertyValidator()
    }

    fun validate(content: ContentStructure, definitions: List<RequirementDefinition>?) : Boolean {
        definitions?.forEach {
            if (!validate(content, it)) {
                return false
            }
        }
        return true
    }

    fun validate(content: ContentStructure, definition: RequirementDefinition) : Boolean {
        return validators[definition.type]?.validate(content, definition.config) ?: false
    }
 }

/**
 * Validate that content is fulfilling configured requirement
 */
interface RequirementValidator {
    fun validate(content: ContentStructure, config: JsonObject): Boolean
}
