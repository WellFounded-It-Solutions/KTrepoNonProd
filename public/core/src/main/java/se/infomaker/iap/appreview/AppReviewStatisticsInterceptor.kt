package se.infomaker.iap.appreview

import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsEventInterceptor
import se.infomaker.iap.appreview.repository.AppReviewRepository

class AppReviewStatisticsInterceptor(private val repository: AppReviewRepository) :
    StatisticsEventInterceptor {

    override fun onEvent(event: StatisticsEvent): StatisticsEvent {
        if (repository.shouldCheckTriggerPoint()) {
            StatisticsCheckersHolder.fulfilmentCheckers.forEach {
                if (it.updateAndCheck(event)) {
                    repository.triggerPointDetected()
                }
            }
        }
        return event
    }
}