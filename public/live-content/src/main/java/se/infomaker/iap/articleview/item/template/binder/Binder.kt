package se.infomaker.iap.articleview.item.template.binder

import android.view.View
import se.infomaker.iap.articleview.item.Item

interface Binder {
    fun bind(view: View, item: Item?)
}