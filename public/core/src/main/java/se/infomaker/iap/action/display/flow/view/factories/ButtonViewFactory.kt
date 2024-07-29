package se.infomaker.iap.action.display.flow.view.factories

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.view.Gravity
import android.view.View
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.action.display.flow.condition.view.ConditionalThemeableButton
import se.infomaker.iap.action.display.flow.view.FlowViewFactory
import se.infomaker.iap.action.display.flow.view.pxToDp
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.util.UI
import se.infomaker.iap.theme.view.ThemeableButton

object ButtonViewFactory : FlowViewFactory {
    override fun create(context: Context, definition: JSONObject, valueHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme): View? {
        return definition.optString("value", null)?.let {
            ConditionalThemeableButton(context).apply {
                text = it
                gravity = Gravity.CENTER

                definition.themeKey()?.let {
                    themeKeys = listOf(it)
                }
                definition.backgroundColor()?.let {
                    themeBackgroundColor = it
                }

                definition.borderColor()?.let {
                    strokeWidth = UI.dp2px(1F).toInt()
                    var color = theme.getColor(it, null)?.get()
                    if (color == null) {
                        try {
                            color = Color.parseColor(it)
                        } catch (_: IllegalArgumentException) {
                        }
                    }
                    if (color != null) {
                        strokeColor = ColorStateList.valueOf(color)
                    }
                }

                definition.image()?.let { image ->
                    val source = image.src()

                    val drawable: Drawable? = theme.getImage(source, null)?.getImage(context) ?: kotlin.run {
                        val identifier = resourceManager.getDrawableIdentifier(source)
                        if (identifier != 0) {
                            return@run ContextCompat.getDrawable(context, identifier)
                        }
                        return@run null
                    }

                    drawable?.let { drawable ->
                        theme.getText(themeKeys, null)?.getColor(theme)?.let { tint ->
                            drawable.mutate().setColorFilter(tint.get(), PorterDuff.Mode.SRC_IN)
                        }

                        image.pxToDp("padding")?.let {
                            compoundDrawablePadding = it
                        }
                        placeImage(image, drawable)
                    }
                }

                setRoundedCorners(true)
            }
        }
    }
}

private fun JSONObject.borderColor(): String? = optString("borderColor", null)

private fun ThemeableButton.placeImage(config: JSONObject, drawable: Drawable) {
    when (config.optString("position", null)) {
        "start" -> {
            setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }
        "center" -> {
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }
        "end" -> {
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }
        "top" -> {
            setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
        }
        "bottom" -> {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)
        }
        else -> { //end
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }
    }
}

private fun JSONObject.src(): String? = optString("src", null)
private fun JSONObject.image(): JSONObject? = optJSONObject("image")
private fun JSONObject.themeKey() = optString("themeKey", null)
private fun JSONObject.backgroundColor() = optString("backgroundColor", optString("themeKey", null))