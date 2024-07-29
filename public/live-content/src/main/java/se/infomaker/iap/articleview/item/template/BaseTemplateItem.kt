package se.infomaker.iap.articleview.item.template

import se.infomaker.iap.articleview.item.Item

interface BaseTemplateItem {
    val id: String
    val template: String
    val items: Map<String, Item>
    val boundViews: List<String>
}