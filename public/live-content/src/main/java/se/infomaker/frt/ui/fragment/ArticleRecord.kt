package se.infomaker.frt.ui.fragment

import org.json.JSONObject
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ModuleInformation
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.livecontentmanager.parser.PropertyObject

data class ArticleRecord(val uuid: String, val content: ContentStructure, val presentationContext: JSONObject = JSONObject()) {

    fun logShown(moduleInfo: ModuleInformation) {
        val statisticsObject = PropertyObject(content.properties, uuid)
        StatisticsEvent.Builder()
            .viewShow()
            .viewName("article")
            .moduleId(moduleInfo.identifier)
            .moduleName(moduleInfo.name)
            .moduleTitle(moduleInfo.title)
            .attribute("article", statisticsObject.describe())
            .attribute("articleHeadline", statisticsObject.optString("ArticleHeadline"))
            .attribute("articleUUID", uuid)
            .attribute("isFrequency", statisticsObject.optString("isFrequency", "false"))
            .attribute("isPremium", statisticsObject.optString("isPremium", "false"))
            .build().let { event ->
                StatisticsManager.getInstance().logEvent(event)
            }
    }
}