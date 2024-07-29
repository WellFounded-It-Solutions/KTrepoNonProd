package se.infomaker.livecontentui.config

import se.infomaker.iap.articleview.preprocessor.PreprocessorConfig

class NativeContentViewConfig : LiveContentUIConfig() {
    var preprocessors: List<PreprocessorConfig>? = null
}