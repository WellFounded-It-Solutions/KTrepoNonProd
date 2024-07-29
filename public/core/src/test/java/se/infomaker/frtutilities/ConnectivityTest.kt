package se.infomaker.frtutilities

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit

class ConnectivityTest {

    private val interval = 2L
    private val duration = 60 / interval

    @Test
    fun retryWithIntervalNoSuccess() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        val testObserver = TestObserver<Boolean>()
        Observable.interval(interval, TimeUnit.SECONDS)
                .take(duration)
                .flatMap {
                    Observable.just(false)
                }
                .filter { it }
                .take(1)
                .subscribe(testObserver)

        testScheduler.advanceTimeBy(60, TimeUnit.SECONDS)
        testObserver.assertComplete()
        testObserver.assertNoValues()
    }

    @Test
    fun retryWithIntervalSuccess() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        val testObserver = TestObserver<Boolean>()
        Observable.interval(interval, TimeUnit.SECONDS)
                .take(duration)
                .flatMap {
                    if (it == 5L) {
                        Observable.just(true)
                    } else {
                        Observable.just(false)
                    }
                }
                .filter { it }
                .take(1)
                .subscribe(testObserver)

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)
        testObserver.assertNotComplete()
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        testObserver.assertValue(true)
        testObserver.assertComplete()
    }
}