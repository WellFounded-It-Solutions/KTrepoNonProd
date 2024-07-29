package se.infomaker.iap.provisioning.backend

import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Creates [Single] using [requestFactory] and set it up to [Single.cache].
 *
 * The request is cached for [ttl] milliseconds and then discarded.
 *
 * If the request errors out, it will be discarded and not replayed when the next caller
 * tries to [get] the replay and instead the [requestFactory] will be invoked to start a new
 * request.
 */
class BackendRequestTimedReplay<T, R>(private val ttl: Long, private val requestFactory: (R?) -> Single<T>) {

    private var timerDisposable: Disposable? = null

    private var cached: Single<T>? = null
        set(value) {
            timerDisposable?.dispose()
            field = value
            field?.let {
                timerDisposable = Single.timer(ttl, TimeUnit.MILLISECONDS)
                        .subscribe { _, _ ->
                            field = null
                        }
            }
        }

    fun get(parameters: R? = null): Single<T> {
        cached?.let {
            return it
        }

        return requestFactory.invoke(parameters)
                .cache()
                .also {
                    cached = it
                }
    }
}
