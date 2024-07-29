package se.infomaker.livecontentui.section

/**
 * [SectionItem]s also implementing this interface marks them as failable.
 *
 * If an item has failed, meaning [isFailure] returns true, it is supposed to be omitted when
 * presenting [SectionItem]s in the UI.
 *
 * Most notably used to filter [SectionItem]s in [SectionAdapterUpdater.dispatch].
 */
interface FailableSectionItem {

    /**
     * If this returns true, the item should be considered failed and not be presented in any UI.
     */
    val isFailure: Boolean
}