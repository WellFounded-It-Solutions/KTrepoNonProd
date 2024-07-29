package se.infomaker.livecontentui.section

/**
 * [SectionItem]s also implementing this interface marks them as expandable.
 *
 * If an item has expanded, meaning [isExpanded] returns true, it is supposed to be omitted when
 * presenting [SectionItem]s in the UI.
 *
 * Most notably used to filter [SectionItem]s in [SectionAdapterUpdater].
 */
interface Expandable {

    /**
     * If this returns true, the item should be considered expanded and should render all subitems it contains.
     */
    val expanded: Boolean

}