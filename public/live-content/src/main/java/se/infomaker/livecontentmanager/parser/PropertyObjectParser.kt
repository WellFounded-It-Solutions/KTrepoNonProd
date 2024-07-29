package se.infomaker.livecontentmanager.parser

import org.json.JSONObject
import se.infomaker.livecontentmanager.model.StreamEventWrapper

interface PropertyObjectParser {
    fun getAllIds(from: List<PropertyObject>, type: String) : Set<String>
    fun fromSearch(response: JSONObject, type: String?) : List<PropertyObject>
    fun fromStreamNotification(event: JSONObject?, type: String?) : StreamEventWrapper
}