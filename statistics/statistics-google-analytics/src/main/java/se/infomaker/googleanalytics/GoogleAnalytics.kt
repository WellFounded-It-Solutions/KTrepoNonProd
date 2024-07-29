package se.infomaker.googleanalytics

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import se.infomaker.googleanalytics.dispatcher.Dispatcher
import se.infomaker.googleanalytics.register.Hit
import se.infomaker.googleanalytics.register.HitDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAnalytics @Inject constructor(
    private val dispatcher: Dispatcher,
    private val hitDao: HitDao
) {
    val globalValues = mutableMapOf<String, String>()
    private val relay = PublishRelay.create<Hit>()
    private val disposable: Disposable

    init {
        disposable = relay.observeOn(Schedulers.newThread())
                .subscribe {
                    hitDao.insert(it)
                    dispatcher.wakeUp()
                }
    }

    internal fun addHit(hit: Hit) {
        relay.accept(hit)
    }
}