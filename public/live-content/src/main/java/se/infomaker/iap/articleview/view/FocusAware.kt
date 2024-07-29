package se.infomaker.iap.articleview.view

/**
 * An [se.infomaker.iap.articleview.item.Item] that also implements this interface can have its
 * current [focusState] mutated to relay the fact that it is currently not visible to the user.
 */
interface FocusAware {

    /**
     * The current [FocusState] of this component.
     */
    var focusState: FocusState
}
