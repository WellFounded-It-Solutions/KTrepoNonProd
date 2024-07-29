package se.infomaker.livecontentui.section.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.gson.JsonObject
import se.infomaker.frtutilities.ktx.requireActivity
import se.infomaker.iap.action.display.flow.mustachify
import se.infomaker.livecontentui.livecontentrecyclerview.fragment.PropertyObjectValueProvider
import se.infomaker.livecontentui.section.PropertyObjectSectionItem
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.SectionedLiveContentActivity
import se.infomaker.livecontentui.section.datasource.list.ListDataProvider
import se.infomaker.livecontentui.section.datasource.newspackage.HorizontalListSectionItem
import se.infomaker.livecontentui.section.datasource.newspackage.PackageDataProvider
import se.infomaker.livecontentui.section.datasource.newspackage.PackageSectionItem
import se.infomaker.livecontentui.section.detail.SectionDetailPagerActivity

interface ItemAction {
    val identifier: String
    fun perform(context: Context, moduleId: String, contentId: String, item: SectionItem, contentViewConfig: JsonObject?)
}

class EditionItemAction : ItemAction {
    override val identifier = "openEdition"

    override fun perform(context: Context, moduleId: String, contentId: String, item: SectionItem, contentViewConfig: JsonObject?) {
        val intent = Intent(context, SectionedLiveContentActivity::class.java)
        val bundle = Bundle()
        bundle.putString("moduleId", moduleId)

        contentViewConfig?.let { config ->
            (item as? PropertyObjectSectionItem)?.let {
                val overlay = config.toString().mustachify(PropertyObjectValueProvider(it.propertyObject))
                intent.putExtra("configOverlay", overlay)
            }
        }

        intent.putExtras(bundle)
        context.startActivity(intent)
    }
}

class ArticleItemAction : ItemAction {
    override val identifier = "openArticle"

    override fun perform(context: Context, moduleId: String, contentId: String, item: SectionItem, contentViewConfig: JsonObject?) {
        val articleExtras = Bundle()
        articleExtras.putString("moduleId", moduleId)

        var groupKey = ListDataProvider.DEFAULT_GROUP_KEY
        if (item is HorizontalListSectionItem) {
            articleExtras.putString("listUuid", item.id)
            groupKey = item.groupKey() ?: ListDataProvider.DEFAULT_GROUP_KEY
        }
        if (item is PackageSectionItem) {
            articleExtras.putString("packageUuid", item.id)
            articleExtras.putBoolean(SectionDetailPagerActivity.IGNORE_SECTION_IDENTIFIER, true)
        }

        SectionDetailPagerActivity.open(context.requireActivity(), moduleId, contentId, null, item.sectionIdentifier(), groupKey, articleExtras)
    }
}