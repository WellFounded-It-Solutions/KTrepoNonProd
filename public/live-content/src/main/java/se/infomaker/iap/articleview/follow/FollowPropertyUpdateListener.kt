package se.infomaker.iap.articleview.follow

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.realm.Realm
import io.realm.RealmChangeListener
import se.infomaker.iap.articleview.OnPrepareView
import se.infomaker.iap.articleview.item.Item
import se.infomaker.storagemodule.Storage
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.streamviewer.stream.SubscriptionUtil

class FollowPropertyUpdateListener(val item: FollowPropertyObjectItem) : RealmChangeListener<Realm>, OnPrepareView, LifecycleObserver {

    var view: FollowCompoundView? = null

    override fun onChange(t: Realm) {
        onUpdate()
    }

    override fun onPreHeat(item: Item, context: Context) {}

    override fun onPrepare(item: Item, view: View) {
        this.view = view.findViewById(R.id.follow)
        Storage.getPersistRealm().addChangeListener(this)
    }

    override fun onPrepareCancel(item: Item, view: View) {
        this.view = null
        Storage.getPersistRealm().removeChangeListener(this)
    }

    private fun onUpdate() {
        view?.following = SubscriptionUtil.hasMatchSubscription(item.articleProperty, item.value)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun destroy() {
        Storage.getPersistRealm().removeChangeListener(this)
    }
}