package se.infomaker.frt.integration

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.infomaker.frt.moduleinterface.ModuleIntegration
import se.infomaker.frt.moduleinterface.prefetch.PrefetchWorker
import se.infomaker.frt.moduleinterface.prefetch.Prefetcher
import se.infomaker.frt.remotenotification.NotificationFilter
import se.infomaker.frt.remotenotification.OnRemoteNotificationListener
import se.infomaker.frt.remotenotification.OnRemoteNotificationListenerFactory
import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler
import se.infomaker.livecontentui.LiveContentStreamProvider
import se.infomaker.livecontentui.di.ContentListNotificationListenerFactory
import se.infomaker.livecontentui.livecontentrecyclerview.notification.ContentListNotificationFilter
import se.infomaker.livecontentui.livecontentrecyclerview.notification.ContentListNotificationListener
import se.infomaker.livecontentui.livecontentrecyclerview.notification.OpenContentListNotificationHandler

open class ContentListIntegration(private val context: Context, private val moduleIdentifier: String) : ModuleIntegration, OnRemoteNotificationListenerFactory, Prefetcher {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ContentListIntegrationEntryPoint {
        fun streamProvider(): LiveContentStreamProvider
        fun contentListNotificationListenerFactory(): ContentListNotificationListenerFactory
    }

    override fun getId(): String {
        return moduleIdentifier
    }

    override fun create(context: Context, moduleIdentifier: String): OnRemoteNotificationListener {
        val entryPoint = EntryPointAccessors.fromApplication(context, ContentListIntegrationEntryPoint::class.java)
        return entryPoint.contentListNotificationListenerFactory().create(moduleIdentifier)
    }

    override fun createFilters(context: Context?, moduleIdentifier: String): Array<out NotificationFilter> {
        return arrayOf(ContentListNotificationFilter(moduleIdentifier))
    }

    override fun createNotificationHandler(context: Context, moduleIdentifier: String): OnNotificationInteractionHandler {
        return OpenContentListNotificationHandler(moduleIdentifier)
    }

    override fun getPrefetchWorker(): PrefetchWorker {
        val entryPoint = EntryPointAccessors.fromApplication(context, ContentListIntegrationEntryPoint::class.java)
        return ContentListPrefetcher(entryPoint.streamProvider(), moduleIdentifier)
    }
}