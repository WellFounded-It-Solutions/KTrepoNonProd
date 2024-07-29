package se.infomaker.frtutilities.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import io.reactivex.FlowableEmitter

@Suppress("DEPRECATION")
class LegacyConnectivitySource(private val context: Context) : ConnectivitySource {

    override fun subscribe(emitter: FlowableEmitter<Unit>) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                    emitter.onNext(Unit)
                }
            }
        }

        emitter.onNext(Unit)

        context.registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        emitter.setCancellable {
            context.unregisterReceiver(receiver)
        }
    }
}