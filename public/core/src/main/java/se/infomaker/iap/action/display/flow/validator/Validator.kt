package se.infomaker.iap.action.display.flow.validator

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.flow.view.ValueView
import timber.log.Timber

interface Validator {
    /**
     * Validates the provided values
     *
     * @return an error message if error is found or null if field is valid
     */
    fun validate(values: JSONObject): String?
}

interface FieldValidator {
    /**
     * Validates a single value
     * @return an error message if the validation failed
     */
    fun validate(value: String, valueProvider: ValueProvider, configuration: JSONObject?): String?
}

fun ValueView.validate(valueProvider: ValueProvider, validators: JSONArray?): String? {
    validators?.let {
        for (i in 0 until it.length()) {
            try {
                val validator = validators.getJSONObject(i)
                ValidatorManager.fieldValidator(validator.getString("type"))?.validate(getValue(), valueProvider, validator.optJSONObject("configuration"))?.let {
                    return it
                }
            } catch (e: JSONException) {
                Timber.e(e, "Invalid configuration")
            }
        }
    }
    return null
}

interface ConfigurationValidator {
    /**
     * Validates the provided values using configuration
     *
     * @return an error message if error is found or null if field is valid
     */
    fun validate(values: JSONObject, configuration: JSONObject): String?
}