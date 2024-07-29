package se.infomaker.iap.action.display.tabs

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.Operation
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class TabsConfig : Serializable {
    var tabs: List<TabOperation>? = null
    var title: String? = null

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        out.writeObject(Gson().toJson(this))
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        Gson().fromJson(`in`.readObject() as String, TabsConfig::class.java).also {
            tabs = it.tabs
            title = it.title
        }
    }
}

//TODO: What to do about the nullability of moduleName?
class TabOperation(
        val title: String?,
        val action: String,
        val moduleID: String?,
        parameters: JsonObject,
        val defaultSelected: Boolean = false) {

    val parameters: JsonObject = parameters.apply {
        if (moduleID != null && !this.has("moduleId")) {
            addProperty("moduleId", moduleID)
        }
    }

    fun asOperation(valueProvider: ValueProvider): Operation = Operation(
            action = action,
            moduleID = moduleID ?: parameters.getAsJsonPrimitive("moduleId")?.asString,
            parameters = JSONObject(parameters.toString()),
            values = valueProvider
    )
}