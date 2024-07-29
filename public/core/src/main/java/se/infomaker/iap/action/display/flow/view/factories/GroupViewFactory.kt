package se.infomaker.iap.action.display.flow.view.factories

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import org.json.JSONArray
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.action.display.flow.condition.Condition
import se.infomaker.iap.action.display.flow.condition.view.ConditionalView
import se.infomaker.iap.action.display.flow.view.FlowViewFactory
import se.infomaker.iap.action.display.flow.view.FlowViewManager
import se.infomaker.iap.action.display.flow.view.LifecycleAwareFlowViewFactory
import se.infomaker.iap.action.display.flow.view.ValueView
import se.infomaker.iap.theme.Theme

object GroupViewFactory : LifecycleAwareFlowViewFactory {
    override fun create(context: Context, definition: JSONObject, flowStepHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme, lifecycle: Lifecycle?): View? {
        return GroupView(context, definition, flowStepHandler, resourceManager, theme, lifecycle = lifecycle)
    }
}

class GroupView @JvmOverloads constructor(context: Context, definition: JSONObject, valueHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme, override var condition: Condition? = null, lifecycle: Lifecycle? = null) : FlexboxLayout(context), ConditionalView {
    private val valueViews = mutableMapOf<String, ValueView>()
    private val clickableViews = mutableMapOf<String, View>()

    fun getValueViews(): Map<String, ValueView> {
        return valueViews
    }

    fun getClickableViews(): Map<String, View> {
        return clickableViews
    }

    init {
        flexDirection = definition.direction()
        flexWrap = FlexWrap.NOWRAP
        justifyContent = definition.justify()

        definition.backgroundColor(theme)?.let {
            setBackgroundColor(it)
        }

        definition.views()?.let { views ->
            (0..views.length())
                    .mapNotNull { views.optJSONObject(it) }
                    .forEach { definition ->
                        FlowViewManager.create(context, definition, valueHandler, resourceManager, theme, lifecycle)?.let { view ->
                            addView(view)
                            definition.optString("id", null)?.let {
                                if (view is ValueView) {
                                    valueViews[it] = view
                                }
                                clickableViews.put(it, view)
                            }
                            if (view is GroupView) {
                                valueViews.putAll(view.getValueViews())
                                clickableViews.putAll(view.getClickableViews())
                            }
                            return@forEach
                        }
                    }
        }
    }
}

private fun JSONObject.justify(): Int = when (optString("justify", null)) {
    "end" -> JustifyContent.FLEX_END
    "center" -> JustifyContent.CENTER
    "spaceBetween" -> JustifyContent.SPACE_BETWEEN
    "spaceAround" -> JustifyContent.SPACE_AROUND
    else -> JustifyContent.FLEX_START
}

private fun JSONObject.views(): JSONArray? = optJSONArray("views")

fun JSONObject.backgroundColor(theme: Theme): Int? =
        optString("backgroundColor", null)?.let {
            theme.getColor(it, null)?.get()
        }

private fun JSONObject.direction(): Int = when (optString("direction", null)) {
    "vertical" -> FlexDirection.COLUMN
    else -> FlexDirection.ROW
}
