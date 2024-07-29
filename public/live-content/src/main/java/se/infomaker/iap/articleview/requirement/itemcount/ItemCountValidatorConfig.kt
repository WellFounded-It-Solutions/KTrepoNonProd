package se.infomaker.iap.articleview.requirement.itemcount

import se.infomaker.iap.articleview.preprocessor.select.SelectorConfig

data class ItemCountValidatorConfig(val min: Int?, val max: Int?, val exact: Int?, val select: SelectorConfig)