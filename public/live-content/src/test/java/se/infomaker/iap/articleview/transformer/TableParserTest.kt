package se.infomaker.iap.articleview.transformer

import android.util.Xml
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.infomaker.iap.articleview.item.table.TableItem
import se.infomaker.iap.articleview.transformer.newsml.parser.TableParser
import timber.log.Timber

@RunWith(RobolectricTestRunner::class)
class TableParserTest {


    @Test
    fun testParseTableObject() {
        val parser = Xml.newPullParser()
        parser.setInput(ResourceHelper.getResourceString("table_object.xml").byteInputStream(), "UTF-8")
        parser.next()
        Timber.d("TEST")
        val list = TableParser().parse(parser)
        (list.firstOrNull() as TableItem).let { tableItem ->
            var row = StringBuilder()
            tableItem.tableRows.forEachIndexed { i, tableRow ->

                tableRow.columns.forEachIndexed { j, cell ->
                    row.append("[$i, $j] ")
                }
                Timber.d(row.toString())
            }
        }
    }
}