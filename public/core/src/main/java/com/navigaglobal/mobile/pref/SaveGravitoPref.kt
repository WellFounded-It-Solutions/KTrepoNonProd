package com.navigaglobal.mobile.pref

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.json.JSONObject

object SaveGravitoPref {
    private const val APP_PREFERENCES = "MYPREF"

    public const val APP_PREFERENCES_KEY_TOKEN = "TOKEN"
    lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    operator fun set(key: String, value: Any?) =
        when (value) {
            is String? -> preferences.edit { it.putString(key, value) }
            is Int -> preferences.edit { it.putInt(key, value) }
            is Boolean -> preferences.edit { it.putBoolean(key, value) }
            is Float -> preferences.edit { it.putFloat(key, value) }
            is Long -> preferences.edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    inline operator fun <reified T : Any> get(
        key: String,
        defaultValue: T? = null
    ): T =
        when (T::class) {
            String::class -> preferences.getString(key, defaultValue as String? ?: "") as T
            Int::class -> preferences.getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class -> preferences.getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> preferences.getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class -> preferences.getLong(key, defaultValue as? Long ?: -1) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    fun  setGravitoKeys(value: String) {
       val response = JSONObject(value)
        val inAppTcData =  response.getJSONObject("inAppTCData")
        set("IABTCF_CmpSdkID",inAppTcData.getString("cmpId"))
        set("IABTCF_CmpSdkVersion",inAppTcData.getString("cmpVersion"))
        set("IABTCF_PolicyVersion",inAppTcData.getString("tcfPolicyVersion"))
        set("IABTCF_gdprApplies",inAppTcData.getString("gdprApplies"))
        set("IABTCF_PublisherCC",inAppTcData.getString("publisherCC"))
        set("IABTCF_PurposeOneTreatment",inAppTcData.getString("purposeOneTreatment"))
        set("IABTCF_UseNonStandardStacks",inAppTcData.getString("useNonStandardStacks"))
        set("IABTCF_TCString",inAppTcData.getString("tcString"))
        set("IABTCF_VendorConsents",inAppTcData.getJSONObject("vendor").getString("consents"))
        set("IABTCF_VendorLegitimateInterests",inAppTcData.getJSONObject("vendor").getString("legitimateInterests"))
        set("IABTCF_PurposeConsents",inAppTcData.getJSONObject("purpose").getString("consents"))
        set("IABTCF_PurposeLegitimateInterests",inAppTcData.getJSONObject("purpose").getString("legitimateInterests"))

        set("IABTCF_SpecialFeaturesOptIns",inAppTcData.getString("specialFeatureOptins"))
        set("IABTCF_PublisherRestrictions{ID}",inAppTcData.getJSONObject("publisher").getJSONObject("restrictions").getString("2"))
        set("IABTCF_PublisherConsent",inAppTcData.getJSONObject("publisher").getString("consents"))
        set("IABTCF_PublisherLegitimateInterests",inAppTcData.getJSONObject("publisher").getString("legitimateInterests"))
        set("IABTCF_PublisherCustomPurposesConsents",inAppTcData.getJSONObject("publisher").getJSONObject("customPurpose").getString("consents"))
        set("IABTCF_PublisherCustomPurposesLegitimateInterests",inAppTcData.getJSONObject("publisher").getJSONObject("customPurpose").getString("legitimateInterests"))


    }


}