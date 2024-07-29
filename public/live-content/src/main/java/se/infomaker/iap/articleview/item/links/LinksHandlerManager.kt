package se.infomaker.iap.articleview.item.links

import android.content.Context
import timber.log.Timber

/**
 * Central point to register link handlers and handle links
 */
object LinksHandlerManager : LinkHandler{
    private val handlers = mutableMapOf<String, LinkHandler>()

    init {
        registerHandler(TextHtmlLinkHandler(), "text/html")
    }

    /**
     * Open link using registered handler
     */
    override fun open(context: Context, moduleId: String, link: Link, title: String) {
        val linkHandler = handlers[link.type]
        if (linkHandler != null) {
            linkHandler.open(context, moduleId, link, title)
        }
        else {
            Timber.w("Unhandled link: " + link)
        }
    }

    /**
     * Check if the link can be opened
     */
    fun canHandle(link: Link) : Boolean{
        return handlers.containsKey(link.type)
    }

    /**
     * Register handler for link type
     */
    fun registerHandler(handler: LinkHandler, type: String) {
        handlers[type] = handler
    }
}