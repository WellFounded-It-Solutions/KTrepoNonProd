package se.infomaker.iap.appreview

import se.infomaker.frt.statistics.StatisticsEvent


class StatisticsFulfillmentChecker(
    private var progress: Int,
    private val fulfillments: List<StatisticsFulfillment>
) {

    fun updateAndCheck(statisticsEvent: StatisticsEvent): Boolean {
        if (fulfillments.isEmpty()) {
            return true
        }

        if (fulfillments[progress].isFulfilledBy(statisticsEvent)) {
            if (++progress == fulfillments.size) {
                progress = 0
                return true
            }
        }
        return false
    }
}

class StatisticsFulfillment(
    private val eventName: String,
    private val matches: Map<String, String>? = null
) {

    fun isFulfilledBy(event: StatisticsEvent): Boolean {
        if (event.eventName != eventName) {
            return false
        }

        matches?.forEach { (key, value) ->
            if (event.attributes[key] != value) {
                return false
            }
        }
        return true
    }
}