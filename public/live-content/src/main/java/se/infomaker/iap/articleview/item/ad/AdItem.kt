package se.infomaker.iap.articleview.item.ad

import com.google.gson.JsonObject
import org.json.JSONObject
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.preprocessor.reproducibleUuid
import se.infomaker.iap.articleview.view.FocusAware
import se.infomaker.iap.articleview.view.FocusState

data class AdItem(
    val adService: String,
    val adConfiguration: JsonObject,
    val content: JSONObject
) : Item(adConfiguration.reproducibleUuid.toString()), FocusAware {

    override var focusState: FocusState = FocusState.OUT_OF_FOCUS

    override val typeIdentifier = AdItem::class.java
    override val matchingQuery: Map<String, String> = mapOf()
    override val selectorType: String = "ad"
}