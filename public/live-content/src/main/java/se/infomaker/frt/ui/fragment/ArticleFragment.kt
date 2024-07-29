package se.infomaker.frt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.transition.TransitionManager
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import se.infomaker.frt.moduleinterface.BaseModule
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.connectivity.hasInternetConnection
import se.infomaker.frtutilities.ktx.config
import se.infomaker.frtutilities.ktx.moduleInfo
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.di.ArticleViewModelFactory
import se.infomaker.iap.articleview.item.ItemViewFactoryProvider
import se.infomaker.iap.articleview.item.ItemViewFactoryProviderBuilder
import com.navigaglobal.mobile.livecontent.R
import com.navigaglobal.mobile.livecontent.databinding.ModuleArticleBinding
import se.infomaker.iap.articleview.presentation.match.matches
import se.infomaker.iap.articleview.view.ItemAdapter
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.ui.theme.OverlayThemeProvider
import se.infomaker.livecontentui.ads.StickyAdsCoordinator
import se.infomaker.livecontentui.offline.OfflineBannerCoordinator
import se.infomaker.livecontentui.offline.OfflineBannerModel
import javax.inject.Inject

@AndroidEntryPoint
class ArticleFragment : BaseModule() {

    private val moduleInfo by moduleInfo()
    private val resources by resources()
    private val theme by theme()
    private val config by config<ArticleConfig>()
    private val adapterRegistry by lazy { AdapterRegistry() }

    private var binding: ModuleArticleBinding? = null
    private var offlineBannerCoordinator: OfflineBannerCoordinator? = null
    private var stickyAdsCoordinator: StickyAdsCoordinator? = null

    @Inject lateinit var viewFactoryProviderBuilder: ItemViewFactoryProviderBuilder

    @Inject lateinit var articleViewModelFactory: ArticleViewModelFactory
    private val viewModel: ArticleViewModel by viewModels {
        ArticleViewModel.provideFactory(articleViewModelFactory, moduleIdentifier, resources, config)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = ModuleArticleBinding.inflate(inflater, container, false).also { this.binding = it }

        binding.swipeRefreshLayout.setOnRefreshListener {
            refresh()
        }

        val offlineBannerCoordinator = OfflineBannerCoordinator(binding.offlineBanner, resources).also { this.offlineBannerCoordinator = it }
        viewLifecycleOwner.lifecycle.addObserver(offlineBannerCoordinator)

        val stickyAdsCoordinator = StickyAdsCoordinator(binding.topStickyAdWrapper, requireActivity().findViewById(R.id.bottom_sticky_ad_wrapper), config.ads?.provider, config.ads?.stickyArticle).also { this.stickyAdsCoordinator = it }
        viewLifecycleOwner.lifecycle.addObserver(stickyAdsCoordinator)

        setupEmptyView()
        setupErrorView()
        setupOfflineWarningView()

        theme.apply(binding)

        return binding.root
    }

    private fun refresh() {
        viewModel.refresh()
    }

    private fun setupEmptyView() {
        inflateResourceManagedLayout(binding?.emptyContainer, "no_articles", R.layout.no_articles)
    }

    private fun setupErrorView() {
        val errorView = inflateResourceManagedLayout(binding?.errorContainer, "stream_error_view", R.layout.default_error_view)

        val offlineWarningTitle = resources.getString("stream_error_title", null)
        errorView.findViewById<TextView>(R.id.error_title)?.let { titleView ->
            titleView.text = offlineWarningTitle
        }

        val offlineWarningMessage = resources.getString("stream_error_message", null)
        errorView.findViewById<TextView>(R.id.error_message)?.let { messageView ->
            messageView.text = offlineWarningMessage
        }
    }

    private fun setupOfflineWarningView() {
        val offlineView = inflateResourceManagedLayout(binding?.offlineErrorContainer, "stream_offline_error_view", R.layout.default_error_view)

        val offlineWarningTitle = resources.getString("stream_offline_error_title", null)
        offlineView.findViewById<TextView>(R.id.error_title)?.let { titleView ->
            titleView.text = offlineWarningTitle
        }

        val offlineWarningMessage = resources.getString("stream_offline_error_message", null)
        offlineView.findViewById<TextView>(R.id.error_message)?.let { messageView ->
            messageView.text = offlineWarningMessage
        }
    }

    private fun inflateResourceManagedLayout(parent: ViewGroup?, layoutResourceName: String, defaultLayoutIdentifier: Int): View {
        var layoutIdentifier = resources.getLayoutIdentifier(layoutResourceName)
        if (layoutIdentifier < 1) {
            layoutIdentifier = defaultLayoutIdentifier
        }
        return LayoutInflater.from(requireContext()).inflate(layoutIdentifier, parent, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect {
                updateAdapter(it.articles)
                reportStatistics(it.articles)
                handleUi(it)
            }
        }
    }

    private fun reportStatistics(articles: List<ArticleRecord>) {
        // TODO Only log event for articles that are actually being displayed on screen...
        val nonReportedArticles = articles.filterNot { viewModel.reportedArticles.contains(it.uuid) }
        nonReportedArticles.forEach {
            it.logShown(moduleInfo)
            viewModel.registerArticleReported(it)
        }
    }

    private fun updateAdapter(articles: List<ArticleRecord>) {
        val adapters = articles.map { article ->
            val itemViewFactoryProvider = buildItemViewFactoryProvider(article.content, resourceManager)
            adapterRegistry.get(article.uuid)?.also {
                it.update(article.content, itemViewFactoryProvider)
            } ?: createAdapter(article, itemViewFactoryProvider).apply { adapterRegistry.set(article.uuid, this) }
        }

        val adapter = binding?.recyclerView?.adapter
        if (adapter is ConcatAdapter) {
            val removedAdapters = adapter.adapters.toMutableList().apply { removeAll(adapters) }
            removedAdapters.forEach {
                adapter.removeAdapter(it)
                adapterRegistry.remove(it as ItemAdapter)
            }

            // TODO Add at the correct index.
            adapters.toMutableList().apply { removeAll(adapter.adapters) }.forEach { adapter.addAdapter(it) }
        } else if (adapters.isNotEmpty()) {
            val adapterConfig = ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
            binding?.recyclerView?.adapter = ConcatAdapter(adapterConfig, adapters)
        }
    }

    private suspend fun handleUi(state: ArticleState) = coroutineScope {
        binding?.swipeRefreshLayout?.isRefreshing = state.isLoading

        binding?.let { binding ->
            TransitionManager.beginDelayedTransition(binding.root)
            val constraintSet = ConstraintSet().apply { clone(binding.root) }
            if (!state.isLoading && state.articles.isEmpty()) {
                if (!state.hasError) {
                    constraintSet.setVisibility(binding.recyclerView.id, View.GONE)
                    constraintSet.setVisibility(binding.errorContainer.id, View.GONE)
                    constraintSet.setVisibility(binding.offlineErrorContainer.id, View.GONE)
                    constraintSet.setVisibility(binding.emptyContainer.id, View.VISIBLE)
                }
                else if (context?.hasInternetConnection() == true) {
                    constraintSet.setVisibility(binding.recyclerView.id, View.GONE)
                    constraintSet.setVisibility(binding.errorContainer.id, View.VISIBLE)
                    constraintSet.setVisibility(binding.offlineErrorContainer.id, View.GONE)
                    constraintSet.setVisibility(binding.emptyContainer.id, View.GONE)
                }
                else {
                    constraintSet.setVisibility(binding.recyclerView.id, View.GONE)
                    constraintSet.setVisibility(binding.errorContainer.id, View.GONE)
                    constraintSet.setVisibility(binding.offlineErrorContainer.id, View.VISIBLE)
                    constraintSet.setVisibility(binding.emptyContainer.id, View.GONE)
                }
            }
            else {
                constraintSet.setVisibility(binding.recyclerView.id, View.VISIBLE)
                constraintSet.setVisibility(binding.errorContainer.id, View.GONE)
                constraintSet.setVisibility(binding.offlineErrorContainer.id, View.GONE)
                constraintSet.setVisibility(binding.emptyContainer.id, View.GONE)
            }
            constraintSet.applyTo(binding.root)
        }

        offlineBannerCoordinator?.let { coordinator ->
            val hasContent = state.articles.isNotEmpty()
            (if (hasContent) state.updateInfo.lastUpdated else state.updateInfo.lastUpdateAttempt)?.let { date ->
                coordinator.bind(OfflineBannerModel(date, hasContent))
            }
        }
    }

    private fun createAdapter(article: ArticleRecord, itemViewFactoryProvider: ItemViewFactoryProvider): ItemAdapter {
        val presentationThemes = mutableListOf<String>()
        config.contentPresentation?.extraThemes?.filter { it.provide().matches(article.content.properties, article.presentationContext) }?.forEach {
            presentationThemes.addAll(it.themes)
        }
        config.contentPresentation?.themes?.firstOrNull { it.provide().matches(article.content.properties, article.presentationContext) }?.let {
            presentationThemes.addAll(it.themes)
        }
        val theme = OverlayThemeProvider.forModule(requireContext(), moduleIdentifier).getTheme(presentationThemes)
        return ItemAdapter(article.content, moduleIdentifier, resources, theme, itemViewFactoryProvider, this) {}
    }

    private fun buildItemViewFactoryProvider(contentStructure: ContentStructure?, resourceManager: ResourceManager): ItemViewFactoryProvider {
        viewFactoryProviderBuilder.reset()
        val allPreprocessors = contentStructure?.presentation?.preprocessors?.union(config.preprocessors
            ?: emptyList())
        allPreprocessors?.forEach { viewFactoryProviderBuilder.withPreprocessor(it, resourceManager) }
        return viewFactoryProviderBuilder.build()
    }

    override fun shouldDisplayToolbar() = true

    override fun onBackPressed() = false

    override fun onAppBarPressed() {
        binding?.recyclerView?.smoothScrollToPosition(0)
    }
}

private class AdapterRegistry {

    private val registry by lazy { mutableMapOf<String, ItemAdapter>() }

    fun get(uuid: String) = registry[uuid]

    fun set(uuid: String, adapter: ItemAdapter) {
        registry[uuid] = adapter
    }

    fun remove(adapter: ItemAdapter) {
        registry.filterValues { it == adapter }.map { it.key }.forEach { registry.remove(it) }
    }
}

private fun Theme.apply(binding: ViewBinding) {
    apply(binding.root)
}