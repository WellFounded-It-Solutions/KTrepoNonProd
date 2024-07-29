package se.infomaker.frtutilities.view

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView
import androidx.annotation.RequiresApi

/**
 * Subclass of WebView to use for Lollipop versions of Android. 21 & 22
 *
 * This works around a problem that used to be worked around by androidx.appcompat:appcompat,
 * but is broken in the current version we are using: 1.1.0
 *
 * Until that is reliably fixed, use this to create a WebView that is safe to use on
 * Lollipop devices.
 *
 * Common use case, regular WebView instantiation throws exception, check if Lollipop and then
 * instantiate this type of WebView instead.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class LollipopSafeWebView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : WebView(context.lollipopWebViewSafeContext(), attrs, defStyleAttr, defStyleRes)

private fun Context.lollipopWebViewSafeContext() = if (Build.VERSION.SDK_INT in 21..22) createConfigurationContext(Configuration()) else this