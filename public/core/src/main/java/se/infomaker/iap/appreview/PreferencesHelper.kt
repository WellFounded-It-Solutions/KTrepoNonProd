package se.infomaker.iap.appreview

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(private val context: Context): PreferencesBase() {

    override var sessionStart:Int
        set(value) {
            editor().edit()?.putInt(SESSION_START, value)?.apply()
        }
        get() = editor().getInt(SESSION_START, 0)

    override var sessionEnd:Int
        set(value) {
            editor().edit()?.putInt(SESSION_END, value)?.apply()
        }
        get() = editor().getInt(SESSION_END, 0)

    override var totalUsageTime:Int
        set(value) {
            editor().edit()?.putInt(TOTAL_USAGE_TIME, value)?.apply()
        }
        get() = editor().getInt(TOTAL_USAGE_TIME, 0)

    override var neverAsk:Boolean
        set(value) {
            editor().edit()?.putBoolean(NEVER_ASK, value)?.apply()
        }
        get() = editor().getBoolean(NEVER_ASK, false)

    override var snoozeStartTime:Int
        set(value) {
            editor().edit()?.putInt(SNOOZE_START_TIME, value)?.apply()
        }
        get() = editor().getInt(SNOOZE_START_TIME, 0)

    private fun editor(): SharedPreferences {
        return context.getSharedPreferences(APP_REVIEW, Context.MODE_PRIVATE)
    }
}