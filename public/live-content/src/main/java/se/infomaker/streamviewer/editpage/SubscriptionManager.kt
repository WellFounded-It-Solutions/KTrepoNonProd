package se.infomaker.streamviewer.editpage

import android.content.Context
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import se.infomaker.frt.remotenotification.PushRegistrationManager
import se.infomaker.frtutilities.connectivity.hasInternetConnection
import se.infomaker.livecontentmanager.query.QueryManager
import se.infomaker.storagemodule.Storage
import se.infomaker.storagemodule.model.Subscription
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.stream.StreamNotificationSettingsHandler
import timber.log.Timber


class SubscriptionManager @AssistedInject constructor(
    private val pushRegistrationManager: PushRegistrationManager,
    private val queryManager: QueryManager,
    @Assisted private val config: FollowConfig
) {

    private val garbage = CompositeDisposable()

    fun subscribeRemote(context: Context, streamId: String, whenDone: (() -> Unit)? = null) {
        if (isConnected(context, "No internet connection, cannot cannot activate subscription")) {
            return
        }

        Storage.getRealm().use {
            it.where(Subscription::class.java).equalTo("uuid", streamId).findFirst()?.run {
                if (!this.remoteStreamId.isNullOrEmpty()) {
                    Timber.d("Subscription already has a remote stream id")
                    return
                }
                it.executeTransaction {
                    Timber.d("Activating subscription with details {uuid=${this.uuid}, name=${this.name}} for remote push notifications")
                    StreamNotificationSettingsHandler.createSubscriptionRemote(config, pushRegistrationManager, queryManager, this, { remoteStreamId ->
                        remoteStreamId?.let {
                            updateRemoteStreamId(streamId, remoteStreamId, whenDone)
                        }
                    })
                }
            }
        }
    }

    private fun isConnected(context: Context, msg: String): Boolean {
        if (!context.hasInternetConnection()) {
            Timber.d(msg)
            return true
        }
        return false
    }

    fun updateRemoteStreamId(streamId: String, remoteStreamId: String, whenDone: (() -> Unit)? = null) {
        Storage.getRealm().use {
            it.where(Subscription::class.java).equalTo("uuid", streamId).findFirst()?.run {
                it.executeTransaction {
                    this.remoteStreamId = remoteStreamId
                    Timber.d("Activated subscription with details {uuid=${this.uuid}, name=${this.name}, remoteStreamId=${this.remoteStreamId}} for remote push notifications")
                    whenDone?.invoke()
                }
            }
        }
    }

    fun unsubscribeRemote(context: Context, streamId: String, whenDone: (() -> Unit)? = null) {
        if (isConnected(context, "No internet connection, cannot cannot deactivate subscription")) {
            return
        }

        Storage.getRealm().use {
            it.where(Subscription::class.java).equalTo("uuid", streamId).findFirst()?.run {
                if (this.remoteStreamId.isNullOrEmpty()) {
                    Timber.d("Remote stream id is missing, cannot cannot deactivate subscription, name=${this.name}")
                    return
                }

                Timber.d("Deactivating subscription with details {uuid=${this.uuid}, name=${this.name}, remoteStreamId=${this.remoteStreamId}} for remote push notifications")
                val nonRealmRemoteStreamId: String? = this.remoteStreamId
                garbage.add(Observable.fromCallable { StreamNotificationSettingsHandler.deleteSubscriptionRemote(config, queryManager, nonRealmRemoteStreamId!!) }
                        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                        .doOnError { e -> Timber.e(e) }
                        .subscribe { removeRemoteStreamId(streamId, whenDone) })
            }
        }
    }

    fun removeRemoteStreamId(streamId: String, whenDone: (() -> Unit)? = null) {
        Storage.getRealm().use {
            it.where(Subscription::class.java).equalTo("uuid", streamId).findFirst()?.run {
                it.executeTransaction {
                    Timber.d("Deactivated subscription with details {uuid=${this.uuid}, name=${this.name}, remoteStreamId=${this.remoteStreamId}} for remote push notifications")
                    this.remoteStreamId = null
                    whenDone?.invoke()
                }
            }
        }
    }

    fun processPendingActivationSubscriptions(context: Context) {
        Timber.d("processPendingActivationSubscriptions")
        Storage.getRealm().use {
            it.where(Subscription::class.java).isNull("remoteStreamId").and().equalTo("pushActivated", true).findAll().let { subs ->
                subs.forEach { sub ->
                    sub.uuid?.let { streamId ->
                        subscribeRemote(context, streamId)
                    }
                }
            }
        }
    }

    fun processPendingDeactivationSubscriptions(context: Context) {
        Timber.d("processPendingDeactivationSubscriptions")
        Storage.getRealm().use {
            it.where(Subscription::class.java).isNotNull("remoteStreamId").and().equalTo("pushActivated", false).findAll().let { subs ->
                subs.forEach { sub ->
                    sub.uuid?.let { streamId ->
                        unsubscribeRemote(context, streamId)
                    }
                }
            }
        }
    }

    fun processPendingSubscriptions(context: Context, whenDone: (() -> Unit)? = null) {
        Timber.d("processPendingSubscriptions")
        processPendingActivationSubscriptions(context)
        processPendingDeactivationSubscriptions(context)
    }

    fun refresh(context: Context, streamId: String, whenDone: (() -> Unit)? = null) {
        if (isConnected(context, "No internet connection, cannot cannot refresh subscription")) {
            return
        }

        Storage.getSubscription(streamId)?.let { sub ->
            val oldRemoteStreamID = sub.remoteStreamId

            if (sub.remoteStreamId.isNullOrEmpty()) {
                Timber.d("Remote stream id is missing, cannot cannot refresh subscription")
                return
            }

            Timber.d("Refreshing subscription with details {uuid=${sub.uuid}, name=${sub.name}, remoteStreamId=${sub.remoteStreamId}} for remote push notifications")
            StreamNotificationSettingsHandler.createSubscriptionRemote(config, pushRegistrationManager, queryManager, sub, { newRemoteStreamId ->
                newRemoteStreamId?.let {
                    updateRemoteStreamId(streamId, newRemoteStreamId) {
                        oldRemoteStreamID?.let {
                            garbage.add(
                                    Observable.fromCallable { StreamNotificationSettingsHandler.deleteSubscriptionRemote(config, queryManager, oldRemoteStreamID) }
                                            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                                            .subscribe { whenDone?.invoke() })
                        }
                    }
                }
            })
        }
    }

    fun clear() {
        garbage.clear()
    }
}