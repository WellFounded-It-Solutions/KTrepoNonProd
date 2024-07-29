package se.infomaker.iap.articleview.item.table

import se.infomaker.iap.articleview.item.Item

data class TableRowItem (val id: String,
                         val rowType: TableRowType,
                         val columns: List<TableCellItem>) : Item(id) {
    override val matchingQuery = mapOf<String, String>()
    override val typeIdentifier = TableRowItem::class.java
    override val selectorType = "tableRow"
}

enum class TableRowType {
    HEAD, BODY, FOOT
}

