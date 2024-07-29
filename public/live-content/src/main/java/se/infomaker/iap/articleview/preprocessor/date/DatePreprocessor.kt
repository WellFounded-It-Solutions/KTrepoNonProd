package se.infomaker.iap.articleview.preprocessor.date

import android.content.Context
import android.text.SpannableStringBuilder
import com.google.gson.Gson
import se.infomaker.frtutilities.DateUtil
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.element.ElementItem
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID


/**
 * @param spannableStringBuilder This is used for unit testing purposes, where Android library is stubbed out
 */
class DatePreprocessor(val context: Context?,
                       val spannableStringBuilder: (text: String) -> SpannableStringBuilder = { SpannableStringBuilder(it) }) : Preprocessor {
    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val dateConfig = Gson().fromJson(config, DatePreprocessorConfig::class.java)

        val inDateTimeFormat: DateTimeFormatter?
        val outDateTimeFormat: DateTimeFormatter?
        if (dateConfig.outputFormat != null) {
            inDateTimeFormat = DateTimeFormatter.ofPattern(dateConfig.inputFormat)
            outDateTimeFormat = DateTimeFormatter.ofPattern(dateConfig.outputFormat)
        } else {
            inDateTimeFormat = null
            outDateTimeFormat = null
        }

        dateConfig.properties.distinct().forEach { property ->
            content.properties.optJSONArray(property)?.let { jsonArray ->
                (0 until jsonArray.length()).map { jsonArray.getString(it) }
                        .forEachIndexed { index, value ->
                            val output = if (inDateTimeFormat != null && outDateTimeFormat != null) {
                                val zonedDateTime = ZonedDateTime.parse(value, inDateTimeFormat)

                                zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
                                        .format(outDateTimeFormat)
                            } else {
                                DateUtil.formatDateString(context, value, null)
                            }

                            content.body.items.add(ElementItem(
                                    id = UUID.nameUUIDFromBytes("$property-${dateConfig.id}-$index".toByteArray()).toString(),
                                    themeKeys = listOf("date", "element", "default"),
                                    attributes = mapOf("type" to "date"),
                                    text = spannableStringBuilder(output)))
                        }
            }
        }
        return content
    }
}