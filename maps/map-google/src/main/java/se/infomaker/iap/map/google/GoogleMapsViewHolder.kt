package se.infomaker.iap.map.google

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import se.infomaker.iap.map.Interaction
import se.infomaker.iap.map.MapOptions
import se.infomaker.iap.map.MapViewHolder

class GoogleMapsViewHolder : MapViewHolder, OnMapReadyCallback {

    private var mapOptions: MapOptions? = null

    private var _mapView: MapView? = null
    override val mapView: MapView?
        get() = _mapView

    override fun initMapWithOptions(context: Context, mapOptions: MapOptions) {
        this.mapOptions = mapOptions
        val liteMode = mapOptions.interaction != Interaction.INTERACTIVE
        val mapView = MapView(context, GoogleMapOptions().liteMode(liteMode))
        mapView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mapView.getMapAsync(this)
        _mapView = mapView
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        _mapView?.onCreate(null)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        _mapView?.onStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        _mapView?.onResume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        _mapView?.onPause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        _mapView?.onStop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        _mapView?.onDestroy()
    }

    override fun onMapReady(map: GoogleMap) {
        mapOptions?.let { options ->
            val latLng = LatLng(options.coordinates.latitude, options.coordinates.longitude)
            val marker = MarkerOptions().position(latLng)
            map.addMarker(marker)
            val cameraPosition = CameraPosition.Builder().target(latLng).zoom(options.zoomLevel).build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            when(options.interaction) {
                Interaction.STATIC -> {
                    map.uiSettings.isMapToolbarEnabled = false
                    map.setOnMapClickListener {
                        // NOOP for static
                    }
                    map.uiSettings.setAllGesturesEnabled(false)
                }
                Interaction.EXTERNAL -> {
                    map.uiSettings.isMapToolbarEnabled = false
                }
                else -> {}
            }
        }
    }
}