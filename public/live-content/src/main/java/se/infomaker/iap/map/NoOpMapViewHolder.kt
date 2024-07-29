package se.infomaker.iap.map

import android.annotation.SuppressLint
import android.content.Context
import android.view.View

@SuppressLint("StaticFieldLeak")
object NoOpMapViewHolder : MapViewHolder {
    override val mapView: View? = null

    override fun initMapWithOptions(context: Context, mapOptions: MapOptions) {
        // no-op
    }
}