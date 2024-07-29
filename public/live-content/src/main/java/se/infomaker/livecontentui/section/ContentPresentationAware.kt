package se.infomaker.livecontentui.section

import org.json.JSONObject

/**
 * [SectionItem]s which implement this interface can provide a
 * context for use with ContentPresentation.
 */
interface ContentPresentationAware {
    var context:JSONObject?
    val content: JSONObject?
}