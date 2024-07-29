package se.infomaker.iap.articleview

import android.content.Context
import android.view.View
import se.infomaker.iap.articleview.item.Item

interface OnPrepareView {
    /**
     * Called when the content is about to be displayed.
     */
    fun onPreHeat(item: Item, context: Context)

    /**
     * Called when the item is getting rendered.
     */
    fun onPrepare(item: Item, view: View)

    /**
     * Called when the item is getting recycled
     */
    fun onPrepareCancel(item: Item, view: View)
}