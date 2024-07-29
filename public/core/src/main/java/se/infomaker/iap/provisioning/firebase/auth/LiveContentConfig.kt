package se.infomaker.iap.provisioning.firebase.auth

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

class LiveContentConfigDeserializer : JsonDeserializer<OpenContentUrlWrapper> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OpenContentUrlWrapper {
        val liveContent = json.asJsonObject.get("liveContent")
        return Gson().fromJson(liveContent, OpenContentUrlWrapper::class.java)
    }
}

data class OpenContentUrlWrapper(@SerializedName("opencontent") val openContentUrl: String?)