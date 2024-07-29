package se.infomaker.frt.statistics

import android.content.Context

class GoogleTagManagerService : StatisticsManager.StatisticsService {

    private lateinit var service: StatisticsManager.StatisticsService

    override fun init(context: Context?, config: MutableMap<String, Any>?) {
        val identifier = context?.resources?.getIdentifier("default_container", "raw", context.packageName)
            ?: 0
        service = if (identifier != 0) {
            LegacyGoogleTagManagerImpl()
        }
        else {
            GoogleTagManagerImpl()
        }
        service.init(context, config)
    }

    override fun getIdentifier(): String {
        return service.identifier
    }

    override fun logEvent(event: StatisticsEvent?) {
        val services = StatisticsManager.getInstance().registeredServices()
        for (service in services) {
            if (service is FirebaseAnalyticsService) {
                return
            }
        }
        service.logEvent(event)
    }

    override fun globalAttributesUpdated(globalAttributes: MutableMap<String, Any>) {
        service.globalAttributesUpdated(globalAttributes)
    }
}