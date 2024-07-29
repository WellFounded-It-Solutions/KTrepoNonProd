package se.infomaker.livecontentui.section.supplementary

import android.view.View
import androidx.fragment.app.Fragment
import com.navigaglobal.mobile.livecontent.R
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.author.DividerDecorationConfig
import se.infomaker.iap.theme.Theme
import se.infomaker.livecontentmanager.config.SublistConfig
import se.infomaker.livecontentui.extensions.copy
import se.infomaker.livecontentui.extensions.patch
import se.infomaker.livecontentui.impressions.ContentTracker
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding
import se.infomaker.livecontentui.section.ContentPresentationAware
import se.infomaker.livecontentui.section.Expandable
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder


class ExpandableSectionItem(
    private val sublistConfig: SublistConfig,
    val listUuid: String,
    val sectionIdentifier: String,
    val sectionItems: List<SectionItem>,
    _context: JSONObject?
) : SectionItem, ContentPresentationAware, Expandable {

    override var context: JSONObject? = _context?.copy()?.patch(ITEM_CONTEXT) ?: ITEM_CONTEXT
    override val content: JSONObject? = null

    private val key: String = listUuid

    override var expanded: Boolean = false

    override fun getId() = key

    override fun sectionIdentifier(): String = sectionIdentifier

    fun getAllItems(): List<SectionItem> {
        sublistConfig.collapseIfMoreThan?.let {
            if (sectionItems.size > it) {
                return if (expanded) if (sublistConfig.collapsible) sectionItems + this else sectionItems else sectionItems.take(sublistConfig.defaultNumVisibleItems ?: it) + this
            }
        }
        return sectionItems
    }

    fun getArticleItems(): List<SectionItem> {
        sublistConfig.collapseIfMoreThan?.let {
            if (sectionItems.size > it) {
                return if (expanded) sectionItems else sectionItems.take(sublistConfig.defaultNumVisibleItems ?: it)
            }
        }
        return sectionItems
    }

    override fun bind(binder: PropertyBinder, viewHolder: SectionItemViewHolder, resourceManager: ResourceManager, theme: Theme): MutableSet<LiveBinding>? {
        viewHolder.item = this
        viewHolder.getView<View>("expanded_button")?.visibility = if (expanded) View.VISIBLE else View.GONE
        viewHolder.getView<View>("collapsed_button")?.visibility = if (expanded) View.GONE else View.VISIBLE
        theme.apply(viewHolder.itemView)
        return null
    }

    fun updateView(view: View) {
        view.findViewById<View>(R.id.expanded_button)?.visibility = if (expanded) View.VISIBLE else View.GONE
        view.findViewById<View>(R.id.collapsed_button)?.visibility = if (expanded) View.GONE else View.VISIBLE
    }

    override fun isItemTheSame(sectionItem: SectionItem?): Boolean {
        (sectionItem as? Expandable)?.let {
            if (id == sectionItem.id  && expanded == sectionItem.expanded) {
                return true
            }
        }
        return false
    }

    override fun areContentsTheSame(sectionItem: SectionItem?): Boolean {
        (sectionItem as? Expandable)?.let {
            if (id == sectionItem.id && expanded == sectionItem.expanded) {
                return true
            }
        }
        return false
    }

    override fun onDetach(viewHolder: SectionItemViewHolder?) {}

    override fun template(): String = "default_list_expand_button"

    override fun templateReference(): String = "default_list_expand_button"

    override fun defaultTemplate(): Int = R.layout.default_list_expand_button

    override fun isClickable(): Boolean = true

    override fun groupKey(): String = "expandable_section_item"

    override fun overlayThemes(): MutableList<String> = emptyList<String>().toMutableList()

    override fun getDividerConfig(): DividerDecorationConfig = SectionItem.NO_DIVIDER_CONFIG

    override fun createDetailView(moduleId: String?): Fragment? = null

    override fun getContentTracker(moduleId: String?): ContentTracker? = null

    companion object{
        private val ITEM_CONTEXT
            get() = JSONObject().apply {
                put("supplementary", "expandable_section")
            }
    }
}