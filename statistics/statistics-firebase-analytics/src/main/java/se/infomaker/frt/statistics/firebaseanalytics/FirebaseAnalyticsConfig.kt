package se.infomaker.frt.statistics.firebaseanalytics

import android.os.Bundle
import com.google.gson.Gson
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frtutilities.template.TemplateProvider
import java.io.StringWriter

data class FirebaseAnalyticsConfig(
    val sendUserId: Boolean,
    val defaultMissingValue: String?,
    val userProperties: Map<String, String>?,
    val eventTypeMapping: Map<String, EventTypeMapping>,
    val events: Map<String, EventMapping>?,
    val screenTracking: Map<String, String>?
) {
    companion object {
        @JvmStatic
        fun fromMap(config: MutableMap<String, Any>) : FirebaseAnalyticsConfig {
            Gson().let { gson ->
                val raw = gson.toJson(config)
                return gson.fromJson(raw, FirebaseAnalyticsConfig::class.java)
            }
        }
    }
}

data class EventMapping(val name: String, val mapping:Map<String, String>?) {

    companion object {
        private const val FB_EVENT_PARAM_LIMIT = 100
        private const val DELIMITER = ","

        fun safeString(source:String):String {
            if (source.length > FB_EVENT_PARAM_LIMIT) {
                if (source.lastIndexOf(DELIMITER) != -1) {
                    return safeDelimitedString(source.substring(0))
                }
                return source.substring(0, FB_EVENT_PARAM_LIMIT)
            }
            return source
        }

        private fun safeDelimitedString(source:String):String {
            val lastIndex = source.lastIndexOf(DELIMITER)
            if (lastIndex > FB_EVENT_PARAM_LIMIT) {
                return safeDelimitedString(source.substring(0, lastIndex))
            }
            return if (lastIndex == -1) if (source.length > FB_EVENT_PARAM_LIMIT) source.substring(0, FB_EVENT_PARAM_LIMIT) else source else source.substring(0, lastIndex)
        }
    }

    fun mapBundle(event: StatisticsEvent, defaultMissingValue: String?) : Bundle {
        return Bundle().also {
            mapping?.forEach { (key, template) ->
                it.putString(key, resolve(template, defaultMissingValue ?: "", event.attributes))
            }
        }
    }

    internal fun resolve(template: String, defaultMissingValue: String, attributes: Map<String, Any?>) : String? {
        val writer = StringWriter()
        TemplateProvider.getMustache(template, defaultMissingValue).execute(attributes, writer)
        return safeString(writer.toString())
    }
}