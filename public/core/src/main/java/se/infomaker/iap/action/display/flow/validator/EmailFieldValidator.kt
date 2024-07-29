package se.infomaker.iap.action.display.flow.validator

import android.util.Patterns
import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider

object EmailFieldValidator : FieldValidator {
    override fun validate(value: String, valueProvider: ValueProvider, configuration: JSONObject?): String? =
            if (Patterns.EMAIL_ADDRESS.matcher(value).matches()) null else configuration?.optString("message", null) ?: "Invalid email address"
}