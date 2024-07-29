package se.infomaker.iap.articleview.item.embed

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.navigaglobal.mobile.livecontent.R
import kotlinx.parcelize.Parcelize
import name.cpr.Video9EnabledWebChromeClient
import name.cpr.Video9EnabledWebView
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.connectivity.hasInternetConnection
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.ktx.theme
import java.net.URISyntaxException


class FullscreenEmbedActivity : AppCompatActivity() {

    private val theme by theme { moduleId }
    private val resources by resources { moduleId ?: "shared" }

    private var moduleId: String? = null
    private lateinit var webView: Video9EnabledWebView
    private lateinit var webChromeClient: Video9EnabledWebChromeClient
    private lateinit var offlineWarningContainer: FrameLayout
    private lateinit var videoLayout: RelativeLayout
    private lateinit var nonVideoLayout: RelativeLayout

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    var params: FullscreenActivityParameters? = null
    private var intentWhitelist: WhiteListedWebIntents? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen9_webview)

        params = this.intent.extras?.getParcelable(FULLSCREEN_ACTIVITY_PARAMS)
        moduleId = params?.moduleId
        val baseUrl = params?.baseUrl
        val data = params?.embedCode
        val mime = params?.mime
        val encoding = params?.encoding

        intentWhitelist = ConfigManager.getInstance().getConfig("global", WhiteListedWebIntents::class.java)
        /*intentWhitelist = params?.allowedPackages?.let { Gson().fromJson(it, WhiteListedWebIntents::class.java) }*/

        webView = findViewById(R.id.screen9WebView)
        videoLayout = findViewById(R.id.screen9VideoLayout)
        nonVideoLayout = findViewById(R.id.screen9NonVideoLayout)
        offlineWarningContainer = findViewById(R.id.offline_warning_container)

        inflateOfflineWarningView(offlineWarningContainer)

        webChromeClient = object : Video9EnabledWebChromeClient(nonVideoLayout, videoLayout) {
            // Subscribe to standard events, such as onProgressChanged()...
            override fun onProgressChanged(view: WebView, progress: Int) {
            }
        }

        webChromeClient.setOnToggledFullscreen { fullscreen ->
            // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
            if (fullscreen) {
                val attrs = window.attributes
                attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
                attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                window.attributes = attrs

                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            } else {
                val attrs = window.attributes
                attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
                attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON.inv()
                window.attributes = attrs
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            }
        }

        webView.webChromeClient = webChromeClient
        // Call private class InsideWebViewClient
        webView.webViewClient = InsideWebViewClient()
        if (savedInstanceState == null) {
            // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
            webView.loadDataWithBaseURL(baseUrl, data ?: "", mime, encoding, baseUrl)
        }

        theme.apply(this)
    }

    private fun inflateOfflineWarningView(parent: ViewGroup) {
        var layoutIdentifier = resources.getLayoutIdentifier("offline_warning")
        if (layoutIdentifier < 1) {
            layoutIdentifier = R.layout.offline_warning_default
        }
        val inflated = LayoutInflater.from(this).inflate(layoutIdentifier, parent, true)

        inflated?.findViewById<TextView>(R.id.offline_warning_title)?.let {
            it.text = resources.getString("offline_warning_title", null)
        }
        inflated?.findViewById<TextView>(R.id.offline_warning_message)?.let {
            it.text = resources.getString("offline_warning_message", null)
        }
    }

    private fun showOfflineWarning() {
        videoLayout.visibility = View.GONE
        nonVideoLayout.visibility = View.GONE
        offlineWarningContainer.visibility = View.VISIBLE
    }

    private fun showErrorToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).apply { show() }
    }

    private fun canHandle(intent: Intent?): Boolean {
        intent?.let { intentToStart ->
            val matchedPackage = intentWhitelist?.allowedWebViewIntents?.firstOrNull {
                it?.containsKey(intentToStart.`package` ?: "") ?: false
            }?.values?.first()

            val schemes = matchedPackage?.let {
                it["schemes"]?.any { scheme -> scheme == intentToStart.scheme }
            } ?: true

            return matchedPackage?.isNotEmpty() ?: false && schemes
        }
        return false
    }

    private inner class InsideWebViewClient : WebViewClient() {

        private var hasError = false

        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            if (url.startsWith("intent://")) {
                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)?.let { intent ->
                        if (canHandle(intent)){
                            return try {
                                startActivity(intent)
                                true
                            } catch (e: ActivityNotFoundException) {
                                view.stopLoading()
                                showErrorToast(view.context, "Unable to start app")
                                false
                            }
                        }
                        view.stopLoading()
                        showErrorToast(view.context, "App security does not permit this operation")
                        return false
                    }
                } catch (e: URISyntaxException) {
                    view.stopLoading()
                    showErrorToast(view.context, "Unable to start app")
                    return false
                }
            }
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (hasError && !hasInternetConnection()) {
                showOfflineWarning()
            }
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            hasError = true
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            hasError = true
        }
    }

    override fun onBackPressed() {
        // Notify the Video9EnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed()) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                // Standard back button implementation (for example this could close the app)
                super.onBackPressed()
            }
        }
    }

    companion object {

        private const val FULLSCREEN_ACTIVITY_PARAMS = "parameters"

        @JvmOverloads
        fun createIntent(
            context: Context,
            baseUrl: String,
            embedCode: String,
            mime: String? = null,
            encoding: String? = null,
            moduleId: String? = null
        ): Intent {
            val parcel =
                FullscreenActivityParameters(baseUrl, embedCode, mime, encoding, moduleId)
            return Intent(context, FullscreenEmbedActivity::class.java).apply {
                putExtra(FULLSCREEN_ACTIVITY_PARAMS, parcel)
            }
        }
    }
}

@Parcelize
data class FullscreenActivityParameters(
    val baseUrl: String,
    val embedCode: String,
    val mime: String? = null,
    val encoding: String? = null,
    val moduleId: String? = null
) : Parcelable

data class WhiteListedWebIntents(val allowedWebViewIntents: List<Map<String, Map<String, List<String>?>?>?>?)
