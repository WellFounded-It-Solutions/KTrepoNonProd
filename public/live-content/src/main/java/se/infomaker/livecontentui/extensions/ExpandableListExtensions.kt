@file:JvmName("ExpandableListUtils")
package se.infomaker.livecontentui.extensions

import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.supplementary.ExpandableSectionItem

val List<SectionItem>.containsExpandableSectionItems: Boolean
    get() = this.filterIsInstance<ExpandableSectionItem>().isNotEmpty()

val List<SectionItem>.allItems: List<SectionItem>
    get() {
        if (this.containsExpandableSectionItems) {
            val items = mutableListOf<SectionItem>()
            this.forEach {
                if (it is ExpandableSectionItem) {
                    items.addAll(it.getArticleItems())
                } else {
                    items.add(it)
                }
            }
            return items
        }
        return this
    }