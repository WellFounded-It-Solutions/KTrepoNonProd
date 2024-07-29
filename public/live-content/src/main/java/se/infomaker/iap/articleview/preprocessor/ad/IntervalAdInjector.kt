package se.infomaker.iap.articleview.preprocessor.ad

import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.item.ad.AdItem
import se.infomaker.iap.articleview.preprocessor.select.Selector
import timber.log.Timber
import java.util.*

class IntervalAdInjector(private val adConfig: AdPreprocessorConfig,
                         private val content: ContentStructure,
                         private val intervalConfig: AdIntervalConfiguration) : AdInserter {

    private val random by lazy { Random() }

    override fun inject(): ContentStructure {
        var adConfigurations = adConfig.providerConfiguration.toMutableList()
        var counter = 0
        var adInsertionIndices = listOf<Int>()
        intervalConfig.select?.let {
            val matchingItemIndices = Selector.getIndexes(content.body.items, it)
            adInsertionIndices = calculateAdInsertionIndices(matchingItemIndices,
                    intervalConfig.interval, intervalConfig.start, intervalConfig.max)
        } ?: Timber.w("Interval selector configuration is missing.")

        if (intervalConfig.alwaysInsert && adInsertionIndices.isEmpty()) {
            content.body.items.add(content.body.items.size, AdItem(adConfig.provider,
                            adConfigurations.removeAt(random.nextInt(adConfigurations.size)), content.properties))
            return content
        }
        adInsertionIndices.forEach {
            if (adConfigurations.isEmpty()) {
                adConfigurations = adConfig.providerConfiguration.toMutableList()
            }
            content.body.items.add((it + counter++), AdItem(adConfig.provider,
                            adConfigurations.removeAt(random.nextInt(adConfigurations.size)), content.properties))
        }
        return content
    }

    private fun calculateAdInsertionIndices(matchingElementIndices: List<Int>,
                                            interval: Int,
                                            start: Int? = null,
                                            max: Int? = null): List<Int> =
            matchingElementIndices
                    .filterIndexed { index, _ -> start?.let { index >= it } ?: true }
                    .filterIndexed { index, _ -> (index + 1) % interval == 0 }
                    .map { it + 1 }
                    .filterIndexed { index, _ -> max?.let { index < it } ?: true }
}