package se.infomaker.streamviewer.stream

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.json.JSONObject
import se.infomaker.frt.remotenotification.PushRegistrationManager
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.livecontentmanager.query.CreateStreamQuery
import se.infomaker.livecontentmanager.query.DeleteStreamQuery
import se.infomaker.livecontentmanager.query.Query
import se.infomaker.livecontentmanager.query.QueryManager
import se.infomaker.livecontentmanager.query.QueryResponseListener
import se.infomaker.livecontentmanager.query.StreamDestination
import se.infomaker.livecontentmanager.query.UpdateStreamQuery
import se.infomaker.storagemodule.Storage
import se.infomaker.storagemodule.model.Subscription
import se.infomaker.storagemodule.model.SubscriptionState
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frt.remotenotification.PushMeta
import se.infomaker.streamviewer.StatsHelper
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.di.SubscriptionManagerFactory
import timber.log.Timber

class StreamNotificationSettingsHandler @AssistedInject constructor(
    private val pushRegistrationManager: PushRegistrationManager,
    private val queryManager: QueryManager,
    subscriptionManagerFactory: SubscriptionManagerFactory,
    @Assisted private val activity: Activity,
    @Assisted val moduleIdentifier: String,
    @Assisted val subscription: Subscription
) {
    private val config: FollowConfig = ConfigManager.getInstance(activity).getConfig(moduleIdentifier, FollowConfig::class.java)
    private val resourceManager by activity.resources { moduleIdentifier }
    private val subscriptionManager = subscriptionManagerFactory.create(config)

    fun streamDeleted() {
        if (subscription.remoteStreamId != null) {
            postQuery(createDeleteStreamQuery())
        }
    }

    fun updateSubscriptionQuestion() {
        //TODO: Use StreamUpdate instead of deleting and creating a new question
        postQuery(createDeleteStreamQuery())
        postQuery(createSubscriptionQuestion())
//        postQuery(createUpdateQuestion())
    }

    fun createSubscriptionQuestion(): CreateStreamQuery {
        val filter = SubscriptionUtil.createFilter(subscription, config)
        val destinationBuilder = pushRegistrationManager.pushMeta?.streamDestinationBuilder() ?: StreamDestination.builder()
        destinationBuilder.setProperties(config.remoteNotification.properties())
        val filters = listOf(filter)
        return CreateStreamQuery(config.liveContent.stream, filters, destinationBuilder.build())
    }

    fun createUpdateQuestion(): UpdateStreamQuery {
        val filter = SubscriptionUtil.createFilter(subscription, config)
        return UpdateStreamQuery(subscription.remoteStreamId, config.liveContent.stream, listOf(filter))
    }

    private fun createDeleteStreamQuery() = DeleteStreamQuery(config.liveContent.stream.contentProvider, subscription.remoteStreamId)

    fun onSettingsChanged(view: View, item: MenuItem?, viewName: String, notificationIcon: ImageView?, onComplete: (() -> Unit)? = null) {

        if (pushRegistrationManager.pushMeta == null) {
            return
        }

        when (subscription.state) {

            SubscriptionState.ACTIVATED -> {
                subscription.uuid?.let {
                    StatsHelper.logSubscriptionEvent(moduleIdentifier, StatsHelper.NOTIFICATIONS_ACTIVATED_EVENT, viewName, subscription)
                    showSnackbar(view, resourceManager.getString("notifications_activated", ""))
                }
            }

            SubscriptionState.DEACTIVATED -> {
                subscription.uuid?.let {
                    StatsHelper.logSubscriptionEvent(moduleIdentifier, StatsHelper.NOTIFICATIONS_DEACTIVATED_EVENT, viewName, subscription)
                    showSnackbar(view, resourceManager.getString("notifications_deactivated", ""))
                }
            }

            SubscriptionState.PENDING_DEACTIVATION -> {
                subscription.uuid?.let {
                    subscriptionManager.unsubscribeRemote(view.context, it, onComplete)
                    showSnackbar(view, resourceManager.getString("notifications_deactivated", ""))
                }
            }

            SubscriptionState.PENDING_ACTIVATION -> {
                subscription.uuid?.let {
                    subscriptionManager.subscribeRemote(view.context, it, onComplete)
                    showSnackbar(view, resourceManager.getString("notifications_activated", ""))
                }
            }
        }
        notificationIcon?.setImageResource(pushIcon(if (subscription.state == SubscriptionState.ACTIVATED || subscription.state == SubscriptionState.PENDING_ACTIVATION) PushIconState.ENABLED else PushIconState.DISABLED))
        if (item != null) {
            item.isChecked = !item.isChecked
        }
    }

    private fun showSnackbar(view: View, message: String){
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    fun initialActivate(context: Context? = null) {
        if (subscription.pushActivated == true) {
            context?.let { c ->
                subscription.uuid?.let { streamId ->
                    subscriptionManager.subscribeRemote(c, streamId)
                }
            }
        }
    }

    private fun postQuery(query: Query, onComplete: (() -> Unit)? = null) {
        queryManager.addQuery(query, object : QueryResponseListener {
            override fun onResponse(query: Query?, response: JSONObject?) {
                activity.runOnUiThread {
                    val subscription = if (!subscription.isValid) Storage.getSubscription(subscription.uuid!!) else subscription
                    val action = JSONUtil.optString(response, "payload.action")
                    if (!TextUtils.isEmpty(action)) {
                        when (action) {
                            "streamCreated" -> {
                                subscription?.uuid?.let {
                                    subscriptionManager.updateRemoteStreamId(it, JSONUtil.getString(response, "payload.data.streamId"), onComplete)
                                }
                            }
                            "streamDeleted" -> {
                                subscription?.uuid?.let {
                                    subscriptionManager.removeRemoteStreamId(it, onComplete)
                                }
                            }
                            else -> Timber.w("Unexpected response %s", response)
                        }
                    } else {
                        subscription?.uuid?.let {
                            subscriptionManager.removeRemoteStreamId(it, onComplete)
                        }
                    }
                }
            }

            override fun onError(exception: Throwable?) {
                Timber.d(exception, "Could not create stream")
            }
        })
    }

    companion object {

        fun pushIcon(state: PushIconState): Int {
            return when (state) {
                PushIconState.ENABLED -> R.drawable.bell_filled
                PushIconState.DISABLED -> R.drawable.bell_no_fill
            }
        }

        @JvmStatic
        @JvmOverloads
        fun deleteSubscriptionRemote(config: FollowConfig, queryManager: QueryManager, remoteStreamId: String, onSuccess: (() -> Unit)? = null, onError: (() -> Unit)? = null) {
            val query = DeleteStreamQuery(config.liveContent.stream.contentProvider, remoteStreamId)

            queryManager.addQuery(query, object : QueryResponseListener {
                override fun onResponse(query: Query?, response: JSONObject?) {
                    onSuccess?.invoke()
                }

                override fun onError(exception: Throwable?) {
                    Timber.e(exception, "Could not delete stream")
                    onError?.invoke()
                }
            })
        }

        fun createSubscriptionRemote(config: FollowConfig, pushRegistrationManager: PushRegistrationManager, queryManager: QueryManager, subscription: Subscription, onSuccess: ((remoteStreamId: String?) -> Unit)? = null, onError: (() -> Unit)? = null) {
            val filter = SubscriptionUtil.createFilter(subscription, config)
            val destinationBuilder = pushRegistrationManager.pushMeta?.streamDestinationBuilder() ?: StreamDestination.builder()
            destinationBuilder.setProperties(config.remoteNotification.properties())
            val filters = listOf(filter)
            val query = CreateStreamQuery(config.liveContent.stream, filters, destinationBuilder.build())

            subscription.uuid?.let {
                queryManager.addQuery(query, object : QueryResponseListener {
                    override fun onResponse(query: Query?, response: JSONObject?) {
                        val action = JSONUtil.optString(response, "payload.action")
                        if (!TextUtils.isEmpty(action)) {
                            when (action) {
                                "streamCreated" -> {
                                    val remoteStreamId = JSONUtil.getString(response, "payload.data.streamId")
                                    Timber.d(">>>--->>>--->>> remoteStreamId=$remoteStreamId")
                                    onSuccess?.invoke(remoteStreamId)
                                }
                                else -> Timber.w("Unexpected response %s", response)
                            }
                        }
                    }

                    override fun onError(exception: Throwable?) {
                        Timber.e(exception, "Could not create stream")
                        onError?.invoke()
                    }
                })
            }
        }
    }
}

private fun PushMeta.streamDestinationBuilder() =
    StreamDestination.builder()
        .setType(type)
        .setPlatform(platform)
        .setArn(arn)
        .setPushTTL(ttl)
        .setAppId(appId)
        .setToken(token)

enum class PushIconState {
    ENABLED,
    DISABLED
}