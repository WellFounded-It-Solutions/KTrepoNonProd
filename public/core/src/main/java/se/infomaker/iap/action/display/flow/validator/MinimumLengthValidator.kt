package se.infomaker.iap.action.display.flow.validator

import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider

object MinimumLengthValidator : FieldValidator {
    override fun validate(value: String, valueProvider: ValueProvider, configuration: JSONObject?): String? {
        val length = configuration.length()
        val message = configuration.message()

        return if (value.length < length) message else null
    }

    private fun JSONObject?.length(): Int {
        return this?.optInt("length", 0) ?: 0
    }

    private fun JSONObject?.message(): String {
        return this?.optString("message", null) ?: "Field must contain more characters"
    }
}