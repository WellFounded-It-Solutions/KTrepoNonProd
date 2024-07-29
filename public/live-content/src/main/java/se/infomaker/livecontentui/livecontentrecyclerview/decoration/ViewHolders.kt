package se.infomaker.livecontentui.livecontentrecyclerview.decoration

import androidx.recyclerview.widget.RecyclerView
import se.infomaker.livecontentui.extensions.isRelated
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.LiveContentRecyclerViewAdapter
import se.infomaker.livecontentui.section.Expandable
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder
import se.infomaker.livecontentui.section.datasource.newspackage.ArticleSectionItem
import se.infomaker.livecontentui.section.ktx.isRelated
import se.infomaker.livecontentui.section.supplementary.SupplementarySectionItem


internal val RecyclerView.ViewHolder.holdsRelatedView: Boolean
    get() = when(this) {
        is SectionItemViewHolder -> item?.isRelated == true
        is LiveContentRecyclerViewAdapter.ViewHolder -> propertyObject?.isRelated == true
        else -> false
    }

internal val RecyclerView.ViewHolder.holdsFooterView: Boolean
    get() = when(this) {
        is SectionItemViewHolder -> (item as? SupplementarySectionItem)?.context?.optString("supplementary") == "footer" || item is Expandable
        else -> false
    }

internal val RecyclerView.ViewHolder.holdsHeaderView: Boolean
    get() = when(this) {
        is SectionItemViewHolder -> (item as? SupplementarySectionItem)?.context?.optString("supplementary") == "header"
        else -> false
    }

internal val RecyclerView.ViewHolder.allowsSeparator: Boolean
    get() = when(this) {
        is SectionItemViewHolder -> item is ArticleSectionItem
        is LiveContentRecyclerViewAdapter.ViewHolder -> propertyObject != null
        else -> false
    }