package se.infomaker.googleanalytics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import timber.log.Timber
import java.util.Locale

fun WindowManager.getDpResolution(): String {
    val metrics = DisplayMetrics()
    defaultDisplay.getMetrics(metrics)
    val width: Int = (metrics.widthPixels/metrics.density).toInt()
    val height: Int = (metrics.heightPixels/metrics.density).toInt()
    return "${width}x$height"
}

fun Context.getAppName(): String {
    val applicationInfo = applicationInfo
    val stringId = applicationInfo.labelRes
    return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else getString(stringId)
}

fun Context.getAppVersion() : String {
    val context = applicationContext
    val manager = context.packageManager
    return try {
        val info = manager.getPackageInfo(context.packageName, 0)
        info.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.e(e, "Failed to determine app version")
        "Unknown"
    }
}

fun Context.getCurrentLocale(): Locale {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales.let { list ->
            if (!list.isEmpty) {
                return list[0]
            }
        }
    }
    return resources.configuration.locale
}
