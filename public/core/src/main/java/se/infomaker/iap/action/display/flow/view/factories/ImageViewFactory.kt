package se.infomaker.iap.action.display.flow.view.factories

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.view.View
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.action.display.flow.condition.view.ConditionalThemeableImageView
import se.infomaker.iap.action.display.flow.mustachify
import se.infomaker.iap.action.display.flow.view.FlowViewFactory
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.ThemeableImageView

object ImageViewFactory : FlowViewFactory {
    override fun create(context: Context, definition: JSONObject, valueHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme): View? {
        return definition.mustachify(valueHandler.getValueProvider()).optString("value", null)?.let { src ->
            ConditionalThemeableImageView(context).apply {
                val identifier = resourceManager.getDrawableIdentifier(src)
                definition.backgroundColor(theme)?.let {
                    setBackgroundColor(it)
                }
                if (identifier != 0) {
                    setImageDrawable(ContextCompat.getDrawable(context, identifier))
                    adjustViewBounds = true
                }
            }
        }
    }
}