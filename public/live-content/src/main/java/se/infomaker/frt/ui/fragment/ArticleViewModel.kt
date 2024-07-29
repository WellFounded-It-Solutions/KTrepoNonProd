package se.infomaker.frt.ui.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.di.ArticleViewModelFactory
import se.infomaker.iap.articleview.transformer.newsml.NewsMLTransformerManager
import se.infomaker.iap.articleview.view.FocusAware
import se.infomaker.iap.articleview.view.FocusState
import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentmanager.stream.HitsListStream
import se.infomaker.livecontentmanager.stream.StreamListener
import se.infomaker.livecontentui.LiveContentStreamProvider

/**
 * This class is specifically _NOT_ using [dagger.hilt.android.lifecycle.HiltViewModel]
 * at this moment.
 *
 * Rewrite this when [dagger.hilt.android.lifecycle.HiltViewModel] supports
 * [AssistedInject] without the workaround of all these factories.
 */
class ArticleViewModel @AssistedInject constructor(
    private val liveContentStreamProvider: LiveContentStreamProvider,
    @Assisted moduleId: String,
    @Assisted private val resources: ResourceManager,
    @Assisted private val config: ArticleConfig
) : ViewModel(), StreamListener<PropertyObject> {

    private val _state = MutableStateFlow(ArticleState.INITIAL)
    private val transformer by lazy { NewsMLTransformerManager.createTransformer("newsML") }
    private val stream by lazy { provideStream() }
    private val presentationContext by lazy { JSONObject().apply { put("moduleId", moduleId) } }

    private val _reportedArticles = mutableListOf<String>()
    val reportedArticles: List<String>
        get() = _reportedArticles.toList()


    val state: StateFlow<ArticleState>
        get() = _state.asStateFlow()

    init {
        stream.addListener(this)
        val currentStreamItems = stream.items
        updateState(currentStreamItems.asArticleRecords(), isLoading = currentStreamItems.size == 0 && !stream.hasError())
    }

    private fun updateState(items: List<ArticleRecord>? = null, isLoading: Boolean? = null) {
        _state.value = _state.value.copy(
            articles = items ?: _state.value.articles,
            isLoading = isLoading ?: _state.value.isLoading,
            hasError = stream.hasError(),
            updateInfo = stream.updateInfo
        )
    }

    fun refresh() {
        stream.reset()
    }

    override fun onCleared() {
        super.onCleared()
        stream.removeListener(this)
    }

    private fun provideStream(): HitsListStream {
        return liveContentStreamProvider.provide(config.liveContent, config.liveContent.defaultProperties, null)
    }

    override fun onItemsAdded(index: Int, items: MutableList<PropertyObject>) {
        updateState(stream.items.asArticleRecords(), false)
    }

    override fun onItemsRemoved(items: MutableList<PropertyObject>) {
        updateState(stream.items.asArticleRecords(), false)
    }

    override fun onItemsChanged(items: MutableList<PropertyObject>) {
        updateState(stream.items.asArticleRecords(), false)
    }

    override fun onEndReached() {
        updateState(stream.items.asArticleRecords(), false)
    }

    override fun onReset() {
        updateState(isLoading = true)
    }

    override fun onError(exception: Exception) {
        _state.value = _state.value.copy(isLoading = false, hasError = true, articles = stream.items.asArticleRecords())
    }

    private fun List<PropertyObject>.asArticleRecords() = map { ArticleRecord(it.id, transformer.transform(it.properties), presentationContext) }
        .map { it.copy(content = it.content.withContentPresentation(config.contentPresentation, presentationContext)) }
        .map { it.copy(content = it.content.preprocess(config.preprocessors, resources)) }
        .also { items -> items.map { it.content.body.items }.forEach { it.filterIsInstance(FocusAware::class.java).map { focusAwareItem ->  focusAwareItem.focusState = FocusState.IN_FOCUS } } }

    fun registerArticleReported(articleRecord: ArticleRecord) {
        _reportedArticles.add(articleRecord.uuid)
    }

    companion object {
        fun provideFactory(
            assistedFactory: ArticleViewModelFactory,
            moduleId: String,
            resources: ResourceManager,
            config: ArticleConfig
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(moduleId, resources, config) as T
            }
        }
    }
}

private val HitsListStream.updateInfo: UpdateInfo
    get() = UpdateInfo(lastUpdated, lastUpdateAttempt)