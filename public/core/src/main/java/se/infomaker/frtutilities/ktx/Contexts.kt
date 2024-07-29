@file:JvmName("ContextUtils")

package se.infomaker.frtutilities.ktx

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import com.navigaglobal.mobile.R
import se.infomaker.frtutilities.context.OneShotActivityCreatedListener

val Context.isDebuggable: Boolean
    get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

val Context.storeListingUrl
    get() = getString(R.string.store_listing)

fun Context.openStoreListing() {
    openStoreListing(packageName)
}

fun Context.openStoreListing(appPackageName: String) {
    try {
        val marketScheme = getString(R.string.store_scheme)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$marketScheme://details?id=$appPackageName")))
    }
    catch (e: ActivityNotFoundException) {
        val storeListing = storeListingUrl
        if (storeListing.isNotEmpty()) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeListing)))
        }
    }
}

fun Context.privatePreferences(name: String? = null): SharedPreferences {
    name?.let {
        return getSharedPreferences(it, Context.MODE_PRIVATE)
    }
    return PreferenceManager.getDefaultSharedPreferences(this)
}

fun Context.getVersionCode() = try {
    PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(packageName, 0))
} catch (e: PackageManager.NameNotFoundException) {
    0
}

fun Context.getVersionName() = try {
    packageManager.getPackageInfo(packageName, 0).versionName ?: "Unknown"
} catch (e: PackageManager.NameNotFoundException) {
    "Unknown"
}

fun Context.getAppName() =
    packageManager.getApplicationLabel(applicationInfo)

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)?.baseContext?.findActivity()
}

fun Context.requireActivity(): Activity {
    return findActivity() ?: throw IllegalStateException("Could not find Activity in context $this")
}

fun Context.doOnActivityCreated(action: (Activity) -> Boolean) {
    OneShotActivityCreatedListener.add(this, action)
}

fun Context.layoutInflater(): LayoutInflater = LayoutInflater.from(this)