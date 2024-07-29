package se.infomaker.iap.articleview.item.decorator

import android.view.View
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.theme.Theme

interface ItemDecorator {
    fun decorate(item: Item, itemView: View, theme: Theme)
}