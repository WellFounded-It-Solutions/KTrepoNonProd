package se.infomaker.livecontentui.section

import org.json.JSONObject

/**
 * [SectionItem]s which implement this interface can provide a
 * context for use with ContentPresentation.
 */
interface ContentPresentationContext {
    var context:JSONObject?
}