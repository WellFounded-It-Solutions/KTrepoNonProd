package se.infomaker.utilities

import android.content.Context
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import se.infomaker.frt.moduleinterface.action.GlobalActionHandler
import se.infomaker.frtutilities.GlobalValueManager
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Operation.Companion.create
import timber.log.Timber


class ActionHelper {

    companion object {
        const val ACTION = "action"
        const val MODULE_ID = "moduleId"
        const val MODULE_NAME = "moduleName"
        const val TITLE = "title"
        const val PARAMETERS = "parameters"
        const val OPEN_MODULE = "openModule"
    }

    fun buildModuleOperation(
        context: Context,
        action: String? = null,
        moduleId: String?,
        moduleName: String?,
        title: String?,
    ): Operation? = buildOperation(
        context = context,
        action = action ?: OPEN_MODULE,
        moduleId = moduleId,
        moduleName = moduleName,
        title = title ?: ""
    )

    fun buildModuleOperationFromJson(context: Context, action: String, parameters: JSONObject?, ): Operation? {
        return try {
            val jsonOperation = JsonObject()
            jsonOperation.addProperty(ACTION, action)
            jsonOperation.addProperty(MODULE_ID, JSONUtil.optString(parameters, "${PARAMETERS}.${MODULE_ID}")?.let { if (it.isNotEmpty()) it else null })
            jsonOperation.add(PARAMETERS, JSONUtil.toJsonObject(JSONUtil.optJSONObject(parameters, "parameters") ?: JSONObject()))
            return jsonOperation.create(action, GlobalValueManager.getGlobalValueManager(context))
        } catch (e: JSONException) {
            Timber.e(e, "Failed to create operation")
            null
        }
    }

    private fun buildOperation(
        context: Context,
        action: String,
        moduleId: String?,
        moduleName: String?,
        title: String,
    ): Operation? {
        return try {
            val jsonObject = JSONObject()
            jsonObject.put(ACTION, action)
            jsonObject.put(MODULE_ID, moduleId)
            jsonObject.put(MODULE_NAME, moduleName)
            jsonObject.put(TITLE, title)
            Operation(
                action,
                moduleId,
                jsonObject,
                GlobalValueManager.getGlobalValueManager(context)
            )
        } catch (e: JSONException) {
            Timber.e(e, "Failed to create operation")
            null
        }
    }

    fun executeOperation(context: Context, operation: Operation) {
        GlobalActionHandler.getInstance().perform(context, operation)
    }
}