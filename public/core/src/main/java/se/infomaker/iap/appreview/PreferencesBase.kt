package se.infomaker.iap.appreview


abstract class PreferencesBase {

    companion object {
        const val APP_REVIEW = "com.naviga.mobile.appreview"
        const val SESSION_END = "sessionEnd"
        const val SESSION_START = "sessionStart"
        const val TOTAL_USAGE_TIME = "totalUsageTime"
        const val NEVER_ASK = "neverAsk"
        const val SNOOZE_START_TIME = "snoozeStartTime"
    }

    abstract var sessionStart:Int

    abstract var sessionEnd:Int

    abstract var totalUsageTime:Int

    abstract var neverAsk:Boolean

    abstract var snoozeStartTime:Int

}