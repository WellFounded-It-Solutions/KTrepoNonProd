package se.infomaker.profile.data

import com.google.gson.*
import org.json.JSONException
import org.json.JSONObject
import se.infomaker.frtutilities.gson.typeadapters.RuntimeTypeAdapterFactory
import timber.log.Timber
import java.lang.reflect.Type

object ConfigLoaderUtil {
    /**
     * Gson supporting Item subtypes
     *
     * @return
     */
    @JvmStatic
    val customGson: Gson

    init {
        val typeFactory = RuntimeTypeAdapterFactory
            .of(ProfileItemConfig::class.java, "type")
            .registerSubtype(AuthenticationItemConfig::class.java, "authentication")
            .registerSubtype(UserItemConfig::class.java, "user")
            .registerSubtype(SettingsItemConfig::class.java, "settings")
            .registerSubtype(VersionItemConfig::class.java, "version")
            .registerSubtype(LicenseItemConfig::class.java, "licenses")
            .registerSubtype(LinkItemConfig::class.java, "link")
            .registerSubtype(AppLinkItemConfig::class.java, "appLink")
            .registerSubtype(TextItemConfig::class.java, "text")
            .registerSubtype(MailItemConfig::class.java, "mail")
            .registerSubtype(ConsentItemConfig::class.java, "consent")
            .registerSubtype(ActionItemConfig::class.java, "action")
            .registerSubtype(HtmlItemConfig::class.java, "html")

        customGson = GsonBuilder()
            .setLenient()
            .registerTypeAdapterFactory(typeFactory)
            .registerTypeAdapter(JSONObject::class.java, JSONObjectTypeAdapter)
            .create()
    }
}

object JSONObjectTypeAdapter : JsonDeserializer<JSONObject> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): JSONObject {
        json?.let {
            try {
                return JSONObject(it.toString())
            }
            catch (e: JSONException) {
                Timber.e(e, "Could not resolve JSON from: %s", it)
            }
        }
        return JSONObject()
    }
}