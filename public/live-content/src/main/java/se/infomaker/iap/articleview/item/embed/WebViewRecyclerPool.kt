package se.infomaker.iap.articleview.item.embed

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.core.content.ContextCompat.startActivity
import androidx.webkit.WebViewClientCompat
import se.infomaker.frtutilities.connectivity.hasInternetConnection
import timber.log.Timber

object WebViewRecyclerPool {

    private val pool = mutableListOf<Triple<String, WebView, Long>>()
    private var poolLimit = 6
    private const val CACHE_DURATION = 120000 /* 2 mins */
    private fun createWebView(context:Context): WebView {
        return WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClientCompat() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (request.hasGesture()) {
                        try {
                            startActivity(context, Intent(Intent.ACTION_VIEW, request.url), null)
                            return true
                        }
                        catch (e: ActivityNotFoundException) {
                            Timber.e("Tried to open an Activity using url: ${request.url}, but no Activity could handle the intent.")
                        }
                    }
                    return false
                }
            }
        }
    }

    fun recycle(webView: WebView, item: HtmlEmbedItem) {
        if (webView.context.hasInternetConnection()) {
            val expires = System.currentTimeMillis() + CACHE_DURATION
            pool.add(0, Triple(item.data, webView, expires))

            while (pool.size > poolLimit) {
                pool.removeAt(pool.size - 1)
            }
        }
        Timber.d("Pool size=${pool.size}")
    }

    private fun emptyExpiredViews() {
            pool.filter { System.currentTimeMillis() > it.third }
                .forEach { pool.remove(it) }
    }

    fun get(context: Context, item: HtmlEmbedItem): WebView {
        emptyExpiredViews()
        pool.asSequence().filter { it.first == item.data }.firstOrNull()?.let {
            pool.remove(it)
            return it.second
        }
        return createWebView(context).apply {
            loadDataWithBaseURL(item.baseUrl, item.data, "text/html", "UTF-8", item.baseUrl)
        }
    }
}