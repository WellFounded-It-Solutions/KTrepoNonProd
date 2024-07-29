package se.infomaker.iap.push.huawei.token

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val TOKEN_PREFERENCES_NAME = "token_preferences"

internal object PreferencesKeys {
    val TOKEN = stringPreferencesKey("token")
}

internal val Context.tokenStore by preferencesDataStore(TOKEN_PREFERENCES_NAME)

