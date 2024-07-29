package se.infomaker.iap.articleview.item.static

import se.infomaker.iap.articleview.requirement.RequirementDefinition

data class StaticItemPreprocessorConfig(
    val selectorType: String?,
    val id: String?,
    val template: String,
    val require: List<RequirementDefinition>?
)