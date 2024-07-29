package se.infomaker.frt

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.webkit.WebView
import com.navigaglobal.mobile.R
import com.yariksoffice.lingver.Lingver
import se.infomaker.frt.util.AbstractActivityLifecycleCallbackListener
import timber.log.Timber

object LanguageManager : AbstractActivityLifecycleCallbackListener() {

    var needsWebViewWorkaround: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    private set

    @JvmStatic
    fun setup(application: Application) {
        if (needsWebViewWorkaround) {
            application.registerActivityLifecycleCallbacks(this);
        }
        val language: String = application.getString(R.string.app_language)
        if (TextUtils.isEmpty(language)) {
            // Let the system dictate language
            needsWebViewWorkaround = false
            Timber.d("Let system control the app language")
            return
        }
        Timber.d("Setting app language to $language")
        Lingver.init(application, language)
    }

    override fun onActivityPaused(activity: Activity) {
        if (needsWebViewWorkaround) {
            applyWebViewWorkaround(activity)
            activity.application.unregisterActivityLifecycleCallbacks(this)
        }
    }

    private fun applyWebViewWorkaround(context: Context) {
        needsWebViewWorkaround = false
        try {
            WebView(context).destroy()
        }
        catch (e: Exception) {
            /*
             * We cannot catch MissingWebViewPackageException since we cannot see it,
             * instead we make a best effort to validate the exception as such.
             *
             * If we don't think this a MissingWebViewPackageException, we rethrow!
             */
            if (e.message?.contains("No WebView installed") != true) {
                throw e
            }
        }
        Lingver.getInstance().setLocale(context, Lingver.getInstance().getLocale())
    }
}