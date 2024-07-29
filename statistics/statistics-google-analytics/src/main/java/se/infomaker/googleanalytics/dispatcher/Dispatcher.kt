package se.infomaker.googleanalytics.dispatcher

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import se.infomaker.googleanalytics.register.Hit
import se.infomaker.googleanalytics.register.HitDao
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The dispatcher is kept alive for TIMEOUT and tries to batch send any events periodically
 */
@Singleton
class Dispatcher @Inject constructor(
    private val gaApi: GoogleAnalyticsApi,
    private val dao: HitDao
) {
    companion object {
        val TIMEOUT = TimeUnit.MINUTES.toMillis(5)
    }
    private var last: Long = System.currentTimeMillis()
    private var subscription: Disposable? = null
    private var isSending = false

    private fun start() {
        subscription?.dispose()
        subscription = Observable.interval(0, 1, TimeUnit.MINUTES).subscribeOn(Schedulers.io()).subscribe {
            if (System.currentTimeMillis() - last > TIMEOUT) {
                pause()
            }
            conditionalSend()
        }
    }

    private fun conditionalSend() {
        if (isSending) {
            Timber.d("Already sending")
            return
        }
        val currentHitCount = dao.numberOfHits()
        if (currentHitCount > 0) {

            val nextBatch = dao.nextTwenty()
            Timber.d("Trying to send ${nextBatch.size} hits of total $currentHitCount")
            isSending = true
            val builder = StringBuilder()
            val included = mutableListOf<Hit>()

            for (hit in nextBatch) {
                /*
                 TODO: find a more elegant way of limiting to 16k for the UTF-8 string
                 */
                if (builder.length + hit.queryParams.length > 1024*8) {
                    break
                }

                builder.append(hit.queryParams)
                val qt = System.currentTimeMillis() - hit.queryTime
                builder.append("&qt=$qt\n")
                included.add(hit)
            }
            val payload = builder.toString()
            try {
                val response = gaApi.batch(payload).execute()
                if (response.isSuccessful) {
                    Timber.d("Sent ${included.size} hits")
                    for (hit in included) {
                        dao.delete(hit)
                    }
                    val afterHitCount = dao.numberOfHits()
                    if (afterHitCount > 0) {
                        Timber.d("more hits left to send")
                        isSending = false
                        conditionalSend()
                        return
                    }
                }
                else {
                    Timber.d("Failed to send hits, will retry later")
                }
            }
            catch (e: IOException) {
                Timber.d(e, "Failed to send hits, will retry later")
            }

            isSending = false
        }
    }

    private fun pause() {
        subscription?.dispose()
        subscription = null
    }

    /**
     * Make
     */
    fun wakeUp() {
        last = System.currentTimeMillis()
        if (subscription == null) {
            start()
        }
    }
}