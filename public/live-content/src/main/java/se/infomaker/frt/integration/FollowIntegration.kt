package se.infomaker.frt.integration

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import se.infomaker.frt.moduleinterface.ModuleIconProvider
import se.infomaker.frt.moduleinterface.ModuleIntegration
import se.infomaker.frt.moduleinterface.action.ActionHandler
import se.infomaker.frt.moduleinterface.action.ActionHandlerCollection
import se.infomaker.frt.moduleinterface.action.ActionHandlerRegister
import se.infomaker.frt.moduleinterface.action.GlobalActionHandler
import se.infomaker.frt.remotenotification.NotificationFilter
import se.infomaker.frt.remotenotification.OnRemoteNotificationListener
import se.infomaker.frt.remotenotification.OnRemoteNotificationListenerFactory
import se.infomaker.frt.remotenotification.PushRegistrationManager
import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.findActivity
import se.infomaker.iap.action.ActionManager
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.storagemodule.Storage
import se.infomaker.storagemodule.model.Subscription
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.streamviewer.action.MatchSubscriptionActionHandler
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.di.RemoteNotificationConfigPreferences
import se.infomaker.streamviewer.di.StreamNotificationFilterFactory
import se.infomaker.streamviewer.di.StreamNotificationListenerFactory
import se.infomaker.streamviewer.di.SubscriptionManagerFactory
import se.infomaker.streamviewer.editpage.SubscriptionManager
import se.infomaker.streamviewer.notification.NotificationInteractionHandler
import se.infomaker.streamviewer.notification.StreamNotificationListener
import se.infomaker.streamviewer.topicpicker.TopicPickerActivity
import timber.log.Timber

open class FollowIntegration(private val context: Context, private val id: String) : ModuleIntegration, OnRemoteNotificationListenerFactory, ActionHandlerRegister, ModuleIconProvider {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FollowEntryPoint {
        fun pushRegistrationManager(): PushRegistrationManager
        fun subscriptionManagerFactory(): SubscriptionManagerFactory
        fun streamNotificationFilterFactory(): StreamNotificationFilterFactory
        fun streamNotificationListenerFactory(): StreamNotificationListenerFactory
        fun configManager(): ConfigManager
        fun matchSubscriptionActionHandler(): MatchSubscriptionActionHandler
        @RemoteNotificationConfigPreferences
        fun remoteNotificationConfigPreferences(): SharedPreferences
    }

    private val config = ConfigManager.getInstance().getConfig(id, FollowConfig::class.java)
    private val liveContentConfig: LiveContentConfig
        get() = config.liveContent
    private val configBaseQuery: String
        get() = liveContentConfig.stream.baseQuery.toString()
    private var requiresRefresh = false
    private val integrationScope = CoroutineScope(Job() + Dispatchers.Default)

    private val subscriptionManager: SubscriptionManager
    private val sharedPrefs: SharedPreferences

    private val entryPoint: FollowEntryPoint =
        EntryPointAccessors.fromApplication(context, FollowEntryPoint::class.java)

    override val moduleIcon: Int
        get() = R.drawable.default_near_me_icon

    init {
        integrationScope.launch {
            entryPoint.pushRegistrationManager().registrationChanges().collect {
                Timber.d("DeviceId has changed, refreshing.")
                requiresRefresh = true
                processRemoteSubscriptions(context)
                requiresRefresh = false
            }
        }
        subscriptionManager = entryPoint.subscriptionManagerFactory().create(config)
        sharedPrefs = entryPoint.remoteNotificationConfigPreferences()
        processRemoteSubscriptions(context)
        val topicPickerActionHandler = object : ActionHandler {
            override fun perform(context: Context, operation: Operation): String {
                context.findActivity()?.let { activity ->
                    activity.startActivityForResult(TopicPickerActivity.createIntent(activity, id, null), 1)
                }
                return ""
            }

            override fun canPerform(context: Context, operation: Operation): Boolean {
                return SHOW_TOPIC_PICKER == operation.action || SHOW_NEAR_ME_TOPIC_PICKER == operation.action
            }
        }
        GlobalActionHandler.getInstance().register(SHOW_TOPIC_PICKER, topicPickerActionHandler)
        GlobalActionHandler.getInstance().register(SHOW_NEAR_ME_TOPIC_PICKER, topicPickerActionHandler)
    }

    private fun processRemoteSubscriptions(context: Context) {
        Timber.d("processRemoteSubscriptions")
        subscriptionManager.processPendingDeactivationSubscriptions(context)
        refreshRemoteSubscriptions()
        subscriptionManager.processPendingActivationSubscriptions(context)
    }

    private fun refreshRemoteSubscriptions() {
        Timber.d("refreshRemoteSubscriptions")
        val (fromSharedPref, fromConfig) = getBaseQueries()
        if (shouldUpdateRemoteSubscriptions(fromSharedPref, fromConfig) || requiresRefresh) {
            Timber.d("Remote subscriptions need to be updated")
            Storage.getRealm().use {
                var numOfSubsToRefresh = it.where(Subscription::class.java).isNotNull("remoteStreamId").and().equalTo("pushActivated", true).count()
                it.where(Subscription::class.java).isNotNull("remoteStreamId").and().equalTo("pushActivated", true).findAll().let { subscriptions ->
                    subscriptions.forEach { subscription ->
                        subscription.uuid?.let { streamId ->
                            subscriptionManager.refresh(context, streamId) {
                                if (--numOfSubsToRefresh == 0L) {
                                    Timber.d("onSharedPreferenceChanged")
                                    updateBaseQuery(configBaseQuery)
                                    subscriptionManager.clear()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getBaseQueries(): Pair<String, String> {
        val savedBaseQuery = sharedPrefs.getString(BASE_QUERY_KEY, null) ?: configBaseQuery
        return Pair(savedBaseQuery, configBaseQuery)
    }

    private fun updateBaseQuery(fromConfig: String) {
        Timber.d("Updating BaseQuery")
        sharedPrefs.edit().putString(BASE_QUERY_KEY, fromConfig).apply()
    }

    private fun shouldUpdateRemoteSubscriptions(fromSharedPref: String, fromConfig: String): Boolean {
        Timber.d("shouldUpdateRemoteSubscriptions=${fromSharedPref.trim() != fromConfig.trim()}")
        return fromSharedPref.trim() != fromConfig.trim()
    }

    override fun getId(): String {
        return id
    }

    override fun create(context: Context, moduleIdentifier: String): OnRemoteNotificationListener {
        return entryPoint.streamNotificationListenerFactory().create(moduleIdentifier)
    }

    override fun createFilters(context: Context, moduleIdentifier: String): Array<NotificationFilter> {
        val filterFactory = entryPoint.streamNotificationFilterFactory()
        val config = entryPoint.configManager().getConfig(moduleIdentifier, FollowConfig::class.java)
        return arrayOf(filterFactory.create(config))
    }

    override fun createNotificationHandler(context: Context, moduleIdentifier: String): OnNotificationInteractionHandler {
        return NotificationInteractionHandler(moduleIdentifier)
    }

    override fun registerActions(context: Context, actionHandler: ActionHandlerCollection) {
        val handler = entryPoint.matchSubscriptionActionHandler()
        actionHandler.register(MatchSubscriptionActionHandler.SHOW_MATCH_SUBSCRIPTION_ACTION, handler)
        actionHandler.register(MatchSubscriptionActionHandler.ADD_MATCH_SUBSCRIPTION_ACTION, handler)
        actionHandler.register(MatchSubscriptionActionHandler.REMOVE_MATCH_SUBSCRIPTION_ACTION, handler)
        ActionManager.register("show-topic-picker", object : se.infomaker.iap.action.ActionHandler {
            override fun isLongRunning(): Boolean {
                return false
            }

            override fun perform(context: Context, operation: Operation, onResult: Function1<Result, Unit>) {
                val intent = TopicPickerActivity.createIntent(context, operation.moduleID!!, null)
                context.startActivity(intent)
                onResult.invoke(Result(true, operation.values, null))
            }

            override fun canPerform(context: Context, operation: Operation): Boolean {
                return true
            }
        })
    }

    companion object {
        const val SHOW_NEAR_ME_TOPIC_PICKER = "showNearMeTopicPicker"
        const val SHOW_TOPIC_PICKER = "showFollowTopicPicker"
        private const val BASE_QUERY_KEY = "LiveContentConfig.BaseQuery"
    }
}