package com.navigaglobal.mobile.consent

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager

class UserConsentProvider(context: Context): SharedPreferences.OnSharedPreferenceChangeListener {

    private val userConsent: MutableLiveData<Boolean>
    private val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    init {
        val hasUserConsent = defaultSharedPreferences.getString(IABTCF_ADDTL_CONSENT, null).asUserConsent()
        userConsent = MutableLiveData(hasUserConsent)
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    fun asLiveData(): LiveData<Boolean> {
        return userConsent
    }

    fun hasUserConsent() : Boolean {
        return userConsent.value == true
    }

    companion object {
        const val IABTCF_ADDTL_CONSENT = "IABTCF_AddtlConsent"
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (key == IABTCF_ADDTL_CONSENT) {
            preferences.getString(IABTCF_ADDTL_CONSENT, null).asUserConsent().let { userConsent ->
                if (this.userConsent.value != userConsent) {
                    this.userConsent.value = userConsent
                }
            }
        }
    }
}

private fun String?.asUserConsent(): Boolean {
    return this != null && "1~" != this
}
