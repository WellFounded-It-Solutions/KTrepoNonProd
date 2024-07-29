package se.infomaker.iap.provisioning.firebase

import android.os.Handler
import android.text.format.DateUtils
import android.text.format.DateUtils.SECOND_IN_MILLIS
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import se.infomaker.iap.provisioning.backend.ProductValidity
import timber.log.Timber

/**
 * Watch a product validity observable and notify when valid product does not exist anymore
 */
class ProductValidityWatcher(val observable: Observable<List<ProductValidity>>, val validProducts: List<String>, val onNoLongerValid: () -> Unit) {

    var timerHandler = Handler()
    var callback : Runnable? = null

    private val garbage = CompositeDisposable()

    /**
     * Start observing
     */
    fun start() {
        if (garbage.size() == 0) {
            cancel()
            garbage.add(observable.distinctUntilChanged().subscribe { list ->
                scheduleLastExpire(list.filter { validProducts.contains(it.name) })
            })
        }
    }

    /**
     * Stop observing
     */
    fun stop() {
        garbage.clear()
        cancel()
    }

    private fun scheduleLastExpire(products: List<ProductValidity>) {
        if (products.isEmpty()) {
            callback?.let {
                timerHandler.post(it)
            }
            return
        }
        cancel()
        val result = products.reduce { acc, productValidity ->
            if (acc.validTo.before(productValidity.validTo)) {
                return@reduce productValidity
            } else {
                return@reduce acc
            }
        }
        val until = result.validTo.time - System.currentTimeMillis()
        callback = Runnable {
            Timber.d("${result.name} expired")
            onNoLongerValid.invoke()
        }.also {
            timerHandler.postDelayed(it, until)
        }
        val timeSpanString = DateUtils.getRelativeTimeSpanString(result.validTo.time, System.currentTimeMillis(), SECOND_IN_MILLIS)
        Timber.d("Scheduling notification, ${result.name} expires in: $timeSpanString")
    }

    private fun cancel() {
        callback?.let {
            timerHandler.removeCallbacks(it)
            callback = null
        }
    }


}