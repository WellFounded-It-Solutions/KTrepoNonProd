package se.infomaker.frt.statistics.blacklist

import android.content.SharedPreferences
import com.google.gson.Gson
import se.infomaker.frt.statistics.di.BlackListPreferences
import java.util.Date
import javax.inject.Inject

class BlackListSharedPreferencesStore @Inject constructor(
    @BlackListPreferences private val sharedPreferences: SharedPreferences
) : Store<BlackList> {
    override fun get(): BlackList? {
        return blacklist
    }

    override fun set(value: BlackList?) {
        this.blacklist = value
        val edit = sharedPreferences.edit()
        if (blacklist == null) {
            edit.remove("value")
        }
        last = Date()
        edit.putLong("last", System.currentTimeMillis())
        edit.apply()
    }

    override fun lastSet(): Date? {
        return last
    }

    private val gson  = Gson()
    private var blacklist: BlackList? = null
    private var last: Date? = null

    init {
        sharedPreferences.getString("value", null)?.let {
            blacklist = gson.fromJson(it, BlackList::class.java)
        }
        sharedPreferences.getLong("last", -1).let {
            if (it != -1L) {
                last = Date(it)
            }
        }
    }
}