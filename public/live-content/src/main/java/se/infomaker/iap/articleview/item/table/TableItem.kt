package se.infomaker.iap.articleview.item.table

import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.element.ElementItem

data class TableItem(val id: String, val themeKeys: List<String>, val caption:ElementItem?, val tableRows: List<TableRowItem>, val width:Int, val height:Int, val metadata: Map<Int, ColumnType>? = null) : Item(id) {

    override val matchingQuery = mapOf<String, String>()
    override val typeIdentifier = TableItem::class.java
    override val selectorType = "table"
}

val List<TableRowItem>.usesSpans: Boolean
    get() {
        this.forEach { row ->
            if (row.columns.any { it.rowspan != 1 }) {
                return true
            }
        }
        return false
    }

enum class ColumnType {
    TEXT, NUMBER
}