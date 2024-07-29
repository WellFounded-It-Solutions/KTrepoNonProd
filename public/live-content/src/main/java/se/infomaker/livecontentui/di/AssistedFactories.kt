package se.infomaker.livecontentui.di

import dagger.assisted.AssistedFactory
import se.infomaker.livecontentui.livecontentrecyclerview.notification.ContentListNotificationListener
import se.infomaker.livecontentui.section.configuration.SectionedLiveContentUIConfig
import se.infomaker.livecontentui.section.datasource.DataSourceProvider
import se.infomaker.streamviewer.notification.StreamNotificationListener

@AssistedFactory
interface DataSourceProviderFactory {
    fun create(config: SectionedLiveContentUIConfig): DataSourceProvider
}

@AssistedFactory
interface ContentListNotificationListenerFactory {
    fun create(moduleId: String): ContentListNotificationListener
}