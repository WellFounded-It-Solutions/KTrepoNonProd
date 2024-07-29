package se.infomaker.iap.action.display.flow

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.navigaglobal.mobile.R
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONArray
import org.json.JSONObject
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.ActionManager
import se.infomaker.iap.action.ActionValueProvider
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import se.infomaker.iap.action.display.configuration
import se.infomaker.iap.action.display.flow.condition.Condition
import se.infomaker.iap.action.display.flow.condition.view.ConditionalView
import se.infomaker.iap.action.display.flow.condition.view.showIf
import se.infomaker.iap.action.display.flow.validator.Validator
import se.infomaker.iap.action.display.flow.validator.ValidatorManager
import se.infomaker.iap.action.display.flow.view.FlowViewManager
import se.infomaker.iap.action.display.flow.view.ValueView
import se.infomaker.iap.action.display.flow.view.factories.GroupView
import se.infomaker.iap.action.display.flow.view.setPadding
import se.infomaker.iap.action.display.getOperation
import se.infomaker.iap.action.flatMapped
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager

class FlowFragment : Fragment(), FlowStepHandler {
    private val garbage = CompositeDisposable()
    override fun getModuleId(): String {
        return arguments?.getOperation()?.moduleID ?: "global"
    }

    override fun getValues(): JSONObject {
        val out = JSONObject()
        valueViews.forEach { (key, provider) ->
            out.put(key, provider.getValue())
        }
        return out
    }

    override fun currentView(): String? = configuration.optString("id", null)

    override fun getValueProvider(): ValueProvider {
        return operation?.values ?: object : ValueProvider {
            override fun observeString(keyPath: String): Observable<String> = Observable.never()
            override fun getStrings(keyPath: String): List<String>? = null
            override fun getString(keyPath: String): String? = null
        }
    }

    override fun validateViews(): Boolean {
        var result = true
        val values = currentView()?.let {
            getValues().flatMapped(it)
        } ?: mapOf()
        val provider = ActionValueProvider(getValueProvider(), values)
        valueViews.forEach { (_, valueView) ->
            result = result.and(valueView.validate(provider))
        }
        return result
    }

    override fun validate(): Single<ValidationResult> {
        val validators = configuration.validators()
        if (validators.isEmpty()) {
            return ValidationResult.SINGLE_SUCCESS
        }
        return Single.defer {
            val values = getValues()
            val result = validators.mapNotNull { it.validate(values) }

            if (result.isNotEmpty()) {
                return@defer Single.just(ValidationResult(false, result.joinToString(separator = "\n")))
            }
            return@defer ValidationResult.SINGLE_SUCCESS
        }
    }

    private lateinit var resourceManager: ResourceManager
    private lateinit var configuration: JSONObject
    private lateinit var theme: Theme
    private var timeLeft: Long = 0
    private var operation: Operation? = null
    private var valueViews = mutableMapOf<String, ValueView>()
    private var clickableViews = mutableMapOf<String, View>()

    /*
     * Handles local flow-click actions
     */
    private val flowClickActionHandler = object : ActionHandler {
        override fun canPerform(context: Context, operation: Operation): Boolean = true

        override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
            operation.getParameter("viewId")?.let { viewId ->
                clickableViews[viewId]?.let {
                    if (it.isClickable) {
                        it.callOnClick()
                        onResult.invoke(Result(true, operation.values))
                    }
                } ?: kotlin.run {
                    onResult.invoke(Result(false, operation.values, "view does not exist"))
                }
            } ?: kotlin.run {
                onResult.invoke(Result(false, operation.values, "viewId, not specified"))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        operation = arguments?.getOperation()
        (operation?.moduleID ?: "").let { moduleId ->
            theme = ThemeManager.getInstance(context).getModuleTheme(moduleId)
            resourceManager = ResourceManager(activity, moduleId)
        }
        configuration = operation?.configuration(resourceManager) ?: JSONObject()
        savedInstanceState?.let {
            timeLeft = savedInstanceState.getLong("timeLeft", 0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("timeLeft", timeLeft)
        outState.putString("values", getValues().toString())
    }

    override fun onResume() {
        super.onResume()
        ActionManager.register("flow-click", flowClickActionHandler)

        if (System.currentTimeMillis() - timeLeft > 500) {
            registerStatsShow()
        }
    }

    private fun registerStatsShow() {
        if (configuration.optBoolean("registerStats", false) && configuration.optString("id", null) != null) {
            val id = configuration.getString("id")
            val builder = StatisticsEvent.Builder()
                    .viewShow()
                    .viewName("flow")
                    .attribute("viewId", id)
            operation?.moduleID?.let {moduleId ->
                builder.moduleId(moduleId)
                val moduleName = ModuleInformationManager.getInstance().getModuleName(moduleId)
                if (!TextUtils.isEmpty(moduleName)) {
                    builder.moduleName(moduleName)
                }
                val moduleTitle = ModuleInformationManager.getInstance().getModuleTitle(moduleId)
                if (!TextUtils.isEmpty(moduleTitle)) {
                    builder.moduleTitle(moduleTitle)
                }
            }
            StatisticsManager.getInstance().logEvent(builder.build())
        }
    }

    override fun onPause() {
        super.onPause()
        timeLeft = System.currentTimeMillis()
        ActionManager.unregister("flow-click", flowClickActionHandler)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.flow, container, false)
        val topContainer = view.findViewById<ViewGroup>(R.id.topContainer)
        setup(topContainer, configuration.views(), configuration.align())
        setup(view.findViewById(R.id.bottomContainer), configuration.footerViews())

        if (configuration.has("padding")) {
            topContainer.setPadding(configuration)
        }
        if (savedInstanceState != null) {
            savedInstanceState.getString("values", null)?.let { json ->
                val values = JSONObject(json)
                values.keys().forEach {
                    val value = values.optString(it)
                    valueViews[it]?.setValue(value)
                }
            }
        }
        theme.apply(view)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        garbage.clear()
    }

    private fun setup(container: ViewGroup?, views: JSONArray?, justifyContent: Int = JustifyContent.CENTER) {
        if (container == null || views == null) {
            return
        }

        (container as? FlexboxLayout)?.let { flexContainer ->
            flexContainer.justifyContent = justifyContent
            flexContainer.flexWrap = FlexWrap.NOWRAP
        }

        activity?.let { currentContext ->
            (0..views.length())
                    .mapNotNull { views.optJSONObject(it) }
                    .forEach { definition ->
                        FlowViewManager.create(currentContext, definition, this, resourceManager, theme, lifecycle)?.let { view ->
                            if (view is ConditionalView) {
                                view.condition?.let { condition ->
                                    view.showIf(getValueProvider(), condition)
                                    setupConditionWatcher(view, condition)
                                }
                            }
                            container.addView(view)
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

    private fun setupConditionWatcher(view: View, condition: Condition) {
        val sources = condition.relevantKeyPaths().map { keyPath ->
            getValueProvider().observeString(keyPath)
        }
        garbage.add(Observable.merge(sources).observeOn(AndroidSchedulers.mainThread()).subscribe {
            view.showIf(getValueProvider(), condition)
        })
    }
}

private fun JSONObject.align(): Int = optString("align", null).let {
    when (it) {
        "top" -> JustifyContent.FLEX_START
        "bottom" -> JustifyContent.FLEX_END
        "center" -> JustifyContent.CENTER
        else -> JustifyContent.FLEX_START
    }
}

private fun JSONObject.views(): JSONArray? = optJSONArray("views")
private fun JSONObject.type(): String = optString("type")
private fun JSONObject.configuration(): JSONObject? = optJSONObject("configuration")
private fun JSONObject.footerViews(): JSONArray? = optJSONArray("footerViews")
private fun JSONObject.validators(): List<Validator> {
    val definitions = optJSONArray("validators")
    if (definitions == null || definitions.length() == 0) {
        return emptyList()
    }
    val out = mutableListOf<Validator>()
    (0..definitions.length()).mapNotNull {
        val definition = definitions.optJSONObject(it)
        ValidatorManager.validator(definition.type(), definition.configuration())?.let { validator ->
            out.add(validator)
        }
    }
    return out
}

