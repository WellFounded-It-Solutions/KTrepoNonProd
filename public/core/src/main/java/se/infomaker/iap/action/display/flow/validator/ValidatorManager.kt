package se.infomaker.iap.action.display.flow.validator

import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.flow.mustachify

/**
 * Provides an entry point to register and access validators
 */
object ValidatorManager {
    private val validators = mutableMapOf<String, Validator>()
    private val fieldValidators = mutableMapOf<String, FieldValidator>()
    private val configurationValidators = mutableMapOf<String, ConfigurationValidator>()

    init {
        register("not-empty", NotEmptyFieldValidator)
        register("email", EmailFieldValidator)
        register("match-field", MatchFieldFieldValidator)
        register("match-value", MatchValueFieldValidator)
        register("minimum-length", MinimumLengthValidator)
    }

    fun validator(type: String, configuration: JSONObject?): Validator? {
        configurationValidators[type]?.let {
            return ValidatorConfigWrapper(it, configuration)
        }
        return validators[type]
    }

    fun fieldValidator(type: String): FieldValidator? {
        return fieldValidators[type]
    }

    fun register(name: String, validator: FieldValidator) {
        fieldValidators[name] = validator
    }

    fun register(name: String, validator: Validator) {
        validators[name] = validator
    }

    fun register(name: String, validator: ConfigurationValidator) {
        configurationValidators[name] = validator
    }
}

object MatchValueFieldValidator: FieldValidator{
    override fun validate(fieldValue: String, valueProvider: ValueProvider, configuration: JSONObject?): String? {
        configuration?.optString("value", null)?.let { value ->
            val resolvedValue = value.mustachify(valueProvider)
            return if (fieldValue == resolvedValue) null else configuration.optString("message", null) ?: "Fields does not match"
        }
        return null
    }
}

object MatchFieldFieldValidator : FieldValidator{
    override fun validate(value: String, valueProvider: ValueProvider, configuration: JSONObject?): String? {
        configuration?.optString("field", null)?.let { field ->
            return if (value == valueProvider.getString(field)) null else configuration.optString("message", null) ?: "Fields does not match"
        }
        return null
    }
}

/**
 * Wraps a validator instance with a configuration to allow passing only
 * values at validation and still use  a single instance validator
 */
private class ValidatorConfigWrapper(val validator: ConfigurationValidator, val configuration: JSONObject?) : Validator {
    override fun validate(values: JSONObject): String? {
        return validator.validate(values, configuration?: JSONObject())
    }
}