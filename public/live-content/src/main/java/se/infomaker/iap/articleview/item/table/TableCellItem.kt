package se.infomaker.iap.articleview.item.table


import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.element.ElementItem

data class TableCellItem(var id: String, var themeKeys:List<String>?, var type:TableRowType, var rowspan: Int = 1, var colspan: Int = 1, var elementItem: ElementItem?) : Item(id) {

    override val matchingQuery = mapOf<String, String>()
    override val typeIdentifier = TableItem::class.java
    override val selectorType = "tableCellItem"
}