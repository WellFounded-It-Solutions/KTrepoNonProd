package se.infomaker.iap.articleview.item.template.binder

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.element.ElementItem

class TextViewBinder : Binder {
    override fun bind(view: View, item: Item?) {
        if (item is ElementItem && view is TextView) {
            view.setText(item.text, TextView.BufferType.SPANNABLE)
            view.movementMethod = LinkMovementMethod.getInstance()
        } else {
            view.visibility = View.GONE
        }
    }
}