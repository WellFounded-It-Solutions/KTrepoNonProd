package se.infomaker.iap.action.display.flow.condition

import org.json.JSONException
import org.json.JSONObject
import se.infomaker.iap.theme.UndefinedException
import timber.log.Timber
import java.util.*

interface ShowIfDefinition {
    val key: String?
    val operator: String?
    val value: String?
}

class ConditionFactory() {

    fun create(definition: ShowIfDefinition): Condition? = try {
        when(definition.operator?.lowercase(Locale.getDefault())){
            "notequal" -> {
                NotEqual(definition.key ?: throw UndefinedException("Key must be defined."),
                    definition.value ?: throw UndefinedException("Value must be defined."), )
            }
            else -> {
                /* Equal is the default operator */
                Equal(definition.key ?: throw UndefinedException("Key must be defined."),
                    definition.value ?: throw UndefinedException("Value must be defined."),)
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to create condition")
        null
    }

    fun create(definition: JSONObject): Condition? {
        return try {
            when(definition.optString("operator").lowercase(Locale.getDefault())){
                "notequal" -> {
                    NotEqual(definition.getString("key"), definition.getString("value"))
                }
                else -> {
                    /* Equal is the default operator */
                    Equal(definition.getString("key"), definition.getString("value"))
                }
            }
        } catch (e: JSONException) {
            Timber.e(e, "Failed to create condition")
            null
        }
    }
}