package se.infomaker.iap.action.display

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.google.gson.JsonObject
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.action.Operation
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager
import timber.log.Timber


/**
 * Gets the title of the operation or empty string if not present
 */
fun JSONObject.title(): String = optString("title")

fun Operation.configuration(resourceManager: ResourceManager): JSONObject {
    val configuration = parameters.optJSONObject("configuration")
    if (configuration != null) {
        return configuration
    }
    val configFile = parameters.optString("configuration", null)
    if (configFile != null) {
        try {
            val asset = resourceManager.getAsset("configuration/$configFile", JsonObject::class.java)
            // Make this better baby
            return JSONObject(asset.toString())
        } catch (e: Throwable) {
            Timber.e(e, "Failed to parse configuration")
        }
    }
    return JSONObject()
}

/**
 * Toolbar type to use
 * solid | transparent | none
 */
fun JSONObject.toolbar(): String = optString("toolbar", null) ?: "solid"


fun Operation.resourceManager(context: Context): ResourceManager = ResourceManager(context, moduleID)

fun Operation.theme(context: Context): Theme = ThemeManager.getInstance(context).getModuleTheme(moduleID)

/**
 * Determine if the operation should allow back
 */
fun JSONObject.allowBack(): Boolean = optBoolean("allowBack", true)
