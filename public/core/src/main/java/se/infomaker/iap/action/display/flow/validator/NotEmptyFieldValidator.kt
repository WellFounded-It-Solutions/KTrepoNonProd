package se.infomaker.iap.action.display.flow.validator

import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider

object NotEmptyFieldValidator : FieldValidator {
    override fun validate(value: String, valueProvider: ValueProvider, configuration: JSONObject?): String? {
        val message = configuration?.optString("message", null) ?: "Field should not be empty"
        return if (value.isEmpty()) message else null
    }
}