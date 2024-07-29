package se.infomaker.googleanalytics

import android.content.Context
import android.view.WindowManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import se.infomaker.googleanalytics.register.Hit
import timber.log.Timber
import java.util.UUID

class Tracker @AssistedInject constructor(
    @ApplicationContext context: Context,
    @Assisted private val trackingId: String,
    private val googleAnalytics: GoogleAnalytics
) {
    var clientId:  String? = null
    set(value) {
        field = if (value?.isEmpty() == true) null else value
    }
    private val windowManager : WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val appIdentifier: String = context.packageName
    private val appVersion: String = context.getAppVersion()
    private val appName: String = context.getAppName()
    private val defaultClientId: String
    val userLanguage: String

    init {
        context.getSharedPreferences("GA-Tracker-$trackingId", Context.MODE_PRIVATE).let {
            var identifier = it.getString("defaultClientId", null)
            if (identifier.isNullOrEmpty()) {
                identifier = UUID.randomUUID().toString()
                it.edit().putString("defaultClientId", identifier).apply()
            }
            defaultClientId = identifier
        }
        userLanguage = context.getCurrentLocale().language
    }

    fun send(attributes: Map<String, String>) {
        val out = mutableMapOf<String, String>().also {
            it["aid"] =  appIdentifier
            it["an"] = appName
            it["av"] = appVersion
            it["cid"] = clientId ?: defaultClientId
            it["ds"] = "app"
            it["sr"] = windowManager.getDpResolution()
            it["t"] = "event"
            it["ul"] = userLanguage
            it["v"] = "1"
            it["tid"] = trackingId
            it.putAll(googleAnalytics.globalValues)
            it.putAll(attributes)
        }
        val params = toParams(out)
        Timber.d("Registering: $params" )
        googleAnalytics.addHit(Hit(params))
    }

    private fun toParams(params: MutableMap<String, String>): String {
        val sorted = params.keys.sorted()
        StringBuilder().let { builder ->
            sorted.forEach { key ->

                builder.append(android.net.Uri.encode(key))
                builder.append("=")
                builder.append(android.net.Uri.encode(params[key]))
                if (sorted.lastIndexOf(key) < sorted.lastIndex) {
                    builder.append("&")
                }
            }
            return builder.toString()
        }
    }
}

