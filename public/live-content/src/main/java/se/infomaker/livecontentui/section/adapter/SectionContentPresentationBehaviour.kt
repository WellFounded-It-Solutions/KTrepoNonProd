package se.infomaker.livecontentui.section.adapter

import androidx.lifecycle.LifecycleOwner
import org.json.JSONObject
import se.infomaker.datastore.DatabaseSingleton
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.livecontentui.OnPresentationContextChangedListener
import se.infomaker.livecontentui.config.BindingOverride
import se.infomaker.livecontentui.config.ContentPresentationConfig
import se.infomaker.livecontentui.config.ContentPresentationConfig.Companion.DEFAULT_TEASER
import se.infomaker.livecontentui.extensions.matchTeaser
import se.infomaker.livecontentui.extensions.matchThemes
import se.infomaker.livecontentui.extensions.patch
import se.infomaker.livecontentui.extensions.safePut
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.AdWrapperAdapter.AD_LAYOUT_ID
import se.infomaker.livecontentui.section.ContentPresentationAware
import se.infomaker.livecontentui.section.PropertyObjectSectionItem
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.ads.AdSectionItem
import se.infomaker.livecontentui.section.ktx.isRelated
import se.infomaker.livecontentui.section.supplementary.ExpandableSectionItem

class SectionContentPresentationBehaviour(
    private val resourceManager: ResourceManager,
    private val contentPresentation: ContentPresentationConfig,
    moduleIdentifier: String,
    lifecycleOwner: LifecycleOwner,
    private val onPresentationContextChangedListener: OnPresentationContextChangedListener
) : SectionViewBehaviour {

    private val baseContext by lazy { JSONObject().apply { safePut("moduleId", moduleIdentifier) } }
    private val viewTypeMap = mutableMapOf<String, Int>()
    private val teasers = mutableMapOf<Int, Teaser>()
    private val themes = mutableMapOf<String, List<String>?>()
    private var readUuids = emptyList<String>()
    private var bookmarkedUuids = emptyList<String>()

    private var nextViewType = 0

    init {
        DatabaseSingleton.getDatabaseInstance().userLastViewDao().liveAll().observe(lifecycleOwner) { readArticles ->
            val new = readArticles.map { it.uuid }
            val read = new - readUuids

            val changes = read.associateWith { JSONObject().apply { put("read", true.toString()) } }

            onPresentationContextChangedListener.onPresentationContextChanged(changes)
        }
        DatabaseSingleton.getDatabaseInstance().bookmarkDao().all().observe(lifecycleOwner) { bookmarks ->
            val new = bookmarks.map { it.uuid }
            val bookmarked = new - bookmarkedUuids
            val unBookmarked = bookmarkedUuids - new
            bookmarkedUuids = new

            val changes = bookmarked.associateWith { JSONObject().apply { put("bookmarked", true.toString()) } } +
                    unBookmarked.associateWith { JSONObject().apply { put("bookmarked", false.toString()) } }

            onPresentationContextChangedListener.onPresentationContextChanged(changes)
        }
    }

    override fun update(sectionItems: List<SectionItem>) {
        viewTypeMap.clear()

        sectionItems.forEach { sectionItem ->
            val id = sectionItem.id
            val teaser = (sectionItem as? ContentPresentationAware)?.let { item ->
                val itemContext = item.context.patch(baseContext)
                itemContext.safePut("read", (id in readUuids).toString())
                itemContext.safePut("bookmarked", (id in bookmarkedUuids).toString())
                item.context = itemContext

                val itemContent = item.content ?: EMPTY_JSON
                themes[sectionItem.key] = contentPresentation.matchThemes(itemContent, itemContext)
                contentPresentation.matchTeaser(itemContent, itemContext)?.let { teaserSpec ->
                    val layout = resourceManager.getLayoutIdentifier(teaserSpec.layout)
                    Teaser(layout, teaserSpec.layout, teaserSpec.bindingOverrides)
                }
            } ?: run {
                // TODO This keeps changing, we should probably figure content presentation for everything, for now just use default for everything that is a property object
                when (sectionItem) {
                    is ExpandableSectionItem -> Teaser(resourceManager.getLayoutIdentifier("default_list_expand_button"), sectionItem.templateReference())
                    is PropertyObjectSectionItem -> Teaser(resourceManager.getLayoutIdentifier(DEFAULT_TEASER), DEFAULT_TEASER)
                    is AdSectionItem -> Teaser(resourceManager.getLayoutIdentifier(AD_LAYOUT_ID), sectionItem.templateReference())
                    else -> Teaser(sectionItem.defaultTemplate(), sectionItem.templateReference())
                }
            }

            teasers.filterValues { it == teaser }.keys.firstOrNull()?.let {
                viewTypeMap[sectionItem.key] = it
            } ?: run {
                teasers[nextViewType] = teaser
                viewTypeMap[sectionItem.key] = nextViewType++
            }
        }
    }

    override fun viewTypeForKey(key: SectionItem): Int = viewTypeMap[key.key] ?: 0

    override fun layoutResourceForViewType(viewType: Int): Int =
        teasers[viewType]?.layoutResource ?: resourceManager.getLayoutIdentifier(
            DEFAULT_TEASER
        )

    override fun bindingOverridesForViewType(viewType: Int) = teasers[viewType]?.bindingOverrides

    override fun themesForKey(key: SectionItem): List<String> {
        val itemThemes = key.overlayThemes()?.toMutableList() ?: mutableListOf()
        themes[key.key]?.let {
            itemThemes.addAll(it)
        }
        return itemThemes.toSet().toList().ifEmpty { emptyList() }
    }

    companion object {
        private val EMPTY_JSON by lazy { JSONObject() }
    }
}

private val SectionItem.key: String
    get() = "$id:${sectionIdentifier()}:$isRelated:${isSublistContent}"

private val SectionItem.isSublistContent: Boolean
    get() = when(this) {
        is ContentPresentationAware -> context?.optBoolean("sublist") == true
        else -> false
}

private data class Teaser(
    val layoutResource: Int,
    val layout: String,
    val bindingOverrides: List<BindingOverride>? = null
)