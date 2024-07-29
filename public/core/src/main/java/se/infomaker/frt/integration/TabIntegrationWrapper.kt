package se.infomaker.frt.integration

import android.app.Activity
import android.content.Context
import se.infomaker.frt.moduleinterface.ModuleIntegration
import se.infomaker.frt.remotenotification.NotificationFilter
import se.infomaker.frt.remotenotification.OnRemoteNotificationListener
import se.infomaker.frt.remotenotification.OnRemoteNotificationListenerFactory
import se.infomaker.frt.remotenotification.RemoteNotification
import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler

class TabIntegrationWrapper(val integration: ModuleIntegration): ModuleIntegration, OnRemoteNotificationListenerFactory {

    override fun getId(): String = integration.id

    var filters: Array<NotificationFilter>? = null
    var handler: OnNotificationInteractionHandler? = null
    var listener: OnRemoteNotificationListener? = null

    override fun createFilters(context: Context?, moduleIdentifier: String?): Array<NotificationFilter> {
        filters = (integration as? OnRemoteNotificationListenerFactory)?.createFilters(context, moduleIdentifier)
        return filters ?: emptyArray()
    }

    override fun createNotificationHandler(context: Context?, moduleIdentifier: String?): OnNotificationInteractionHandler {
        handler = (integration as? OnRemoteNotificationListenerFactory)?.createNotificationHandler(context, integration.id)
        return object : OnNotificationInteractionHandler{
            override fun handleOpenNotification(activity: Activity?, notification: MutableMap<String, String>?) {
                handler?.handleOpenNotification(activity, notification)
            }

            override fun handleDeleteNotification(context: Context?, notification: MutableMap<String, String>?) {
                handler?.handleDeleteNotification(context, notification)
            }
        }
    }

    override fun create(context: Context?, moduleIdentifier: String?): OnRemoteNotificationListener {
        listener = (integration as? OnRemoteNotificationListenerFactory)?.create(context, moduleIdentifier)
        return listener ?: OnRemoteNotificationListener { }
    }

    fun matches(notification: Map<String, String>) : Boolean {
        val testNotification = RemoteNotification("foobar", notification)
        return matches(testNotification)
    }

    fun matches(remoteNotification: RemoteNotification) : Boolean {
        filters?.forEach {
            if (it.matches(remoteNotification)) {
                return true
            }
        }
        return false
    }
}