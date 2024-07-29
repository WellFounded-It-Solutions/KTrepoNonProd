package se.infomaker.iap.action.display.flow.view.factories

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.action.display.actionLinkify
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.action.display.flow.condition.view.ConditionalThemeableTextView
import se.infomaker.iap.action.display.flow.mustachify
import se.infomaker.iap.action.display.flow.view.FlowViewFactory
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.ThemeableTextView

object TextViewFactory : FlowViewFactory {
    override fun create(context: Context, definition: JSONObject, valueHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme): View? {

        val definition = definition.mustachify(valueHandler.getValueProvider())
        return definition.optString("value", null)?.let {
            ConditionalThemeableTextView(context).apply {
                theme.getColor("link", null)?.let { setLinkTextColor(it.get()) }
                text = it.actionLinkify(context, resourceManager.moduleIdentifier, valueHandler.getValueProvider())
                movementMethod = LinkMovementMethod.getInstance()
                gravity = definition.textAlign()

                definition.optString("themeKey", null)?.let { themeKey ->
                    themeKeys = listOf(themeKey)
                }
            }
        }
    }
}

private fun JSONObject.textAlign(): Int {
    return when (optString("textAlign", null)) {
        "start" -> Gravity.START
        "end" -> Gravity.END
        else -> Gravity.CENTER_HORIZONTAL
    }
}
