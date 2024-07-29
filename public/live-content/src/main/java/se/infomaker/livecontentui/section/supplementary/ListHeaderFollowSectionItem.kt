package se.infomaker.livecontentui.section.supplementary

import org.json.JSONObject
import se.infomaker.livecontentmanager.parser.PropertyObject
import com.navigaglobal.mobile.livecontent.R

class ListHeaderFollowSectionItem(
    concept: PropertyObject,
    articleProperty: String
) : SupplementarySectionItem(concept, articleProperty, "list_header_follow", "list_header_follow", ITEM_CONTEXT) {

    override fun getId() = "header:${super.getId()}"

    override fun defaultTemplate() = R.layout.default_list_header_follow

    override fun isClickable() = false

    companion object {
        private val ITEM_CONTEXT
            get() = JSONObject().apply {
                put("supplementary", SupplementarySectionItemType.HEADER.toString().toLowerCase())
            }
    }
}