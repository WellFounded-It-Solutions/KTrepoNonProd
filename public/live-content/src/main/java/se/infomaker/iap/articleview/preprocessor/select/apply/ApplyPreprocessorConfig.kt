package se.infomaker.iap.articleview.preprocessor.select.apply

import se.infomaker.iap.articleview.preprocessor.PreprocessorConfig
import se.infomaker.iap.articleview.preprocessor.select.SelectorConfig

class ApplyConfig(var select: SelectorConfig, var preprocessors: List<PreprocessorConfig> = mutableListOf())
