package se.infomaker.iap.articleview.item.element

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor
import timber.log.Timber

interface OnLinkClickInterceptor {
    /**
     * Called when an url is clicked.
     *
     * Note that this method IS always called from the UI thread
     *
     * @param context
     * @param url to intercept
     * @param resultListener used to deliver intercept result asynchronously
     */
    fun intercept(context: Context, url: String, resultListener: OnInterceptResult)
}

interface OnInterceptResult {
    /**
     * Note that this method should ALWAYS be called on the UI thread
     * @param isHandled true if the interceptor has handled the link
     * @param url to open if the link was not handled
     */
    fun onInterceptResult(isHandled: Boolean, url: String)
}

object OnLinkClickManager {

    private val listeners = mutableSetOf<OnLinkClickInterceptor>()

    /**
     * Add interceptor to be called when links are clicked
     */
    fun add(interceptor: OnLinkClickInterceptor) {
        listeners.add(interceptor)
    }

    /**
     * Remove interceptor if registered
     */
    fun remove(interceptor: OnLinkClickInterceptor) {
        listeners.remove(interceptor)
    }

    /**
     * Calling this method will allow any registered link interceptor evaluate
     * the url and decide to handle it or not, if no interceptor handles it, the url
     * is opened in a chrome custom tab. Only ONE interceptor will be allowed to handle
     * the url.
     */
    fun onLinkClick(context: Context, url: String) {
        intercept(context, url, object : OnInterceptResult {
            override fun onInterceptResult(isHandled: Boolean, url: String) {
                if (!isHandled) {
                    openInCustomTab(context, url)
                }
            }
        })
    }

    private fun openInCustomTab(context: Context, url: String) {
        val theme = ThemeManager.getInstance(context).appTheme
        val builder = CustomTabsIntent.Builder()
        val primary = theme.getColor("primaryColor", ThemeColor.TRANSPARENT)
        val color = if (primary === ThemeColor.TRANSPARENT) Color.GRAY else primary.get()
        builder.setToolbarColor(color)
        val customTabsIntent = builder.build()
        var uri = Uri.parse(url)
        if (uri.scheme == null) {
            uri = Uri.parse("http://$url")
        }
        try {

            val statsEventBuilder = StatisticsEvent.Builder()
            statsEventBuilder.event("openUrl")
            statsEventBuilder.attribute("url", uri.toString())
            StatisticsManager.getInstance().logEvent(statsEventBuilder.build())
            customTabsIntent.launchUrl(context, uri)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Failed to launch custom tab")
        }
    }

    private fun intercept(context: Context, url: String, resultListener: OnInterceptResult) {
        if (listeners.size > 0) {
            untilHandled(context, url, listeners.iterator(), resultListener)
        }
        else {
            resultListener.onInterceptResult(false, url)
        }
    }

    private fun untilHandled(context: Context, url: String, iterator: MutableIterator<OnLinkClickInterceptor>, resultListener: OnInterceptResult) {
        if (!iterator.hasNext()) {
            resultListener.onInterceptResult(false, url)
            return
        }
        iterator.next().apply {
            intercept(context, url, object : OnInterceptResult{
                override fun onInterceptResult(isHandled: Boolean, url: String) {
                    if (isHandled) {
                        resultListener.onInterceptResult(isHandled, url)
                    }
                    else {
                        untilHandled(context, url, iterator, resultListener)
                    }
                }
            })
        }
    }
}
