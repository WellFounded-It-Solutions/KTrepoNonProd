package se.infomaker.iap.articleview.preprocessor.ad

import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import timber.log.Timber

class AdPreprocessor : Preprocessor {

    override fun process(content: ContentStructure,
                         config: String,
                         resourceProvider: ResourceProvider): ContentStructure {
        val adConfig = Gson().fromJson(config, AdPreprocessorConfig::class.java)
        if (adConfig.providerConfiguration.isEmpty()) {
            return content
        }

        adConfig?.insertStrategy?.let { insertionStrategy ->
            return when (insertionStrategy.strategy) {
                Strategy.INTERVAL -> {
                    insertionStrategy.config?.let { intervalConfig ->
                        Gson().fromJson(intervalConfig, AdIntervalConfiguration::class.java).let {
                            return IntervalAdInjector(adConfig, content, it).inject()
                        }
                    }
                    Timber.w("No ad insertion interval configuration provided.")
                    content
                }
                else -> DefaultAdInjector(adConfig, content).inject()
            }
        } ?: return DefaultAdInjector(adConfig, content).inject()
    }
}

interface AdInserter {
    fun inject(): ContentStructure
}