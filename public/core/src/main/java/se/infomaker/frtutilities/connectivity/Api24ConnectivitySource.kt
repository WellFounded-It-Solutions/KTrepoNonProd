package se.infomaker.frtutilities.connectivity

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.annotation.RequiresApi
import io.reactivex.FlowableEmitter

@RequiresApi(24)
class Api24ConnectivitySource(private val connectivityManager: ConnectivityManager) : ConnectivitySource {

    override fun subscribe(emitter: FlowableEmitter<Unit>) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                emitter.onNext(Unit)
            }

            override fun onLost(network: Network) {
                emitter.onNext(Unit)
            }
        }

        emitter.onNext(Unit)
        connectivityManager.registerDefaultNetworkCallback(callback)

        emitter.setCancellable {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}