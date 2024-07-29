package se.infomaker.iap.articleview.preprocessor.select.move

import se.infomaker.iap.articleview.preprocessor.select.SelectorConfig

abstract class InsertConfig {
    var fallback: InsertConfig? = null
}
class InsertIndexConfig(var position: Int) : InsertConfig()
class InsertRelativeConfig(var position: String = "before", var select: SelectorConfig = SelectorConfig()) : InsertConfig()
