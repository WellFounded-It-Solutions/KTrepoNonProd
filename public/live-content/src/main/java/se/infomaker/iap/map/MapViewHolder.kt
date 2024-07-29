package se.infomaker.iap.map

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleObserver

interface MapViewHolder : LifecycleObserver {

    val mapView: View?

    fun initMapWithOptions(context: Context, mapOptions: MapOptions)

}