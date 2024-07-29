package se.infomaker.livecontentui.section.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import se.infomaker.frtutilities.ktx.requireActivity
import se.infomaker.iap.action.display.openCustomTab
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentmanager.query.MatchFilter
import se.infomaker.livecontentui.livecontentrecyclerview.activity.LiveContentRecyclerviewActivity
import se.infomaker.livecontentui.section.PropertyObjectSectionItem
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.ktx.hasExternalLink
import se.infomaker.livecontentui.section.ktx.isRelated
import se.infomaker.livecontentui.section.supplementary.ListFooterSectionItem
import timber.log.Timber

class DetailActivityOrchestrator(private val moduleId: String, private val moduleTitle: String?, private val extras: Bundle?) {

    fun open(view: View, item: SectionItem) {
        (item as? ListFooterSectionItem)?.let {
            openLinkedPropertyObject(view, item)
            return
        }
        when {
            item.hasExternalLink -> view.context.openExternalLink(item)
            item.isRelated -> SectionItemDetailActivity.open(view.context, item.id, moduleId, moduleTitle, extras)
            else -> SectionDetailPagerActivity.open(view.requireActivity(), moduleId, item.id, moduleTitle, item.sectionIdentifier(), item.groupKey(), extras)
        }
    }

    private fun openLinkedPropertyObject(view: View, item: ListFooterSectionItem) {
        view.context.startActivity(Intent(view.context, LiveContentRecyclerviewActivity::class.java).also { intent ->
            Bundle().also { bundle ->
                extras?.let { bundle.putAll(it) }
                bundle.putString("moduleId", moduleId)
                bundle.putString("moduleTitle", moduleTitle)
                bundle.putString("title", item.propertyObject.name)
                val filter = MatchFilter(item.articleProperty, item.propertyObject.id)
                bundle.putSerializable("queryFilters", ArrayList(listOf(filter)))
                intent.putExtras(bundle)
            }
        })
    }
}

fun Context.openExternalLink(item: SectionItem) =
    (item as? PropertyObjectSectionItem)?.let { openExternalLink(it.propertyObject) }

fun Context.openExternalLink(propertyObject: PropertyObject) {
    val externalLink = try {
        Uri.parse(propertyObject.externalLink)
    }
    catch (e: Exception) {
        Timber.e(e, "Failed to open external link: ${propertyObject.externalLink}")
        return
    }

    try {
        startActivity(Intent(Intent.ACTION_VIEW, externalLink).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
    catch (e: ActivityNotFoundException) {
        openCustomTab(externalLink)
    }
}