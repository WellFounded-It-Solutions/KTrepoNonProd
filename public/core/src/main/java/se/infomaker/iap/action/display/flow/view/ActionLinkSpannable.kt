package se.infomaker.iap.action.display.flow.view

import android.content.Context
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.view.View
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.openUri
import timber.log.Timber

class ActionLinkSpannable(val context: Context, val valueProvider: ValueProvider, val moduleIdentifier: String, val linkAttributes: Map<String, String>?) : ClickableSpan() {
    override fun onClick(widget: View) {
        linkAttributes?.get("href")?.let { url ->
            if (TextUtils.isEmpty(url)) {
                return
            }
            try {
                widget.context.openUri(moduleIdentifier, url, valueProvider)
            } catch (e: Throwable) {
                Timber.e(e, "Failed to open link")
            }
        }
    }
}