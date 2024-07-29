package se.infomaker.iap.articleview.item.element

import android.net.Uri
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View

class LinkSpannable(val linkAttributes: Map<String, String>?) : ClickableSpan() {
    override fun onClick(widget: View) {
        val url = linkAttributes?.get("href")
        if (TextUtils.isEmpty(url)) {
            return
        }
        var uri = Uri.parse(url)
        if (uri.scheme == null) {
            uri = Uri.parse("http://$url")
        }
        OnLinkClickManager.onLinkClick(widget.context, uri.toString())
    }
}

fun URLSpan.toLinkSpannable(): LinkSpannable {
    return LinkSpannable(mapOf("href" to url))
}