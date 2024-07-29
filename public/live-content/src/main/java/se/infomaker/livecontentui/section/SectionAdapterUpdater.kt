package se.infomaker.livecontentui.section

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.Observable
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.livecontentui.section.adapter.SectionAdapter
import se.infomaker.livecontentui.section.supplementary.ExpandableSectionItem
import java.util.Date
import java.util.HashMap
import java.util.concurrent.Executors

/**
 * Make sure diffs are calculated on a background thread and dispatched in correct order
 */
class SectionAdapterUpdater(private val sections: MutableList<Section>, private val resourceManager: ResourceManager, private val adapter: SectionAdapter) {

    private var currentSectionItems: List<SectionItem>? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    fun updateExpandableSectionState(listUuid: String) {
        val expandableSection = currentSectionItems.getExpandableSection(listUuid)
        if (adapter.expandableSectionTracker.expandedLists.contains(listUuid)) {
            adapter.expandableSectionTracker.expandedLists.remove(listUuid)
        } else {
            adapter.expandableSectionTracker.expandedLists.add(listUuid)
        }
        dispatch((currentSectionItems ?: emptyList()).filterNot { expandableSection?.sectionItems?.contains(it) ?: false }, getLastUpdated())
    }

    fun collapseAllExpandableSections() {
        adapter.expandableSectionTracker.expandedLists.clear()
    }

    private fun getLastUpdated(): Date? {
        var out: Date? = null
        for (section in sections) {
            val lastUpdated = section.lastUpdated()
            if (lastUpdated != null && (out == null || out.before(lastUpdated))) {
                out = lastUpdated
            }
        }
        return out
    }

    fun dispatch(sectionItems: List<SectionItem?>, lastUpdated: Date?) {

        executor.execute {
            val nonNullSuccessfulSectionItems = mutableListOf<SectionItem>().also { successItems ->
                sectionItems.filterNotNull().filterNot {
                    (it as? FailableSectionItem)?.isFailure == true
                }.forEach { sectionItem ->
                    if (sectionItem is ExpandableSectionItem) {
                        sectionItem.expanded = adapter.expandableSectionTracker.expandedLists.contains(sectionItem.listUuid)
                        successItems.addAll((sectionItem.getAllItems()).filterNot { item -> successItems.contains(item) } )
                    }else{
                        successItems.add(sectionItem)
                    }
                }
            }
            /**
             * Note that this thread is the only one that should mutate currentSectionItems
             */

            val reverseMap = createReverseMap(nonNullSuccessfulSectionItems.mapNotNull { it.template() }.toSet())
            val templateToLayoutMap = reverseKeyValue(reverseMap)
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return currentSectionItems?.size ?: 0
                }

                override fun getNewListSize(): Int {
                    return nonNullSuccessfulSectionItems.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val old = currentSectionItems!![oldItemPosition]
                    val newItem = nonNullSuccessfulSectionItems[newItemPosition]
                    return old.isItemTheSame(newItem)
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return currentSectionItems!![oldItemPosition].areContentsTheSame(nonNullSuccessfulSectionItems[newItemPosition])
                }
            })
            /**
             * Next calculation start the diff from this list of items
             */
            currentSectionItems = nonNullSuccessfulSectionItems.toList()

            mainThreadHandler.post {
                adapter.update(SectionAdapterData(nonNullSuccessfulSectionItems, reverseMap, templateToLayoutMap, diffResult, getMinimumSectionState(), lastUpdated))
            }
        }
    }

    private fun getMinimumSectionState(): SectionState? {
        return try {
            Observable.fromIterable(sections).map { section -> section.observeState().blockingFirst() }
                    .reduce { sectionState, second -> if (sectionState.ordinal > second.ordinal) sectionState else second }
                    .blockingGet()
        } catch (exception: Exception) {
            SectionState.READY
        }
    }

    private fun createReverseMap(templates: Set<String>): Map<String, Int> {
        val reverseMap = HashMap<String, Int>()
        for (template in templates) {
            if (!reverseMap.containsKey(template)) {
                val layoutIdentifier = resourceManager.getLayoutIdentifier(template)
                if (layoutIdentifier != 0) {
                    reverseMap[template] = layoutIdentifier
                }
            }
        }
        return reverseMap
    }

    private fun <T,K>reverseKeyValue(templates: Map<T, K>): Map<K,T> {
        val out = mutableMapOf<K,T>()
        for (key in templates.keys) {
            templates[key]?.let {
                out[it] = key
            }
        }
        return out
    }
}

fun List<SectionItem>?.getExpandableSection(uuid:String): ExpandableSectionItem? = this?.filterIsInstance<ExpandableSectionItem>()?.first { it.listUuid == uuid }