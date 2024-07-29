package se.infomaker.livecontentui.section.datasource.list

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import okhttp3.internal.toImmutableList
import org.json.JSONObject
import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentmanager.config.PresentationBehaviour
import se.infomaker.livecontentmanager.extensions.getLastUpdated
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser
import se.infomaker.livecontentmanager.query.ParameterSearchQuery
import se.infomaker.livecontentmanager.query.Query
import se.infomaker.livecontentmanager.query.QueryManager
import se.infomaker.livecontentmanager.query.QueryResponseListener
import se.infomaker.livecontentmanager.query.lcc.BroadcastObjectChangeManager
import se.infomaker.livecontentui.extensions.optSortedListObjects
import se.infomaker.livecontentui.extensions.putAt
import se.infomaker.livecontentui.section.BehaviorResolver
import se.infomaker.livecontentui.section.ContentPresentationAware
import se.infomaker.livecontentui.section.LayoutResolver
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.SectionItemFactory
import se.infomaker.livecontentui.section.SectionUpdateNotifier
import se.infomaker.livecontentui.section.configuration.ExtraContent
import se.infomaker.livecontentui.section.configuration.Orientation
import se.infomaker.livecontentui.section.datasource.DataSource
import se.infomaker.livecontentui.section.datasource.DataSourceResponse
import se.infomaker.livecontentui.section.supplementary.SupplementarySectionItemFactory
import timber.log.Timber

class ListDataProvider @JvmOverloads constructor(
    layoutResolver: LayoutResolver,
    behaviorResolver: BehaviorResolver,
    private val liveContentConfig: LiveContentConfig,
    private val sectionIdentifier: String,
    private val config: ListSectionConfig,
    private val queryManager: QueryManager,
    private val changeManager: BroadcastObjectChangeManager,
    private val context: JSONObject? = null,
    private val layout: Orientation = Orientation.VERTICAL,
    private val extra: ExtraContent? = null
) : DataSource, QueryResponseListener {

    private val sectionItemFactory = SectionItemFactory(layoutResolver, behaviorResolver, config, SupplementarySectionItemFactory(liveContentConfig))
    private val properties = liveContentConfig.getProperties(config.propertyMapReference)
    private val typePropertyMap = liveContentConfig.typePropertyMap ?: emptyMap()
    private val typeDescriptionTemplate = liveContentConfig.typeDescriptionTemplate ?: emptyMap()
    private val objectParser =  DefaultPropertyObjectParser(typePropertyMap, typeDescriptionTemplate, liveContentConfig.transformSettings)
    private val publishRelay = BehaviorRelay.create<DataSourceResponse>()
    private val extraRelay = BehaviorRelay.createDefault<List<SectionItem>>(emptyList())
    private val currentObjects = mutableSetOf<String>()
    private val disposables = CompositeDisposable()
    private var retryCount = 0

    private var extraQueryListener = object : QueryResponseListener {
        override fun onResponse(query: Query, response: JSONObject) {
            extra?.let { extraContent ->
                try {
                    val items = objectParser.fromSearch(response, extraContent.config.propertyMapReference)
                        .flatMap { list ->
                            list.optSortedListObjects() ?: emptyList()
                        }
                        .flatMap { propertyObject ->
                            sectionItemFactory.listFromPropertyObject(propertyObject, sectionIdentifier, groupKey, context)
                        }
                    extraRelay.accept(items)
                }
                catch (throwable: Throwable) {
                    Timber.e(throwable, "Failed to parse extra content")
                    extraRelay.accept(emptyList())
                }
            }
        }

        override fun onError(exception: Throwable) {
            Timber.e(exception, "Failed to fetch extra content")
            extraRelay.accept(emptyList())
        }
    }

    private val groupKey: String
        get() = if (config.group != null) config.group else DEFAULT_GROUP_KEY

    override fun resume(): Boolean {
        if (disposables.size() == 0) {
            update()
            disposables.add(
                changeManager.observeWithFilter(currentObjects).subscribe { s: String? ->
                    if (config.isUpdateAllOnChange) {
                        SectionUpdateNotifier.updateAll()
                    } else {
                        update()
                    }
                })
            if (currentObjects.size > 0) {
                changeManager.subscribe(currentObjects)
            }
            return true
        }
        return false
    }

    override fun update() {
        queryManager.addQuery(
            ParameterSearchQuery(
                liveContentConfig.search.contentProvider,
                properties,
                config.queryParams,
                null
            ),
            this
        )
        extra?.let {
            queryManager.addQuery(
                ParameterSearchQuery(
                    null,
                    liveContentConfig.getProperties(it.config.propertyMapReference),
                    it.config.queryParams,
                    null
                ),
                extraQueryListener
            )
        }
    }

    override fun pause() {
        if (disposables.size() > 0 && currentObjects.size > 0) {
            changeManager.unsubscribe(currentObjects)
        }
        disposables.clear()
    }

    override fun observeResponse(): Observable<DataSourceResponse> {
        return Observables.combineLatest(publishRelay, extraRelay) { dataSourceResponse, extraItems ->
            if (extra != null && extra.positions.isNotEmpty()) {
                val outItems = dataSourceResponse.items.toMutableList()
                extra.positions.withIndex().map { (index, position) ->
                    extraItems.getOrNull(index)?.let { extraItem ->
                        if (position < outItems.size) {
                            outItems.add(position, extraItem)
                        }
                        else {
                            outItems.add(extraItem)
                        }
                    }
                }
                DataSourceResponse(outItems.toImmutableList(), dataSourceResponse.error, dataSourceResponse.lastUpdated)
            }
            else {
                dataSourceResponse
            }
        }
    }

    override fun groupKeys() = setOf(groupKey)

    @Synchronized
    override fun onResponse(query: Query, response: JSONObject) {
        retryCount = 0
        try {
            val items = objectParser.fromSearch(response, config.propertyMapReference)
                .flatMap { list ->
                    when(layout) {
                        Orientation.HORIZONTAL -> listOf(list).withIndex()
                        else -> list.optSortedListObjects()?.withIndex() ?: emptyList()
                    }
                }
                .flatMap { indexObject ->
                    val related = config.presentationBehaviour == PresentationBehaviour.RELATED && indexObject.index != 0
                    sectionItemFactory.listFromPropertyObject(indexObject.value, sectionIdentifier, groupKey, context, liveContentConfig.subListBehaviour, layout, related)
                }

            (items.firstOrNull() as? ContentPresentationAware)?.let { item ->
                val itemContext = item.context ?: JSONObject()
                itemContext.putAt("position.first", true)
                item.context = itemContext
            }

            val newUuids = items.map { it.id }.toSet()
            val listUuids = objectParser.fromSearch(response, config.propertyMapReference).map { it.id }.toSet()
            val allUuids = newUuids + listUuids
            val added = if (currentObjects.isNotEmpty()) {
                val removed = currentObjects.filterNot { allUuids.contains(it) }
                if (removed.isNotEmpty()) {
                    changeManager.unsubscribe(removed.toSet())
                }
                allUuids.filterNot { currentObjects.contains(it) }
            } else {
                allUuids
            }
            currentObjects.clear()
            currentObjects.addAll(allUuids)
            if (added.isNotEmpty()) {
                changeManager.subscribe(java.util.HashSet(added))
            }
            Timber.d("got " + items.size + " items")
            publishRelay.accept(DataSourceResponse(items, null, response.getLastUpdated()))
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Failed to parse response")
            publishRelay.accept(DataSourceResponse(null, throwable, null))
        }
    }

    override fun onError(exception: Throwable) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++
            update()
            Timber.e(exception, "Retrying $retryCount (max retries $MAX_RETRY_COUNT)")
        } else {
            retryCount = 0
            Timber.w(exception, "Could not load data")
            publishRelay.accept(DataSourceResponse(null, exception, null))
        }
    }

    companion object {
        const val DEFAULT_GROUP_KEY = "article"
        private const val MAX_RETRY_COUNT = 3
    }
}