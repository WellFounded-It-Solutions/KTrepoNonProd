package se.infomaker.frtutilities

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import timber.log.Timber

object ForegroundDetector : LifecycleObserver {

    private val relay = PublishRelay.create<Boolean>()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onForeground() {
        Timber.d("App entered foreground.")
        relay.accept(true)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onBackground() {
        Timber.d("App went to the background.")
        relay.accept(false)
    }

    @JvmStatic
    fun observable(): Observable<Boolean> = relay
}