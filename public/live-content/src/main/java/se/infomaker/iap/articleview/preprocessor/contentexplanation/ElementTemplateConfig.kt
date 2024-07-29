package se.infomaker.iap.articleview.preprocessor.contentexplanation

import com.google.gson.JsonElement
import se.infomaker.iap.articleview.requirement.RequirementDefinition

data class ElementTemplateConfig(val keyTemplate: String?, val templateMap: JsonElement?, val template: String?, val type: String?, val require: List<RequirementDefinition>?)