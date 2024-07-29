package se.infomaker.iap.articleview.item.map

import se.infomaker.iap.articleview.item.Item

data class MapItem(val id: String, var lat:String, var lng: String, var zoom:String) : Item(id) {

    override val typeIdentifier = MapItem::class.java
    override val matchingQuery: Map<String, String> = mapOf()
    override val selectorType: String = "map"
    var aspectRatio:String? = null
    var interaction:String? = null
}