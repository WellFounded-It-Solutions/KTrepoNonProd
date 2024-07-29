package se.infomaker.iap.articleview.preprocessor.ad

import com.google.gson.JsonObject
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.item.ad.AdItem
import java.util.Random

class DefaultAdInjector(
        private val adConfig: AdPreprocessorConfig,
        private val content: ContentStructure) : AdInserter {

    private val random by lazy { Random() }

    override fun inject(): ContentStructure {
        var adConfigurations: MutableList<JsonObject> = adConfig.providerConfiguration.toMutableList()
        repeat((0 until (adConfig.amount ?: 1)).count()) {
            if (adConfigurations.isEmpty()) {
                adConfigurations = adConfig.providerConfiguration.toMutableList()
            }
            content.body.items.add(AdItem(adConfig.provider, adConfigurations.removeAt(random.nextInt(adConfigurations.size)), content.properties))
        }
        return content
    }
}