package se.infomaker.streamviewer.stream

import android.app.Activity
import android.graphics.Color
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.snackbar.Snackbar
import se.infomaker.storagemodule.Storage
import se.infomaker.storagemodule.model.Subscription
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.streamviewer.StatsHelper
import se.infomaker.streamviewer.di.StreamNotificationSettingsHandlerFactory

open class SubscriptionMenuHandler(
    val view: View,
    val activity: Activity,
    val moduleIdentifier: String,
    val subscription: Subscription,
    val notificationItem: MenuItem,
    val notificationIcon: ImageView?,
    val viewName: String,
    private val settingsHandlerFactory: StreamNotificationSettingsHandlerFactory
) : PopupMenu.OnMenuItemClickListener {
    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                val restoreSubscription = Storage.getPersistRealm().copyFromRealm(subscription)
                StatsHelper.logSubscriptionEvent(moduleIdentifier, StatsHelper.DELETE_STREAM_EVENT, viewName, subscription)
                Storage.delete(subscription)

                Snackbar.make(view, activity.getString(R.string.deleted_stream), Snackbar.LENGTH_LONG)
                        .setAction(activity.getString(R.string.undo)) {
                            Storage.addOrUpdateSubscription(restoreSubscription)
                        }
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(snackbar: Snackbar, @DismissEvent event: Int) {
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                    settingsHandlerFactory.create(activity, moduleIdentifier, restoreSubscription).streamDeleted()
                                }
                            }
                        })
                        .setActionTextColor(Color.RED)
                        .show()

                return true
            }
            R.id.pushSettings -> {
                Storage.getRealm().use {
                    it.executeTransaction {
                        subscription.pushActivated = subscription.pushActivated?.not()
                    }
                }
                val streamNotificationSettingsHandler = settingsHandlerFactory.create(activity, moduleIdentifier, subscription)
                streamNotificationSettingsHandler.onSettingsChanged(view, notificationItem, viewName, notificationIcon)
                return true
            }
            else -> return false
        }
    }
}