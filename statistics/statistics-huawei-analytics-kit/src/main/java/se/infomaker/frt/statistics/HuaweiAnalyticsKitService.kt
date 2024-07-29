package se.infomaker.frt.statistics

import android.content.Context
import android.os.Bundle
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.analytics.HiAnalyticsTools
import se.infomaker.frtutilities.ktx.isDebuggable

class HuaweiAnalyticsKitService : StatisticsManager.StatisticsService {

    private lateinit var analytics: HiAnalyticsInstance

    override fun init(context: Context, config: MutableMap<String, Any>?) {
        if (context.isDebuggable) {
            HiAnalyticsTools.enableLog()
        }
        analytics = HiAnalytics.getInstance(context)
        // TODO Config?
    }

    override fun getIdentifier() = "HuaweiAnalyticsKit"

    override fun logEvent(event: StatisticsEvent) {
        // TODO Mappings?

        val eventParams = Bundle().apply {
            event.attributes?.forEach { (key, value) ->
                val safeValue = if (value == null || value == "") MISSING_VALUE else value.toAnalyticsSafeString()
                putString(key, safeValue)
            }
        }
        analytics.onEvent(event.eventName, eventParams)
    }

    override fun globalAttributesUpdated(globalAttributes: MutableMap<String, Any>) {
        // NOP
    }

    private fun Any?.toAnalyticsSafeString(): String {
        val string = toString()
        if (string.length > VALUE_LIMIT) {
            return string.substring(0, VALUE_LIMIT)
        }
        return string
    }

    companion object {
        private const val MISSING_VALUE = "N/A"
        private const val VALUE_LIMIT = 256
    }
}