package se.infomaker.livecontentmanager.query.lcc.infocaster

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class GenericPayloadDeserializer : JsonDeserializer<GenericPayload> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): GenericPayload? {
        json?.let {
            try {
                return GenericPayload(it.asString)
            }
            catch (e: UnsupportedOperationException) {
                return GenericPayload(it.toString())
            }
        }
        return null
    }
}
