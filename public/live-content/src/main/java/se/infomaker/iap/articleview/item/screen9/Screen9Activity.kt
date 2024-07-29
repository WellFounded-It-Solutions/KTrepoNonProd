package se.infomaker.iap.articleview.item.screen9

import android.content.Context
import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import name.cpr.Video9EnabledWebChromeClient
import name.cpr.Video9EnabledWebView
import com.navigaglobal.mobile.livecontent.R

class Screen9Activity: AppCompatActivity() {

    private lateinit var webView: Video9EnabledWebView
    private lateinit var webChromeClient: Video9EnabledWebChromeClient

    companion object {
        fun createIntent(context: Context, url:String): Intent
        {
            return Intent(context, Screen9Activity::class.java).apply {
                putExtra("url", url)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.screen9_webview)
        val extras = this.intent.extras
        val url = extras?.getString("url")

        webView = findViewById(R.id.screen9WebView)
        val videoLayout = findViewById<ViewGroup>(R.id.screen9VideoLayout)
        val nonVideoLayout = findViewById<ViewGroup>(R.id.screen9NonVideoLayout)
        webChromeClient = object : Video9EnabledWebChromeClient(nonVideoLayout, videoLayout)
        {
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
        // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
        url?.let {
            webView.loadUrl(it)
        }
    }

    private inner class InsideWebViewClient : WebViewClient() {

        override// Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
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
}