package se.infomaker.livecontentui.section.adapter

import android.content.Context
import android.os.Bundle
import android.view.View
import com.google.gson.JsonObject
import org.json.JSONObject
import se.infomaker.frt.moduleinterface.action.GlobalActionHandler
import se.infomaker.frtutilities.GlobalValueManager
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.Operation
import se.infomaker.livecontentui.bookmark.BookmarkActionBottomSheetFragment
import se.infomaker.livecontentui.bookmark.BookmarkFeatureFlag
import se.infomaker.livecontentui.config.ContentTypeTemplateConfig
import se.infomaker.livecontentui.extensions.findFragmentManager
import se.infomaker.livecontentui.livecontentrecyclerview.view.ViewClick
import se.infomaker.livecontentui.section.ExpandableListTracker
import se.infomaker.livecontentui.section.SectionAdapterUpdater
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.SectionItemWrapper
import se.infomaker.livecontentui.section.datasource.newspackage.ArticleSectionItem
import se.infomaker.livecontentui.section.datasource.newspackage.HorizontalListSectionItem
import se.infomaker.livecontentui.section.datasource.newspackage.PackageSectionItem
import se.infomaker.livecontentui.section.detail.DetailActivityOrchestrator
import se.infomaker.livecontentui.section.supplementary.ExpandableSectionItem
import timber.log.Timber

class SectionItemClickHandler(private val moduleId: String, moduleTitle: String?, extras: Bundle, private val contentViewConfig: JsonObject?, private val updater: SectionAdapterUpdater? = null, private val expandableListTracker: ExpandableListTracker? = null) {

    private val detailActivityOrchestrator = DetailActivityOrchestrator(moduleId, moduleTitle, extras)
    private val actions = mutableMapOf<String, ItemAction>()

    init {
        register(EditionItemAction())
        register(ArticleItemAction())
    }

    fun register(action: ItemAction) {
        actions[action.identifier] = action
    }

    fun onItemClick(view: View, item: SectionItem) {
        when (item) {
            is ExpandableSectionItem -> {
                updater?.updateExpandableSectionState(item.listUuid)
                item.expanded = expandableListTracker?.expandedLists?.contains(item.listUuid) ?: false
                item.updateView(view)
            }
            else -> detailActivityOrchestrator.open(view, item)
        }
    }

    fun onItemLongClick(view: View, item: SectionItem?): Boolean {
        if (BookmarkFeatureFlag.isEnabled(view.context) && item is ArticleSectionItem) {
            view.findFragmentManager()?.let {
                BookmarkActionBottomSheetFragment.newInstance(moduleId, item.propertyObject).show(it, null)
            }
            return true
        }
        return false
    }

    fun onSubItemClick(context: Context, viewClick: ViewClick, item: SectionItem) {
        (item as? SectionItemWrapper)?.let { wrapper ->
            val actionModuleId = wrapper.config?.moduleId ?: moduleId
            // Configured actions will win
            wrapper.config?.actions?.let { actions ->
                actions[viewClick.identifier]?.let {
                    val operation = it.asOperation(actionModuleId, GlobalValueManager.getGlobalValueManager(context))
                    GlobalActionHandler.getInstance().perform(context, operation)
                    return
                }
            }

            actions[viewClick.identifier]?.let {
                val contentId = viewClick.contentId ?: item.id
                it.perform(context, actionModuleId, contentId, item, contentViewConfig)
                return
            }
            Timber.w("Unsupported clickIdentifier: %s", viewClick.identifier)
        }
    }
}

/**
 * Shamelessly stolen from iap-base-android/se.infomaker.iap.action.Operation
 */
private fun ContentTypeTemplateConfig.Action.asOperation(moduleId: String?, valueProvider: ValueProvider?): Operation {
    return Operation(
            action = action,
            moduleID = this.moduleId ?: moduleId,
            parameters = JSONObject(parameters?.toString() ?: "{}"),
            values = valueProvider)
}