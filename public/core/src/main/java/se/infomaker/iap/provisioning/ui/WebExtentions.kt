package se.infomaker.iap.provisioning.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.navigaglobal.mobile.R
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.ktx.brandColor
import timber.log.Timber

fun Context.openCustomTab(uri: Uri) {
    val theme = ThemeManager.getInstance(this).appTheme
    val builder = CustomTabsIntent.Builder()
    builder.setToolbarColor(theme.brandColor.get())

    val customTabsIntent = builder.build()
    customTabsIntent.intent.flags = customTabsIntent.intent.flags or
            Intent.FLAG_ACTIVITY_NO_HISTORY or
            Intent.FLAG_ACTIVITY_NEW_TASK
    try {
        customTabsIntent.launchUrl(this, uri)
    }
    catch (e: ActivityNotFoundException) {
        Timber.w(e, "Failed to open uri $uri")
        Toast.makeText(this, getString(R.string.failed_to_open_uri), Toast.LENGTH_SHORT).show()
    }
}