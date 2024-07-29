package se.infomaker.frtutilities.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build

object ConnectivitySourceFactory {

    fun create(context: Context): ConnectivitySource {
        return when {
            Build.VERSION.SDK_INT >= 24 -> {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                Api24ConnectivitySource(connectivityManager)
            }
            else -> LegacyConnectivitySource(context)
        }
    }
}