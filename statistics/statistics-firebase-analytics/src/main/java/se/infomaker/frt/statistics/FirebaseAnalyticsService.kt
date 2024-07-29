package se.infomaker.frt.statistics

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import io.hansel.ujmtracker.HanselTracker
import se.infomaker.frt.statistics.extensions.resolveMustache
import se.infomaker.frt.statistics.firebaseanalytics.EventMapping
import se.infomaker.frt.statistics.firebaseanalytics.FirebaseAnalyticsConfig
import timber.log.Timber


private const val MISSING_VALUE = "N/A"
private var bottom_drawer = "none"
private var moduleTitle: String = ""
private var moduleID: String = ""
private var moduleParent: String = ""

class FirebaseAnalyticsService : StatisticsManager.StatisticsService {
    override fun getIdentifier(): String {
        return "FirebaseAnalytics"
    }

    private var config: FirebaseAnalyticsConfig? = null
    private var analytics: FirebaseAnalytics? = null

    override fun init(context: Context, rawConfig: MutableMap<String, Any>?) {
        if (rawConfig == null) {
            return
        }

        Timber.e("FirebaseRaw: %s", rawConfig)

        this.config = FirebaseAnalyticsConfig.fromMap(rawConfig)
        analytics = FirebaseAnalytics.getInstance(context)
    }

    override fun logEvent(event: StatisticsEvent) {

        Timber.d("FirebaseStatisticsEvent: %s", event.eventName, event.attributes)

        config?.let { config ->
            if (config.eventTypeMapping.isNullOrEmpty()) {
                val attributes = Bundle()
                event.attributes?.let { attribute ->
                    attribute.forEach{
                        attributes.putString(it.key, if (it.value == null || it.value == "") MISSING_VALUE else EventMapping.safeString(it.value.toString()))
                    }
                }

                // Create a new bundle for properties
//                val properties = Bundle()
//                val propertiesMap = HashMap<String, Any>()
//                for (key in properties.keySet()) {
//                    propertiesMap[key] = properties.get(key) as Any
//                }

                Timber.d("TrackEvents: %s", event.eventName)
                Timber.d("EventAttributes1: %s", event.attributes)
                Timber.d("EventAttributes2: %s", attributes)

                if (event.attributes.isNotEmpty() && event.attributes.containsKey("moduleParent") && event.attributes.containsKey("moduleID")) {
                    val moduleTitle = (event.attributes["moduleTitle"] ?: "").toString().lowercase()
                    val moduleID = (event.attributes["moduleID"] ?: "").toString().lowercase()
                    val moduleParent = (event.attributes["moduleParent"] ?: "").toString().lowercase()

                    if (moduleID.isNotBlank() && moduleParent.isNotBlank()) {
                        val HanselpropertiesMap = hashMapOf<String, Any>(
                            "moduleParent" to moduleParent,
                            "moduleID" to moduleID
                        )

                        Timber.d("FirebaseEventLog: $moduleParent-$moduleID")

                        analytics?.logEvent("$moduleParent-$moduleID", attributes)

                        val hanselData = HanselTracker.logEvent("$moduleParent-$moduleID", "fbs", HanselpropertiesMap)
                        for (key in hanselData.keys) {
                            attributes.putString(key, hanselData[key].toString())
                        }

                        if (!config.screenTracking.isNullOrEmpty() && event.isViewEvent) {
                            val convertedScreenTracking: HashMap<String, Any> = config.screenTracking?.let { HashMap(it) }?.mapValues { it.value as Any } as HashMap<String, Any>

//                            convertedScreenTracking.let {
//                                HanselTracker.logEvent("$moduleParent-$moduleID", "fbs", it)
//                            }
                        }
                    } else {
                        Timber.d("Skipping event logging due to empty moduleID or moduleParent")
                        Timber.d("EVENT_VALUES: $moduleParent-$moduleID")
                    }
                }

                if (!config.screenTracking.isNullOrEmpty() && event.isViewEvent && !event.attributes.containsKey("moduleParent")) {
                    trackScreenView(event, config.screenTracking)
                }
                return
            }
        }

//        val eventName = config?.eventTypeMapping?.get(event.eventName)?.let { eventTypeMapping ->
//            val value = event.attributes[eventTypeMapping.key]
//            if (value != null) {
//                return@let eventTypeMapping.mapping[value]
//            }
//            Timber.d("No mapping for $value")
//            return
//        } ?: event.eventName ?: return
//
//        config?.events?.get(eventName)?.let { eventMapping ->
//            val eventBundle = eventMapping.mapBundle(event, config?.defaultMissingValue)
//            if (config?.screenTracking?.isEmpty() == false && event.isViewEvent) {
//                trackScreenView(event, config?.screenTracking)
//            }
//        }
    }

    private fun trackScreenView(event: StatisticsEvent, screenTracking: Map<String, String>?) {
        val screenName = screenTracking?.get("screen_name")?.resolveMustache(event.attributes)
        val screenClass = screenTracking?.get("screen_class")?.resolveMustache(event.attributes)

        ////// New Comment //////

        Timber.d("FirebaseLog SName: %s, SClass: %s Event: %s Attributes: %s", screenName, screenClass, event.eventName, event.attributes)

        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {

            screenTracking?.get("screen_class")?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it.resolveMustache(event.attributes)) }
            screenTracking?.get("screen_name")?.let { param(FirebaseAnalytics.Param.SCREEN_NAME, it.resolveMustache(event.attributes)) }
        }

        val moduleID = (event.attributes["moduleID"] ?: "").toString().lowercase()

        val hanselpropertiesMapn = hashMapOf<String, Any>(
            "screen_name" to moduleID,
            "screen_class" to moduleID
        )

        if (screenName != null) {
            HanselTracker.logEvent(screenName.split("-")[0], "fbs", hanselpropertiesMapn)
        }

//        HanselTracker.logEvent("screen_load", "fbs", screenTracking as HashMap<String?, Any?>?)

//        val convertedScreenTracking: HashMap<String, Any> = screenTracking?.let { HashMap(it) }?.mapValues { it.value as Any } as HashMap<String, Any>
//
//        if (screenName != null) {
//            HanselTracker.logEvent(screenName.split("-")[0], "fbs", convertedScreenTracking)
//        }

        ////// New Comment //////


    }

    override fun globalAttributesUpdated(globalAttributes: Map<String, Any>) {
        config?.let {
            if (it.sendUserId) {
                val userId = globalAttributes["userID"] as? String
                analytics?.setUserId(userId)
            }
            it.userProperties?.forEach { (key, template) ->
                var value: String? = template.resolveMustache(globalAttributes)
                if (TextUtils.isEmpty(value)) {
                    value = null
                }
                analytics?.setUserProperty(key, value)
            }
        }
    }
}