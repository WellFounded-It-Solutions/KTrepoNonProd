package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.table.ColumnType
import se.infomaker.iap.articleview.item.table.TableCellItem
import se.infomaker.iap.articleview.item.table.TableItem
import se.infomaker.iap.articleview.item.table.TableRowItem
import se.infomaker.iap.articleview.item.table.TableRowType
import java.util.*

class TableParser : ItemParser {

    private val tableExtractors = mutableMapOf<String, TableExtractor>()
    private val elementParser = ElementParser()

    init {
        register(TableHeadExtractor())
        register(TableBodyExtractor())
        register(TableFootExtractor())
    }

    private fun register(tableExtractor: TableExtractor) {
        tableExtractors[tableExtractor.elementName] = tableExtractor
    }

    override fun parse(parser: XmlPullParser): List<TableItem> {
        val rowItems = mutableListOf<TableRowItem>()
        var listItems = mutableListOf<TableCellItem>()
        val attributes = parser.getAttributes()
        var rowType = TableRowType.BODY
        var done = false
        var caption:ElementItem? = null
        var keys = listOf<String>()
        var metadata: Map<Int, ColumnType>? = null
        while (!done) {
           parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when(parser.name) {
                        "meta" -> {
                            metadata = extractTableMetaData(parser)
                        }
                        "thead" -> {
                            rowType = TableRowType.HEAD
                            keys = listOf("tableHeader", "element", "default")
                        }
                        "tfoot" -> {
                            rowType = TableRowType.FOOT
                            keys = listOf("tableFooter", "element", "default")
                        }
                        "tbody" -> {
                            rowType = TableRowType.BODY
                            keys = listOf("tableBody", "element", "default")
                        }
                        "caption" -> {
                            caption = elementParser.extractSingleElement(parser, parser.name)
                            val themeKeys = caption.themeKeys.toMutableList()
                            themeKeys.add(0, "tableCaption")
                            caption.themeKeys = themeKeys
                        }
                    }
                    tableExtractors[parser.name]?.extract(parser, rowType)?.run {
                        this.apply {
                            elementItem = elementParser.extractSingleElement(parser, parser.name)
                            themeKeys = keys
                        }
                        listItems.add(this)
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "tr") {
                        val uuid = attributes["id"] ?: UUID.randomUUID().toString()
                        rowItems.add(TableRowItem(uuid, rowType, listItems))
                        listItems = mutableListOf()
                    }
                    if (parser.name == "object") {
                        done = true
                    }
                }
            }
        }
        val uuid = attributes["id"] ?: UUID.randomUUID().toString()
        return listOf(TableItem(uuid, listOf("table"), caption, rowItems, tableWidth(rowItems), tableHeight(rowItems), metadata))
    }

    private fun extractTableMetaData(parser: XmlPullParser): Map<Int, ColumnType> {
        val metadata = mutableMapOf<Int, ColumnType>()
        var done = false
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "col") {
                        val attributes = parser.getAttributes()
                        val id = attributes["id"]?.toInt()
                        val format = attributes["format"]?.let {
                            when (it.toUpperCase()){
                                ColumnType.NUMBER.name -> ColumnType.NUMBER
                                else -> ColumnType.TEXT
                            }
                        } ?: ColumnType.TEXT

                        id?.let {
                            metadata.put(it, format)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "meta") {
                        done = true
                    }
                }
            }
        }
        return metadata
    }

    private fun tableHeight(rows: MutableList<TableRowItem>): Int {
        return rows.size
    }

    private fun tableWidth(rows: MutableList<TableRowItem>): Int {
        val mergeColumns = rows[0].columns.filter { cell ->
            cell.colspan != 0
        }

        if (mergeColumns.isEmpty()) {
            return rows[0].columns.size
        }

        var j = 0
        mergeColumns.forEach { cell ->
            j += cell.colspan
        }
        rows[0].columns.size + j - mergeColumns.size
        return  rows[0].columns.size + j - mergeColumns.size
    }
}