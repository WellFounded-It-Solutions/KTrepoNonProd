package se.infomaker.livecontentmanager.model

import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentmanager.stream.EventType

data class StreamEventWrapper(var event: EventType, var objects: List<PropertyObject>) {
    fun isNotEmpty() : Boolean {
        return objects.isNotEmpty()
    }
}