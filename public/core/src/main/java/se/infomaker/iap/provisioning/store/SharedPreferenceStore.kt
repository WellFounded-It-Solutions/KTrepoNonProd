package se.infomaker.iap.provisioning.store

import android.annotation.SuppressLint
import android.content.SharedPreferences

class SharedPreferenceStore(private val sharedPreferences: SharedPreferences) : KeyValueStore {
    override fun putBoolean(key: String, value: Boolean?): KeyValueStore {
        if (value != null) {
            if (editor != null) {
                editor?.putBoolean(key, value)
            } else {
                sharedPreferences.edit().putBoolean(key, value).apply()
            }
            return this
        }
        return this
    }

    override fun getBoolean(key: String, fallback: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, fallback)
    }

    var editor: SharedPreferences.Editor? = null

    override fun put(key: String, value: String?): KeyValueStore {

        if (editor != null) {
            editor?.putString(key, value)
        } else {
            sharedPreferences.edit().putString(key, value).apply()
        }
        return this
    }

    override fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    @SuppressLint("CommitPrefEdits")
    override fun beginTransaction(): KeyValueStore {
        editor = sharedPreferences.edit()
        return this
    }

    override fun endTransaction() {
        editor?.commit()
        editor = null
    }

    override fun rollback() {
        editor = null
    }

    override fun clear() {
        editor = null
        sharedPreferences.edit().clear().apply()
    }
}