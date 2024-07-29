package se.infomaker.iap.appreview

object StatisticsCheckersHolder {

    val fulfilmentCheckers = listOf(
        StatisticsFulfillmentChecker(
            0,
            listOf(
                StatisticsFulfillment("articleRead"),
                StatisticsFulfillment("viewShow", mapOf("viewName" to "articleList"))
            )
        ), StatisticsFulfillmentChecker(
            0,
            listOf(
                StatisticsFulfillment("readerClosed"),
                StatisticsFulfillment("viewShow", mapOf("viewName" to "covers"))
            )
        )
    )
}