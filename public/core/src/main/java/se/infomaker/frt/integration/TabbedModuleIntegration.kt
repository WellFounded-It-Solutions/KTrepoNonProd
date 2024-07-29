package se.infomaker.frt.integration

import android.app.Activity
import android.content.Context
import se.infomaker.frt.module.ModuleIntegrationProvider
import se.infomaker.frt.moduleinterface.ModuleIntegration
import se.infomaker.frt.moduleinterface.prefetch.PrefetchWorker
import se.infomaker.frt.moduleinterface.prefetch.Prefetcher
import se.infomaker.frt.remotenotification.NotificationFilter
import se.infomaker.frt.remotenotification.OnRemoteNotificationListener
import se.infomaker.frt.remotenotification.OnRemoteNotificationListenerFactory
import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler
import se.infomaker.frt.ui.fragment.TabbedModuleConfig
import se.infomaker.frtutilities.ConfigManager

class TabbedModuleIntegration(private val context: Context, private val moduleId: String) : ModuleIntegration, OnRemoteNotificationListenerFactory, Prefetcher {

    private val integrations: List<TabIntegrationWrapper>
    private val prefetchWorker: PrefetchWorker
    private val filters: Array<NotificationFilter>

    init {
        val config = ConfigManager.getInstance().getConfig("TabbedModule", moduleId, TabbedModuleConfig::class.java)
        integrations = config.tabs.map { TabIntegrationWrapper(ModuleIntegrationProvider.createModuleIntegration(context, it.module, it.id)) }
        filters = eagerlyCreateFilters()
        prefetchWorker = TabbedModulePrefetchWorker(moduleId, integrations.map { it.integration }.filterIsInstance(Prefetcher::class.java))
    }

    private fun eagerlyCreateFilters(): Array<NotificationFilter> {
        val filters = mutableListOf<NotificationFilter>()
        integrations.map { it.createFilters(context, it.id) }.forEach {
            filters.addAll(it)
        }
        return filters.toTypedArray()
    }

    override fun getId(): String = moduleId

    override fun createFilters(context: Context?, moduleIdentifier: String?): Array<NotificationFilter> {
        return filters
    }

    override fun createNotificationHandler(context: Context?, moduleIdentifier: String?): OnNotificationInteractionHandler {
        return object : OnNotificationInteractionHandler {

            override fun handleOpenNotification(activity: Activity?, notification: MutableMap<String, String>) {
                getHandler(notification)?.handleOpenNotification(activity, notification)
            }

            override fun handleDeleteNotification(context: Context?, notification: MutableMap<String, String>) {
                getHandler(notification)?.handleDeleteNotification(context, notification)
            }

            private fun getHandler(notification: MutableMap<String, String>) : OnNotificationInteractionHandler? {
                return integrations.firstOrNull { it.matches(notification) }?.let {
                    return it.handler ?: it.createNotificationHandler(context, moduleIdentifier)
                }
            }
        }
    }

    override fun create(context: Context?, moduleIdentifier: String?): OnRemoteNotificationListener {
        return OnRemoteNotificationListener { notification ->
            integrations.firstOrNull { it.matches(notification) }?.let {
                (it.listener ?: it.create(context, moduleIdentifier)).onNotification(notification)
            }
        }
    }

    override fun getPrefetchWorker(): PrefetchWorker {
        return prefetchWorker
    }
}