package se.infomaker.iap.articleview.preprocessor.prayer

import com.google.gson.Gson
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.ContentViewModel
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.decorator.PaddingDecorator
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.prayer.PrayerTime
import se.infomaker.iap.articleview.item.prayer.PrayerTimesItem
import java.util.UUID

class PrayerTimesPreprocessor : Preprocessor {

    companion object {
        const val ISLAMIC_DATE = "islamicdate"
    }

    override fun process(
        content: ContentStructure,
        config: String,
        resourceProvider: ResourceProvider
    ): ContentStructure {

        val prayerTimesPreprocessorConfig =
            Gson().fromJson(config, PrayerTimesPreprocessorConfig::class.java)

        val prayerTimes = mutableListOf<PrayerTime>()
        val today = LocalDate.now()
        content.body.items
            .filterIsInstance<ElementItem>()
            .forEach { item ->
                when (item.typeWithoutPrefix) {
                    ISLAMIC_DATE -> { }
                    else -> {
                        prayerTimes.add(
                            PrayerTime(
                                LocalDateTime.parse("${today}T${item.text}"),
                                item.typeWithoutPrefix
                            )
                        )
                    }
                }
            }

        val prayerTimeItem = PrayerTimesItem(
            UUID.randomUUID().toString(),
            prayerTimesPreprocessorConfig.locations,
            prayerTimes = prayerTimes
        ).apply {
            decorators.add(PaddingDecorator(themeKeys, themeKeys, themeKeys, themeKeys))
        }

        return ContentStructure(
            body = ContentViewModel(mutableListOf(prayerTimeItem)),
            properties = JSONObject(content.properties.toString())
        )
    }
}

private val ElementItem.typeWithoutPrefix
    get() = prefix?.let { type.replace(it, "").decapitalize() } ?: type