package se.infomaker.iap.articleview.item.table

import se.infomaker.iap.articleview.item.element.ElementItem


data class Placeholder(val id:String, val x:Int, val y:Int, var data:ElementItem?)

class CustomTableManager(private val list: List<TableRowItem>) {
    var tableHeight: Int = 0
    var tableWidth: Int = 0
    var table:MutableList<MutableList<Placeholder?>>

    init {  table = prepareTable() }

    fun getCellDimensions(id:String): Pair<Int, Int> {
        var xCount = 0
        var yCount = 0
        var found = false
        var lastX = 0
        for(i in 0 until tableHeight) {
            for(j in 0 until tableWidth) {
                table[i][j]?.run {
                    if (this.id == id && !found) {
                        xCount++
                        lastX = j
                    }
                    if (this.id != id && xCount != 0) {
                       found = true
                    }
                }
                if (found) {
                    break
                }
            }
            table[i][lastX]?.run {
                if (this.id == id) {
                    yCount++
                }
            }
        }
        return Pair(xCount, yCount)
    }

    private fun calculateTableHeight(rows: List<TableRowItem>): Int {
        return rows.size
    }

    private fun calculateTableWidth(rows: List<TableRowItem>): Int {
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

    private fun createEmptyTable(width:Int, height:Int): MutableList<MutableList<Placeholder?>> {
            val newTable = mutableListOf<MutableList<Placeholder?>>()
            var y = 0
            while (y < height) {
                var x = 0
                val row = mutableListOf<Placeholder?>()
                while (x < width) {
                    row.add(null)
                    x++
                }
                newTable.add(row)
                y++
            }
            return newTable
    }

    private fun prepareTable(): MutableList<MutableList<Placeholder?>> {
        tableWidth = calculateTableWidth(list)
        tableHeight = calculateTableHeight(list)
        val newTable = createEmptyTable(tableWidth, tableHeight)
        var localX:Int
        list.forEach{ tableRow ->
            repeat(tableRow.columns.size) {
                for ((y, row) in newTable.withIndex()) {
                    localX = 0
                    for ((x, dataCell) in row.withIndex()) {
                        if (dataCell == null) {
                            if (localX < list[y].columns.size) {
                                val cell = list[y].columns[localX]
                                localX++
                                (0 until cell.rowspan).forEachIndexed { _, ly ->
                                    (0 until cell.colspan).forEachIndexed { _, lx ->
                                        newTable[y + ly][x + lx] =
                                            Placeholder(cell.uuid, y + ly, x + lx, cell.elementItem)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return newTable
    }
}