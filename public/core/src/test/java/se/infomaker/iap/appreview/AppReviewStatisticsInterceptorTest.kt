package se.infomaker.iap.appreview

import org.junit.Assert.assertEquals
import org.junit.Test
import se.infomaker.frt.statistics.StatisticsEvent


class AppReviewStatisticsInterceptorTest {

    private var articleReadEvent:StatisticsEvent
    private var articleListEvent:StatisticsEvent

    init {
        val statsBuilder = StatisticsEvent.Builder()
        statsBuilder.event("viewShow")
        statsBuilder.moduleId("ModuleId")
        statsBuilder.moduleName("ModuleName")
        statsBuilder.moduleTitle("ModuleTitle")
        statsBuilder.attribute("viewName", "articleList")
        articleListEvent = statsBuilder.build()

        val statsBuilder1 = StatisticsEvent.Builder()
        statsBuilder1.event("articleRead")
        statsBuilder1.moduleId("ModuleId")
        statsBuilder1.moduleName("ModuleName")
        statsBuilder1.moduleTitle("ModuleTitle")
        articleReadEvent = statsBuilder1.build()
    }

    @Test
    fun checkFulfillment() {
        val checker = StatisticsFulfillmentChecker(0,
            listOf(
                StatisticsFulfillment("articleRead"),
                StatisticsFulfillment("viewShow", mapOf("viewName" to "articleList"))
            )
        )

        assertEquals(checker.updateAndCheck(articleListEvent), false)
        assertEquals(checker.updateAndCheck(articleReadEvent), false)

        assertEquals(checker.updateAndCheck(articleReadEvent), false)
        assertEquals(checker.updateAndCheck(articleListEvent), true)

        assertEquals(checker.updateAndCheck(articleListEvent), false)
        assertEquals(checker.updateAndCheck(articleReadEvent), false)

        assertEquals(checker.updateAndCheck(articleReadEvent), false)
        assertEquals(checker.updateAndCheck(articleListEvent), true)

    }

}