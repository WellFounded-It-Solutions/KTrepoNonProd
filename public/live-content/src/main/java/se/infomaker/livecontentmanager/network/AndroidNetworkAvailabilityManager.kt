package se.infomaker.livecontentmanager.network

import android.content.Context
import android.net.ConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidNetworkAvailabilityManager @Inject constructor(
    @ApplicationContext context: Context
) : NetworkAvailabilityManager {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override fun hasNetwork(): Boolean = connectivityManager?.activeNetworkInfo?.isConnected == true
}