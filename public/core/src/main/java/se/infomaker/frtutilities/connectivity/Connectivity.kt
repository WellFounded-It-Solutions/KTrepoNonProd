package se.infomaker.frtutilities.connectivity

import android.content.Context
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object Connectivity {
    private val garbage = CompositeDisposable()
    private val relay = BehaviorRelay.create<Boolean>()
    private const val interval = 2L
    private const val duration = 60 / interval

    internal fun init(context: Context) {
        val connectivitySource = ConnectivitySourceFactory.create(context)
        Flowable.create(connectivitySource, BackpressureStrategy.BUFFER)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map {
                    try {
                        context.hasInternetConnectionOrThrow()
                    } catch (t: Throwable) {
                        /*
                        An error here indicates that the connectivitySource has a null
                        activeNetwork object which can occur when resuming from an
                        hibernated state.  Ergo, emit false to reflect this and call
                        checkConnectivity which will then recheck whether there's an
                        activeNetwork object and update the relay.
                        */
                        checkConnectivity(context)
                        false
                    }
                }
                .subscribe { relay.accept(it) }
                .addTo(garbage)
    }

    private fun checkConnectivity(context: Context) {
        val disposable = Observable.interval(interval, TimeUnit.SECONDS)
                .take(duration)
                .flatMap { Observable.just(context.hasInternetConnection()) }
                .filter { it }
                .take(1)
                .subscribeOn(Schedulers.computation())
                .subscribe {
                    relay.accept(it)
                }
    }

    @JvmStatic
    fun observable(): Observable<Boolean> {
        return relay.distinctUntilChanged()
    }
}

private fun Disposable.addTo(composite: CompositeDisposable) {
    composite.add(this)
}