package se.infomaker.frt.statistics.googleanalytics

import com.google.gson.Gson
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frtutilities.template.TemplateProvider
import timber.log.Timber
import java.io.StringWriter

data class GAConfig(val trackingId:String?, val commonMapping:List<Mapping>?, val mapping:Map<String, List<Mapping>>?, val filter:Map<String, List<Filter>>?, val events:Map<String,LegacyMapping>?) {
    companion object {
        @JvmStatic
        fun fromMap(config: MutableMap<String, Any>) : GAConfig{
            Gson().let { gson ->
                val raw = gson.toJson(config)
                return gson.fromJson(raw, GAConfig::class.java)
            }
        }
    }
}

data class Filter(val op: String?, val key: String?, val value: String?) {
    fun evaluate(event: StatisticsEvent): Boolean {
        return when(op) {
            "equal" -> {
                event.attributes[key] == value
            }
            else -> {
                Timber.w("Invalid operator $op")
                true
            }
        }
    }
}

data class LegacyMapping(val eventMapping:Map<String, String>?, val cdMapping:List<String>?)

data class Mapping(val variable:String, val template:String) {
    fun resolveTemplate(event: StatisticsEvent) : String {
        val writer = StringWriter()
        TemplateProvider.getMustache(template, "").execute(event.attributes, writer)
        return writer.toString()
    }
}