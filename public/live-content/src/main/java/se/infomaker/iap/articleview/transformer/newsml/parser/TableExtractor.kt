package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.table.TableCellItem
import se.infomaker.iap.articleview.item.table.TableRowType
import java.util.*

interface TableExtractor {
    val rowType: TableRowType
    val elementName: String
    fun extract(parser: XmlPullParser, rowType: TableRowType):TableCellItem
}

abstract class TableExtractorBase : TableExtractor {

    var attributes : Map<String, String>? = null

    override fun extract(parser: XmlPullParser, rowType: TableRowType): TableCellItem {
        val attributes = parser.getAttributes()
        val uuid = attributes["id"] ?: UUID.randomUUID().toString()
        return TableCellItem(
                uuid,
                null,
                rowType,
                attributes["rowspan"]?.toInt() ?: 1,
                attributes["colspan"]?.toInt() ?: 1, null)
    }
}

class TableHeadExtractor : TableExtractorBase() {
    override val rowType: TableRowType = TableRowType.HEAD
    override val elementName = "th"
}

class TableBodyExtractor : TableExtractorBase() {
    override val rowType: TableRowType = TableRowType.BODY
    override val elementName = "td"
}

class TableFootExtractor : TableExtractorBase() {
    override val rowType: TableRowType = TableRowType.FOOT
    override val elementName = "td"
}