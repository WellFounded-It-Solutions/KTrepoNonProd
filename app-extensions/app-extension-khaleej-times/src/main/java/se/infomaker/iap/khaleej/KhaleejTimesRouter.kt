package se.infomaker.iap.khaleej

import android.Manifest
import android.content.Context
import android.content.Intent
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import se.infomaker.iap.StateRouter

class KhaleejTimesRouter(private val defaultRouter: StateRouter?): StateRouter {

    override fun currentRoute() = defaultRouter?.currentRoute()

    override fun route(context: Context, intent: Intent, onComplete: () -> Unit, onCancel: () -> Unit) {
        val interceptedOnComplete = {
            if (!context.locationPermissionGranted) {
                Dexter.withContext(context)
                    .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(object : BaseMultiplePermissionsListener() {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            // Location tracking logic can be added here based on your requirements,
                            // since TealiumProvider.startLocationTracking is removed.
                        }
                    })
                    .check()
            }
            onComplete()
        }
        defaultRouter?.route(context, intent, interceptedOnComplete, onCancel)
    }
}
