package se.infomaker.iap.articleview.theme

import se.infomaker.iap.theme.OnThemeUpdateListener

/**
 * A theme that might change over time. Exposes methods to add or remove [OnThemeUpdateListener]s
 * which are used to notify interested components when such a change occurs.
 *
 * It is the responsibility of the implementation to keep track of all the listeners
 * and notify all of them.
 *
 * It is the responsibility of the caller of [addOnUpdateListener] to call [removeOnUpdateListener]
 * before its lifecycle is over to avoid leaking the listener object if necessary.
 */
interface UpdatableTheme {

    /**
     * Adds a listener to receive the [OnThemeUpdateListener.onThemeUpdated] callback.
     */
    fun addOnUpdateListener(listener: OnThemeUpdateListener)

    /**
     * Remove listener to stop receiving the [OnThemeUpdateListener.onThemeUpdated] callback.
     */
    fun removeOnUpdateListener(listener: OnThemeUpdateListener)
}