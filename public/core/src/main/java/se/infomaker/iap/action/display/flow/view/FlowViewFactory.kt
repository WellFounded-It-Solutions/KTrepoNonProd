package se.infomaker.iap.action.display.flow.view

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.theme.Theme

/**
 * Factory that creates views to be used in a Flow.
 */
interface FlowViewFactory {
    /**
     * Creates a view from definition
     *
     * @param context
     * @param definition definition of view
     * @param flowStepHandler can extract and update values
     * @return a view or null if definition is invalid
     */
    fun create(context: Context, definition: JSONObject, flowStepHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme): View?
}

/**
 * Factory that creates views to be used in a Flow, that can handle a [Lifecycle] to properly clean
 * up after itself. 
 */
interface LifecycleAwareFlowViewFactory : FlowViewFactory {

    /**
     * Creates a view based on a given [JSONObject] definition.
     *
     * Extends the functionality of [FlowViewFactory.create] by accepting a [Lifecycle] when
     * creating view.
     * The lifecycle can be used by this factory to properly clean up subscriptions or observations
     * to avoid leaking them and all of the created views [Context].
     */
    fun create(context: Context, definition: JSONObject, flowStepHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme, lifecycle: Lifecycle?): View?

    /**
     * Convenience override to allow callers to only override lifecycle aware method.
     */
    override fun create(context: Context, definition: JSONObject, flowStepHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme): View? {
        return create(context, definition, flowStepHandler, resourceManager, theme, null)
    }
}