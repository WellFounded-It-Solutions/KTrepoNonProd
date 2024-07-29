package se.infomaker.iap.map.google

import android.content.Context
import com.google.android.gms.maps.MapsInitializer
import se.infomaker.frtutilities.AbstractInitContentProvider
import timber.log.Timber

/**
 * https://developers.google.com/maps/documentation/android-sdk/releases#version_1800
 *
 * The new renderer will become the default renderer in 2022, at which point explicit opt-in
 * will not be required.
 */
class GoogleMapsSetup : AbstractInitContentProvider() {

    override fun init(context: Context) {
        MapsInitializer.initialize(context, MapsInitializer.Renderer.LATEST) { renderer ->
            when(renderer) {
                MapsInitializer.Renderer.LEGACY -> Timber.d("The legacy version of the renderer is used.")
                MapsInitializer.Renderer.LATEST -> Timber.d("The latest version of the renderer is used.")
            }
        }
    }

}