package se.infomaker.iap.action.display.flow.view.factories

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.actionLinkify
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.action.display.flow.condition.Condition
import se.infomaker.iap.action.display.flow.mustachify
import se.infomaker.iap.action.display.flow.validator.validate
import se.infomaker.iap.action.display.flow.condition.view.ConditionalView
import se.infomaker.iap.action.display.flow.view.FlowViewFactory
import se.infomaker.iap.action.display.flow.view.ValueView
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.ThemeableCheckbox

object CheckboxViewFactory : FlowViewFactory {
    override fun create(context: Context, definition: JSONObject, valueHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme): View? {

        return InputCheckbox(context, definition).
                apply {
                    theme.getColor("link", null)?.let { setLinkTextColor(it.get()) }
                    text = definition.text().mustachify(valueHandler.getValueProvider()).actionLinkify(context, resourceManager.moduleIdentifier, valueHandler.getValueProvider())
                    movementMethod = LinkMovementMethod.getInstance()
                    definition.themeKey()?.let { themeKey ->
                        themeKeys = listOf(themeKey)
                    }
                    isChecked = definition.value()
                }
    }
}

private class InputCheckbox(context: Context, val definition: JSONObject, override var condition: Condition? = null) : ThemeableCheckbox(context), ValueView, ConditionalView {
    override fun setValue(value: String) {
        isChecked = value.toLowerCase() == "true"
    }

    override fun getValue(): String {
        return if (isChecked) "true" else "false"
    }

    override fun validate(valueProvider: ValueProvider): Boolean {
        error = validate(valueProvider, definition.optJSONArray(("validators")))
        return error == null
    }
}

private fun JSONObject.value() = optBoolean("value", false)
private fun JSONObject.themeKey() = optString("themeKey")
private fun JSONObject.text() = optString("text")
