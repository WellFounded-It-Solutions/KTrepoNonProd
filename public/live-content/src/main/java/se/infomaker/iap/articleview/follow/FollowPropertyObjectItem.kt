package se.infomaker.iap.articleview.follow

import android.view.View
import se.infomaker.iap.articleview.item.Item
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.streamviewer.stream.SubscriptionUtil

data class FollowPropertyObjectItem(val propertyObject: PropertyObject, val template: String, override val selectorType: String, val articleProperty: String, val value: String, val title: String) : Item(propertyObject.id) {

    override val matchingQuery = mapOf<String, String>()
    override val typeIdentifier = createTemplateIdentifier(template)

    val following: Boolean
        get() = SubscriptionUtil.hasMatchSubscription(articleProperty, value)

    var onClick: View.OnClickListener? = null

    companion object {
        fun createTemplateIdentifier(template: String): Any {
            return "${FollowPropertyObjectItem::class.java.canonicalName}-$template"
        }
    }
}