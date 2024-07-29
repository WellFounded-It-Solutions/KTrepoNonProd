package se.infomaker.iap.articleview.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxrelay2.BehaviorRelay
import com.navigaglobal.mobile.livecontent.R
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.connectivity.Connectivity
import se.infomaker.frtutilities.ktx.moduleInfo
import se.infomaker.frtutilities.runtimeconfiguration.OnModuleConfigChangeListener
import se.infomaker.iap.articleview.ArticleConfig
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactoryProvider
import se.infomaker.iap.articleview.item.ItemViewFactoryProviderBuilder
import se.infomaker.iap.articleview.item.RequiresNetwork
import se.infomaker.iap.articleview.item.author.RecyclerViewDividerDecorator
import se.infomaker.iap.articleview.presentation.match.matches
import se.infomaker.iap.articleview.theme.TextSizeAwareTheme
import se.infomaker.iap.articleview.transformer.Transformer
import se.infomaker.iap.articleview.transformer.TransformerProvider
import se.infomaker.iap.articleview.util.getJSONObject
import se.infomaker.iap.articleview.view.modifier.TextSizeMultiplier
import se.infomaker.iap.articleview.view.modifier.TextSizeMultiplierProvider
import se.infomaker.iap.theme.OnThemeUpdateListener
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.util.UI
import se.infomaker.iap.theme.view.ThemeableFrameLayout
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.iap.ui.theme.OverlayThemeProvider
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
open class ContentFragment : Fragment(), OnThemeUpdateListener, OnModuleConfigChangeListener {

    private val moduleInfo by moduleInfo()
    val moduleId: String?
        get() = moduleInfo.identifier
    val moduleName: String?
        get() = moduleInfo.name

    private var connectionDisposable: Disposable? = null
    private val resourceManager by lazy { ResourceManager(context, moduleId) }
    var backgroundView: ThemeableFrameLayout? = null
    var recyclerView: RecyclerView? = null
    var progressView: ContentLoadingProgressBar? = null
    var errorTextView: ThemeableTextView? = null
    var transformer: Transformer? = null
    var properties: JSONObject? = null
    val viewDisposables = CompositeDisposable()
    var config: ArticleConfig? = null
    var themeOverlays: List<String>? = null
    private var presentationContext: JSONObject? = null
    val relay = BehaviorRelay.create<JSONObject>()
    private val _resumedState = BehaviorRelay.createDefault(FocusState.OUT_OF_FOCUS)
    open val focusState: Observable<FocusState>
        get() = _resumedState
    val contentRelay = BehaviorRelay.create<ContentStructure>()
    var overlayThemeProvider: OverlayThemeProvider? = null
    var dividerDecorator: RecyclerViewDividerDecorator? = null
    private var textSizeMultiplier: TextSizeMultiplier? = null
    private val networkedItems = mutableListOf<Int>()
    private val blockedItems = BehaviorRelay.createDefault(emptyList<Item>())

    @Inject lateinit var viewFactoryProviderBuilder: ItemViewFactoryProviderBuilder

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeOverlays = arguments?.getStringArrayList(THEME_OVERLAYS)
        setHasOptionsMenu(true)
        loadConfig()
        properties = arguments?.getJSONObject(PROPERTIES)
        presentationContext = arguments?.getJSONObject(PRESENTATION_CONTEXT)
    }

    private fun loadConfig() {
        config = ConfigManager.getInstance(activity).getConfig(moduleName, moduleId, ArticleConfig::class.java)
        Timber.e("ContentFragment Name: %s, ID: %s", moduleName, moduleId)
        config?.let { articleConfig ->
            transformer = TransformerProvider.getTransformer(articleConfig)
            if (articleConfig.hideTextSizeModifier != true) {
                setHasOptionsMenu(true)
                textSizeMultiplier = TextSizeMultiplierProvider.provide(requireContext(), articleConfig.textSizeSteps)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_change_text_size)?.apply {
            resourceManager
                .getDrawableIdentifier("action_change_text_size")
                .takeIf { it != 0 }
                ?.let { this.icon = AppCompatResources.getDrawable(requireContext(), it) }
        }?.applyTheme(resolveTheme())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_text_size -> {
                textSizeMultiplier?.next()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        ConfigManager.getInstance().registerOnModuleConfigChangeListener(this)
        ThemeManager.getInstance(context).addOnUpdateListener(this)
        connectionDisposable = Connectivity.observable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { e -> Timber.e(e) }
                .subscribe {
                    if (it == true) {
                        Timber.d("Detected internet connection, notifying network dependent items.")
                        networkedItems.forEach { index ->
                            recyclerView?.adapter?.notifyItemChanged(index)
                        }
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        _resumedState.accept(FocusState.IN_FOCUS)
    }

    override fun onPause() {
        super.onPause()
        _resumedState.accept(FocusState.OUT_OF_FOCUS)
    }

    override fun onStop() {
        super.onStop()
        ConfigManager.getInstance().removeOnModuleConfigChangeListener(this)
        ThemeManager.getInstance(context).removeOnUpdateListener(this)
        connectionDisposable?.dispose()
        networkedItems.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.article_fragment, container, false) as ThemeableFrameLayout
        this.backgroundView = view
        recyclerView = view.findViewById(R.id.recyclerView)
        errorTextView = view.findViewById(R.id.errorTextView)

        overlayThemeProvider = OverlayThemeProvider.forModule(activity, moduleId)

        recyclerView?.layoutManager = object : StickyHeadersLinearLayoutManager<ItemAdapter>(requireActivity()) {
            override fun getExtraLayoutSpace(state: RecyclerView.State?): Int {
                val extraLayoutSpace = super.getExtraLayoutSpace(state)
                return if (extraLayoutSpace == 0) UI.dp2px(300f).toInt() else extraLayoutSpace
            }
        }

        val snapHelper = MyVerticalSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        progressView = view.findViewById(R.id.progress)
        themeChrome()
        progressView?.show()
        transformer?.let { transformer ->
            val content = Observables.combineLatest(relay, focusState) { article, focusState -> Pair(article, focusState) }
                    .map { (article, focus) ->
                        val contentStructure = transformer.transform(article)
                            .withContentPresentation(config?.contentPresentation, presentationContext)
                        ContentState(contentStructure, focus)
                    }
                    .map { state -> state.apply { content.preprocess(config?.preprocessors, resourceManager) } }
                    .map { state -> state.content.apply { body.items.filterIsInstance(FocusAware::class.java).forEach { it.focusState = state.focus } } }

            viewDisposables.add(
                    Observables.combineLatest(content, blockedItems) { article, blockedItems -> article.apply { body.stripItemsMatching(blockedItems) } }
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ contentStructure ->
                                contentRelay.accept(contentStructure)
                                progressView?.hide()
                                if (contentStructure.body.items.size != 0) {
                                    activity?.let { context ->
                                        contentStructure.body.items.forEach {
                                            it.listeners.forEach { listener ->
                                                listener.onPreHeat(it, context)
                                            }
                                        }
                                    }

                                    val itemViewFactoryProvider = buildItemViewFactoryProvider(contentStructure, resourceManager)
                                    val adapter = recyclerView?.adapter
                                    if (adapter is ItemAdapter) {
                                        adapter.update(updated = contentStructure, viewFactoryProvider = itemViewFactoryProvider)
                                    } else {
                                        recyclerView?.adapter = createAdapter(contentStructure, itemViewFactoryProvider)
                                    }

                                    getNetworkedItems(contentStructure)

                                } else {
                                    errorTextView?.visibility = View.VISIBLE
                                    recyclerView?.visibility = View.GONE
                                }
                            }, { error -> Timber.e(error, "Could not transform and load article") }))
        }
        arguments?.getJSONObject(PROPERTIES)?.let {
            relay.accept(it)
        }
        return view
    }

    private fun createAdapter(contentStructure: ContentStructure, itemViewFactoryProvider: ItemViewFactoryProvider): ItemAdapter {
        val overlayTheme = resolveTheme()
        val theme = textSizeMultiplier?.let { TextSizeAwareTheme(overlayTheme, lifecycle, it.observable()) } ?: overlayTheme
        return ItemAdapter(contentStructure, moduleId!!, resourceManager, theme, itemViewFactoryProvider, this) { failed ->
            val current = blockedItems.value
            if (current != null) {
                val updated = current.toMutableList().apply { add(failed) }
                blockedItems.accept(updated.toList())
            }
            else {
                blockedItems.accept(listOf(failed))
            }
        }
    }

    private fun buildItemViewFactoryProvider(contentStructure: ContentStructure, resourceManager: ResourceManager): ItemViewFactoryProvider {
        viewFactoryProviderBuilder.reset()
        val allPreprocessors = contentStructure.presentation.preprocessors.union(config?.preprocessors ?: emptyList())
        allPreprocessors.forEach { viewFactoryProviderBuilder.withPreprocessor(it, resourceManager) }
        return viewFactoryProviderBuilder.build()
    }

    private fun getNetworkedItems(contentStructure: ContentStructure) {
        contentStructure.body.items.forEachIndexed { index, item ->
            if (item is RequiresNetwork) {
                networkedItems.add(index)
            }
        }
    }

    private fun themeChrome() {
        val theme = resolveTheme()

        context?.let { context ->
            ResourcesCompat.getDrawable(context.resources, R.drawable.recycler_horizontal_divider, null)?.let { dividerDrawable ->
                dividerDecorator?.let { itemDecoration ->
                    recyclerView?.removeItemDecoration(itemDecoration)
                }
                dividerDecorator = RecyclerViewDividerDecorator(dividerDrawable, theme, resourceManager).also { itemDecoration ->
                    recyclerView?.addItemDecoration(itemDecoration)
                }
            }
        }

        backgroundView?.apply(theme)
        errorTextView?.apply(theme)
    }

    fun update(properties: JSONObject) {
        relay.accept(properties)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewDisposables.clear()
    }

    override fun onThemeUpdated() {
        overlayThemeProvider?.reset()
        themeChrome()
        (recyclerView?.adapter as? ItemAdapter)?.let {
            it.theme = resolveTheme()
            it.notifyDataSetChanged()
        }
    }

    override fun onModuleConfigUpdated(modules: Set<String>) {
        loadConfig()
        relay.value?.let {
            update(it)
        }
    }

    private fun resolveTheme(): Theme {
        val presentationThemes = themeOverlays?.toMutableList() ?: mutableListOf()
        val matchProperties = properties ?: JSONObject()
        config?.contentPresentation?.extraThemes?.filter { it.provide().matches(matchProperties, presentationContext) }?.forEach {
            presentationThemes.addAll(it.themes)
        }
        config?.contentPresentation?.themes?.firstOrNull { it.provide().matches(matchProperties, presentationContext) }?.let {
            presentationThemes.addAll(it.themes)
        }
        return overlayThemeProvider?.getTheme(presentationThemes)
            ?: ThemeManager.getInstance(context).appTheme
    }

    companion object {
        const val MODULE_ID = "moduleId"
        const val MODULE_NAME = "moduleName"
        const val THEME_OVERLAYS = "themeOverlays"
        const val PROPERTIES = "properties"
        const val PRESENTATION_CONTEXT = "presentationContext"

        @JvmOverloads
        fun newInstance(moduleId: String, moduleName: String, properties: JSONObject, themeOverlays: List<String>, presentationContext: JSONObject? = null): ContentFragment {
            return ContentFragment().apply {
                addAttributes(this, moduleId, moduleName, properties, themeOverlays, presentationContext)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun addAttributes(fragment: Fragment, moduleId: String, moduleName: String, properties: JSONObject, themeOverlays: List<String>?, presentationContext: JSONObject? = null) {
            val arguments = Bundle()
            arguments.putString(MODULE_ID, moduleId)
            arguments.putString(MODULE_NAME, moduleName)
            if (themeOverlays != null) {
                arguments.putStringArrayList(THEME_OVERLAYS, ArrayList(themeOverlays))
            }
            arguments.putString(PROPERTIES, properties.toString())
            presentationContext?.let {
                arguments.putString(PRESENTATION_CONTEXT, it.toString())
            }
            fragment.arguments = arguments
        }
    }
}

private fun MenuItem.applyTheme(theme: Theme) {
    icon?.let { itemIcon ->
        val color = theme.getColor("toolbarAction", ThemeColor.WHITE).get()
        DrawableCompat.setTint(itemIcon, color)
    }
}
