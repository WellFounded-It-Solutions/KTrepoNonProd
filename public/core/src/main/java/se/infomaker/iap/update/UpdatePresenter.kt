package se.infomaker.iap.update

/**
 * A user facing component that relays information about a specific [Update] (that is not
 * [Update.type] == [UpdateType.NONE]) to the user.
 *
 * If an [UpdatePresenter] is active and the [UpdateManager] receives a signal that the
 * [UpdateType] of the application might have changes it is the responsibility of this
 * component to properly handle the this change. That might be as simple as restarting.
 *
 * The [UpdatePresenter] is also responsible for reporting to the [UpdateManager] that it is
 * actually ready to receive changes in [UpdateType] by setting itself as the
 * [UpdateManager.presenter]. There is only room for one presenter at a time.
 */
interface UpdatePresenter {

    /**
     * Called when there is a potential change in the [Update] presented to the user.
     */
    fun present(update: Update)
}