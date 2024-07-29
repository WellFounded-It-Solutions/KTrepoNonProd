package se.infomaker.iap.action.display.flow.view

import android.content.Context
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayout
import com.navigaglobal.mobile.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.action.ActionManager
import se.infomaker.iap.action.createOperation
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.action.display.flow.ValidationResult
import se.infomaker.iap.action.display.flow.condition.ConditionFactory
import se.infomaker.iap.action.display.flow.condition.view.ConditionalView
import se.infomaker.iap.action.display.flow.view.factories.ButtonViewFactory
import se.infomaker.iap.action.display.flow.view.factories.CheckboxViewFactory
import se.infomaker.iap.action.display.flow.view.factories.GroupViewFactory
import se.infomaker.iap.action.display.flow.view.factories.ImageViewFactory
import se.infomaker.iap.action.display.flow.view.factories.InputFieldViewFactory
import se.infomaker.iap.action.display.flow.view.factories.TextViewFactory
import se.infomaker.iap.action.presentConfirm
import se.infomaker.iap.action.presentMessageDialog
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.util.UI
import timber.log.Timber

object FlowViewManager : LifecycleAwareFlowViewFactory, LifecycleObserver {
    private val garbage = CompositeDisposable()
    private val conditionFactory = ConditionFactory()
    private val factories = mutableMapOf(
            "text" to TextViewFactory,
            "image" to ImageViewFactory,
            "input" to InputFieldViewFactory,
            "button" to ButtonViewFactory,
            "group" to GroupViewFactory,
            "checkbox" to CheckboxViewFactory
    )

    /**
     * Register a flow view type factory
     */
    fun registerTypeFactory(type: String, factory: FlowViewFactory) {
        factories[type] = factory
    }

    /**
     * Create a view from configuration
     */
    override fun create(context: Context, definition: JSONObject, flowStepHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme, lifecycle: Lifecycle?): View? {
        if (!factories.contains(definition.type())) {
            Timber.e("Unsupported type ${definition.type()}")
            return null
        }

        val factory = factories[definition.type()]
        val view = (factory as? LifecycleAwareFlowViewFactory)?.create(context, definition, flowStepHandler, resourceManager, theme, lifecycle)
                ?: factory?.create(context, definition, flowStepHandler, resourceManager, theme)
                ?: return null

        lifecycle?.addObserver(this)

        // Tag view with id to allow findViewByTag
        definition.id()?.let { id ->
            view.setTag(R.id.flowViewId, id)
        }

        // Prepare layout definition
        val width = definition.width()
        val height = definition.height()
        val layoutParams = FlexboxLayout.LayoutParams(width, height)

        if (definition.has("margin")) {
            layoutParams.setMargin(definition)
        }
        if (definition.has("padding")) {
            view.setPadding(definition)
        }
        definition.align()?.let {
            layoutParams.alignSelf = when (it) {
                "start" -> AlignItems.FLEX_START
                "center" -> AlignItems.CENTER
                "end" -> AlignItems.FLEX_END
                else -> AlignItems.CENTER
            }
        }
        definition.optJSONObject("onClick")?.let { onClick ->
            // TODO Observable operation to avoid having to create another one just when we want to handle it.
            val operation = onClick.createOperation(flowStepHandler)
            val canPerform = ActionManager.canPerform(context, operation)
            ActionManager.observeCanPerform(context, operation)?.let {
                garbage.add(it.subscribeOn(Schedulers.io())
                        .startWith(canPerform)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ canPerform ->
                            updateClickState(view, canPerform, onClick, flowStepHandler)
                        }, {
                            Timber.e("Could not determine if view should be clickable.")
                        }))
            } ?: run {
                updateClickState(view, canPerform, onClick, flowStepHandler)
            }
        }

        definition.showIf()?.let {
            if (view is ConditionalView) {
                conditionFactory.create(it)?.let { condition ->
                    view.condition = condition
                }
            }
        }

        view.layoutParams = layoutParams
        return view
    }

    private fun updateClickState(view: View, enableClick: Boolean, onClick: JSONObject, flowStepHandler: FlowStepHandler) {
        if (enableClick) {
            view.isEnabled = true
            view.setOnClickListener {
                val proceed = Runnable {
                    // Validate views if required
                    if (onClick.optBoolean("validateViews", false) && !flowStepHandler.validateViews()) {
                        return@Runnable
                    }
                    val garbage = CompositeDisposable()
                    garbage.add(flowStepHandler.validate().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ validationResult: ValidationResult ->
                                garbage.clear()
                                if (validationResult.success) {
                                    val operation = onClick.createOperation(flowStepHandler)
                                    operation.perform(view.context) { result ->
                                        if (!result.success) {
                                            result.errorMessage?.let {
                                                presentMessageDialog(view.context, it)
                                            }
                                        }
                                    }
                                } else {
                                    validationResult.message?.let {
                                        presentMessageDialog(view.context, it)
                                    }
                                }
                            }, { error ->
                                Timber.e(error, "Failed to validate")
                            }))
                }

                val confirm = onClick.optString("confirm", null)
                if (!TextUtils.isEmpty(confirm)) {
                    presentConfirm(view.context, confirm, proceed)
                } else {
                    proceed.run()
                }
            }
        }
        else {
            view.isEnabled = false
            view.setOnClickListener(null)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun cleanUp() {
        garbage.clear()
    }
}

private fun FlexboxLayout.LayoutParams.setMargin(definition: JSONObject) {
    definition.optJSONObject("margin")?.let { margin ->
        leftMargin = margin.pxToDp("left") ?: leftMargin
        topMargin = margin.pxToDp("top") ?: topMargin
        rightMargin = margin.pxToDp("right") ?: rightMargin
        bottomMargin = margin.pxToDp("bottom") ?: bottomMargin
        return
    }
    definition.optString("margin", null)?.let {
        definition.pxToDp("margin")?.let { margin ->
            setMargins(margin, margin, margin, margin)
        }
    }
}

fun View.setPadding(definition: JSONObject) {
    definition.optJSONObject("padding")?.let { padding ->
        setPadding(
                padding.pxToDp("left") ?: paddingLeft,
                padding.pxToDp("top") ?: paddingTop,
                padding.pxToDp("right") ?: paddingRight,
                padding.pxToDp("bottom") ?: paddingBottom
        )
        return
    }
    definition.optString("padding", null)?.let {
        definition.pxToDp("padding")?.let { padding ->
            setPadding(padding, padding, padding, padding)
        }
    }
}

fun JSONObject.pxToDp(key: String): Int? {
    val value = optDouble(key, Double.NEGATIVE_INFINITY)
    if (value != Double.NEGATIVE_INFINITY) {
        return UI.dp2px(value.toFloat()).toInt()
    }
    return null
}

private fun JSONObject.align() = optString("align")

private fun JSONObject.width(): Int {
    val double = optDouble("width", Double.NEGATIVE_INFINITY)
    if (double != Double.NEGATIVE_INFINITY) {
        return UI.dp2px(double.toFloat()).toInt()
    }
    optString("width", null)?.let {
        return when (it) {
            "fit" -> FlexboxLayout.LayoutParams.WRAP_CONTENT
            else -> FlexboxLayout.LayoutParams.MATCH_PARENT
        }
    }
    return FlexboxLayout.LayoutParams.MATCH_PARENT
}

private fun JSONObject.height(): Int {
    val double = optDouble("height", Double.NEGATIVE_INFINITY)
    if (double != Double.NEGATIVE_INFINITY) {
        return UI.dp2px(double.toFloat()).toInt()
    }
    return FlexboxLayout.LayoutParams.WRAP_CONTENT
}

private fun JSONObject.id(): String? = optString("id")

private fun JSONObject.showIf(): JSONObject? = optJSONObject("showIf")


private fun JSONObject.type() = optString("type")