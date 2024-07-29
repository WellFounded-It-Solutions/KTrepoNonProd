package se.infomaker.streamviewer.di

import android.app.Activity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.follow.PropertyObjectFollowItemViewFactory
import se.infomaker.storagemodule.model.Subscription
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.editpage.SubscriptionManager
import se.infomaker.streamviewer.notification.StreamNotificationFilter
import se.infomaker.streamviewer.notification.StreamNotificationListener
import se.infomaker.streamviewer.stream.StreamNotificationSettingsHandler
import se.infomaker.streamviewer.topicpicker.TopicLiveData

@AssistedFactory
interface SubscriptionManagerFactory {
    fun create(config: FollowConfig): SubscriptionManager
}

@AssistedFactory
interface StreamNotificationSettingsHandlerFactory {
    fun create(
        activity: Activity,
        moduleId: String,
        subscription: Subscription
    ): StreamNotificationSettingsHandler
}

@AssistedFactory
interface PropertyObjectFollowItemViewFactoryFactory {
    fun create(template: String): PropertyObjectFollowItemViewFactory
}

@AssistedFactory
interface TopicLiveDataFactory {
    fun create(
        resourceManager: ResourceManager,
        @Assisted("url") url: String?,
        @Assisted("asset") assetPath: String
    ): TopicLiveData
}

@AssistedFactory
interface StreamNotificationFilterFactory {
    fun create(config: FollowConfig): StreamNotificationFilter
}

@AssistedFactory
interface StreamNotificationListenerFactory {
    fun create(moduleId: String): StreamNotificationListener
}