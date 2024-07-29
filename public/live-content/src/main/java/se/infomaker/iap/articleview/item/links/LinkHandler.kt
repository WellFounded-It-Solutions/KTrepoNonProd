package se.infomaker.iap.articleview.item.links

import android.content.Context

interface LinkHandler {
    /**
     * Open link
     */
    fun open(context: Context, moduleId: String, link: Link, title: String)
}