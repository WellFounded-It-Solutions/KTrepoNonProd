package se.infomaker.livecontentui

import org.json.JSONObject

/**
 * Callback to be invoked whenever the presentation context of a [ViewBehaviour] key that handles
 * content presentation changes.
 */
interface OnPresentationContextChangedListener {

    /**
     * Called when the presentation context changes.
     *
     * [changes] contains the id of the key (usually [se.infomaker.livecontentui.section.SectionItem.getId]
     * or [se.infomaker.livecontentmanager.parser.PropertyObject.id] that has changed and the
     * patchable presentation context that is associated with said key.
     */
    fun onPresentationContextChanged(changes: Map<String, @JvmSuppressWildcards JSONObject>)
}