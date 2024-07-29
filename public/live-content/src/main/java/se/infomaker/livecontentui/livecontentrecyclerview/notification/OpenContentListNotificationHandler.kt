package se.infomaker.livecontentui.livecontentrecyclerview.notification

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.livecontentui.livecontentdetailview.activity.ArticlePagerActivity
import se.infomaker.livecontentui.section.SectionedLiveContentActivity

class OpenContentListNotificationHandler(private val moduleIdentifier: String) : OnNotificationInteractionHandler {
    override fun handleOpenNotification(activity: Activity, notification: Map<String, String>) {
        val uuid = notification["uuid"]

        val contentType = notification["contentType"]
        if ("package".equals(contentType, ignoreCase = true)) {

            val intent = Intent(activity, SectionedLiveContentActivity::class.java)

            val bundle = Bundle()
            activity.pushPackageModuleId(moduleIdentifier)?.let {
                bundle.putString("hitsListListId", it) // !!! ??? DRUGS !??!?!
                bundle.putString("moduleId", it)
            } ?: run {
                bundle.putString("hitsListListId", moduleIdentifier) // !!! ??? DRUGS !??!?!
                bundle.putString("moduleId", moduleIdentifier)
            }
            bundle.putString("source", "notification")
            bundle.putString("packageUuid", uuid)

            intent.putExtras(bundle)

            activity.startActivity(intent)

        } else {
            ArticlePagerActivity.openArticle(activity, moduleIdentifier, null, uuid, "notification")
        }
        logNotificationEventStatistic(notification, uuid)
    }

    private fun logNotificationEventStatistic(notification: Map<String, String>, contentId: String?) {
        val title = notification["title"]
        val text = notification["message"]

        StatisticsEvent.Builder().event("openNotification")
            .attribute("title", title)
            .attribute("text", text)
            .attribute("notificationTitle", title)
            .attribute("notificationText", text)
            .attribute("contentId", contentId)
            .moduleId(moduleIdentifier)
            .moduleName(ModuleInformationManager.getInstance().getModuleName(moduleIdentifier))
            .moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleIdentifier))
            .build()
            .also {
                StatisticsManager.getInstance().logEvent(it)
            }
    }

    override fun handleDeleteNotification(context: Context?, notification: MutableMap<String, String>?) {
        // Do nothing
    }
}

private fun Context.pushPackageModuleId(moduleIdentifier: String): String? {
    return ConfigManager.getInstance(applicationContext).getProperty(moduleIdentifier, String::class.java, "pushPackageModuleId")
}