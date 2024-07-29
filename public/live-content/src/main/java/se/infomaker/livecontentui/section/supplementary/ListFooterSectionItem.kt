package se.infomaker.livecontentui.section.supplementary

import com.navigaglobal.mobile.livecontent.R
import org.json.JSONObject
import se.infomaker.livecontentmanager.parser.PropertyObject

class ListFooterSectionItem(
    concept: PropertyObject,
    articleProperty: String,
) : SupplementarySectionItem(concept, articleProperty, "list_footer", "list_footer", ITEM_CONTEXT) {

    override fun getId() = "footer:${super.getId()}"

    override fun defaultTemplate() = R.layout.default_list_footer

    override fun isClickable() = true

    companion object {
        private val ITEM_CONTEXT
            get() = JSONObject().apply {
                put("supplementary", SupplementarySectionItemType.FOOTER.toString().toLowerCase())
            }
    }
}
