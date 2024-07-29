package se.infomaker.livecontentui.livecontentrecyclerview.adapter

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import se.infomaker.datastore.ArticleLastViewMemoryCache
import se.infomaker.datastore.DatabaseSingleton
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.OnPresentationContextChangedListener
import se.infomaker.livecontentui.ViewBehaviour
import se.infomaker.livecontentui.config.BindingOverride
import se.infomaker.livecontentui.config.ContentPresentationConfig
import se.infomaker.livecontentui.config.ContentPresentationConfig.Companion.DEFAULT_TEASER
import se.infomaker.livecontentui.extensions.isFirst
import se.infomaker.livecontentui.extensions.isRelated
import se.infomaker.livecontentui.extensions.matchTeaser
import se.infomaker.livecontentui.extensions.matchThemes
import se.infomaker.livecontentui.extensions.safePut

class ContentPresentationBehaviour(
    private val resourceManager: ResourceManager,
    private val contentPresentation: ContentPresentationConfig,
    moduleIdentifier: String,
    lifecycleOwner: LifecycleOwner,
    private val onPresentationContextChangedListener: OnPresentationContextChangedListener
) : ViewBehaviour<PropertyObject>, LifecycleObserver {

    private val garbage = CompositeDisposable()
    private val baseContext by lazy { JSONObject().apply { safePut("moduleId", moduleIdentifier) } }
    private val readUuids = ArticleLastViewMemoryCache.get().map { it.uuid }.toMutableList()
    private val bookmarkedUuids = mutableListOf<String>()
    private val viewTypeMap = mutableMapOf<String, Int>()
    private val teasers = mutableMapOf<Int, Pair<String, List<BindingOverride>?>>()
    private val themes = mutableMapOf<String, List<String>?>()

    private var nextViewType = 0

    init {
        garbage.add(ArticleLastViewMemoryCache.observe()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { articles ->

                val new = articles.map { it.uuid }
                val changed = new - readUuids

                changed.forEach {
                    viewTypeMap.remove(it)
                    themes.remove(it)
                }

                readUuids.clear()
                readUuids.addAll(new)

                val changes = changed.associateWith { JSONObject().apply { put("read", true.toString()) } }

                onPresentationContextChangedListener.onPresentationContextChanged(changes)
            })

        DatabaseSingleton.getDatabaseInstance().bookmarkDao().all().observe(lifecycleOwner) { bookmarks ->
            val new = bookmarks.map { it.uuid }
            val bookmarked = new - bookmarkedUuids
            val unBookmarked = bookmarkedUuids - new
            val changed = bookmarked union unBookmarked

            changed.forEach {
                viewTypeMap.remove(it)
                themes.remove(it)
            }

            bookmarkedUuids.clear()
            bookmarkedUuids.addAll(new)

            val changes = bookmarked.associateWith { JSONObject().apply { put("bookmarked", true.toString()) } } +
                    unBookmarked.associateWith { JSONObject().apply { put("bookmarked", false.toString()) } }

            onPresentationContextChangedListener.onPresentationContextChanged(changes)
        }

        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun clear() {
        garbage.clear()
    }

    override fun viewTypeForKey(key: PropertyObject): Int {

        viewTypeMap[key.key]?.let {
            return it
        }

        val itemContext = itemContextForKey(key)
        val teaser = contentPresentation.matchTeaser(key.properties, itemContext)?.let {
            Pair(it.layout, it.bindingOverrides)
        } ?: run {
            Pair(DEFAULT_TEASER, null)
        }

        teasers.filterValues { it == teaser }.keys.firstOrNull()?.let {
            viewTypeMap[key.key] = it
            return it
        }

        teasers[nextViewType] = teaser
        viewTypeMap[key.key] = nextViewType

        return nextViewType++
    }

    override fun layoutResourceForViewType(viewType: Int) = resourceManager.getLayoutIdentifier(teasers[viewType]?.first ?: DEFAULT_TEASER)

    override fun bindingOverridesForViewType(viewType: Int) = teasers[viewType]?.second

    override fun themesForKey(key: PropertyObject): List<String>? {

        themes[key.key]?.let {
            return it
        }

        val itemContext = itemContextForKey(key)
        return contentPresentation.matchThemes(key.properties, itemContext).also {
            this.themes[key.key] = it
        }
    }

    override fun presentationContextForKey(key: PropertyObject): JSONObject {
        return itemContextForKey(key)
    }

    private fun itemContextForKey(key: PropertyObject): JSONObject {
        val presentationContext = baseContext
        presentationContext.safePut(CONTEXT_READ_KEY_PATH, (key.id in readUuids).toString())
        presentationContext.safePut(CONTEXT_BOOKMARKED_KEY_PATH, (key.id in bookmarkedUuids).toString())
        presentationContext.safePut(CONTEXT_RELATED_KEY_PATH, key.isRelated)
        presentationContext.safePut(CONTEXT_FIRST_KEY_PATH, key.isFirst)
        return presentationContext
    }

    companion object {
        private const val CONTEXT_READ_KEY_PATH = "read"
        private const val CONTEXT_BOOKMARKED_KEY_PATH = "bookmarked"
        private const val CONTEXT_RELATED_KEY_PATH = "related"
        private const val CONTEXT_FIRST_KEY_PATH = "position.first"
    }
}

private val PropertyObject.key: String
    get() = "$id:$isRelated"