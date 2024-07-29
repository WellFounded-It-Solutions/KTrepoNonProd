@file:JvmName("ConnectivityUtils")

package se.infomaker.frtutilities.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

fun Context.hasInternetConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivityManager.hasInternetConnection()
}

internal fun Context.hasInternetConnectionOrThrow(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ?: throw NullPointerException("No active network object")
    } else {
        @Suppress("DEPRECATION")
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}

internal fun ConnectivityManager.hasInternetConnection() = if (Build.VERSION.SDK_INT >= 23) {
    getNetworkCapabilities(activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            ?: false
} else {
    @Suppress("DEPRECATION")
    activeNetworkInfo?.isConnected ?: false
}