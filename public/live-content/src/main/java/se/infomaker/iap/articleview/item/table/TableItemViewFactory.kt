package se.infomaker.iap.articleview.item.table

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_TEXT_END
import android.view.View.TEXT_ALIGNMENT_TEXT_START
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import se.infomaker.frtutilities.ResourceManager
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.item.element.ElementItemViewFactory
import se.infomaker.iap.articleview.item.table.TableRowType.*
import se.infomaker.iap.articleview.util.UI
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.size.ThemeSize
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.iap.theme.ktx.brandColor


class TableItemViewFactory : ItemViewFactory {

    private val elementItemViewFactory = ElementItemViewFactory()

    override fun typeIdentifier(): Any = TableItem::class.java

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View =
        LayoutInflater.from(parent.context).inflate(resourceManager.getLayoutIdentifier("table_layout"), parent, false) as HorizontalScrollView

    private fun borderColor(styleName: String, theme: Theme): Int =
        theme.getColor(styleName, theme.getColor(PRIMARY_COLOR, ThemeColor.BLACK)).get()

    private fun backgroundColor(styleName: String, theme: Theme): Int =
        theme.getColor(styleName, theme.getColor(DEFAULT_BACKGROUND, ThemeColor.WHITE)).get()

    private fun bodyRowColor(styleName:String, theme: Theme): Int =
        theme.getColor(styleName, theme.getColor(TABLE_CONTAINER_BACKGROUND, ThemeColor(Color.parseColor(if (styleName == TABLE_ODD_ROW_BACKGROUND) { "#fafafa" } else { "#eeeeee" })))).get()

    private fun headerBackgroundColor(theme: Theme) =
        theme.getColor(TABLE_HEADER_BACKGROUND, theme.getColor(TABLE_CONTAINER_BACKGROUND, theme.getColor(DEFAULT_BACKGROUND, ThemeColor.WHITE))).get()

    private fun sizeInPxFromTheme(key: String, theme: Theme, fallbackSize: ThemeSize = ThemeSize.DEFAULT) =
        theme.getSize(key, fallbackSize).sizePx.toInt()

    private fun createShape(color: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        setColor(color)
        setStroke(1, color)
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        if (item !is TableItem) return
        item.let { table ->
            val primaryColor = theme.brandColor.get()
            val tableBackground = backgroundColor(TABLE_BACKGROUND, theme)
            val tableContainerBackground = backgroundColor(TABLE_CONTAINER_BACKGROUND, theme)
            val tableHeaderBorderColor = borderColor(TABLE_HEADER_BORDER, theme)
            val tableFooterBorder = borderColor(TABLE_FOOTER_BORDER, theme)
            val tableOddRowBackground = bodyRowColor(TABLE_ODD_ROW_BACKGROUND, theme)
            val tableEvenRowBackground = bodyRowColor(TABLE_EVEN_ROW_BACKGROUND, theme)

            val tableMarginTop = sizeInPxFromTheme(TABLE_MARGIN_TOP, theme, ThemeSize(0f))
            val tableMarginBottom = sizeInPxFromTheme(TABLE_MARGIN_BOTTOM, theme, ThemeSize(0f))
            val tableMarginLeft = sizeInPxFromTheme(TABLE_MARGIN_LEFT, theme, ThemeSize(0f))
            val tableMarginRight = sizeInPxFromTheme(TABLE_MARGIN_RIGHT, theme, ThemeSize(0f))

            val tablePaddingTop = sizeInPxFromTheme(TABLE_PADDING_TOP, theme)
            val tablePaddingBottom = sizeInPxFromTheme(TABLE_PADDING_BOTTOM, theme)
            val tablePaddingLeft = sizeInPxFromTheme(TABLE_PADDING_LEFT, theme)
            val tablePaddingRight = sizeInPxFromTheme(TABLE_PADDING_RIGHT, theme)

            val tableCellPaddingTop = sizeInPxFromTheme(TABLE_CELL_PADDING_TOP, theme)
            val tableCellPaddingBottom = sizeInPxFromTheme(TABLE_CELL_PADDING_BOTTOM, theme)
            val tableCellPaddingLeft = sizeInPxFromTheme(TABLE_CELL_PADDING_LEFT, theme)
            val tableCellPaddingRight = sizeInPxFromTheme(TABLE_CELL_PADDING_RIGHT, theme)

            val tableCaptionPaddingTop = sizeInPxFromTheme(TABLE_CELL_CAPTION_TOP, theme)
            val tableCaptionPaddingBottom = sizeInPxFromTheme(TABLE_CELL_CAPTION_BOTTOM, theme)
            val tableCaptionPaddingLeft = sizeInPxFromTheme(TABLE_CELL_CAPTION_LEFT, theme)
            val tableCaptionPaddingRight = sizeInPxFromTheme(TABLE_CELL_CAPTION_RIGHT, theme)

            val tableHeaderBorderSize = sizeInPxFromTheme(TABLE_HEADER_BORDER, theme, ThemeSize(1f))
            val tableFooterBorderSize =  sizeInPxFromTheme(TABLE_FOOTER_BORDER, theme, ThemeSize(1f))
            val tableBorderSize = sizeInPxFromTheme(TABLE_BORDER, theme, ThemeSize(3f))

            view.findViewById<HorizontalScrollView>(R.id.view)?.apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setPadding(tablePaddingLeft, tablePaddingTop, tablePaddingRight, tablePaddingBottom)
                setBackgroundColor(tableBackground)
            }

            view.findViewById<CardView>(R.id.table_container)?.apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
                    setContentPadding(tableMarginLeft, tableMarginTop, tableMarginRight, tableMarginBottom)
                }
                setBackgroundColor(tableContainerBackground)
            }

            view.findViewById<LinearLayout>(R.id.table_header_border).apply {
                background = createShape(tableHeaderBorderColor)
                setPadding(0, tableBorderSize, 0, 0)
            }

            view.findViewById<ThemeableTextView>(R.id.caption)?.let { textView ->
                table.caption?.let {
                    elementItemViewFactory.themeView(textView, it, theme)
                    if (theme.getText(it.themeKeys, null) == null) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    } else {
                        textView.themeKeys = it.themeKeys
                        textView.apply(theme)
                    }
                }
                textView.setPadding(tableCaptionPaddingLeft, tableCaptionPaddingTop, tableCaptionPaddingRight, tableCaptionPaddingBottom)
                textView.setBackgroundColor(tableContainerBackground)
            }

            val usesSpans = table.tableRows.usesSpans
            table.tableRows.forEachIndexed { index, row ->
                row.columns.forEachIndexed { columnIndex, cell ->
                    cell.elementItem?.run {
                        view.findViewWithTag<ThemeableTextView>(cell.id)?.let { textView ->

                            elementItemViewFactory.themeView(textView, this, theme)
                            textView.setPadding(tableCellPaddingLeft, tableCellPaddingTop,
                                    tableCellPaddingRight, tableCellPaddingBottom)

                            when(cell.type) {
                                HEAD -> {
                                    textView.parent.apply {
                                        this as LinearLayout
                                        this.background = createShape(tableHeaderBorderColor)
                                        this.setPadding(0, 0, 0, tableHeaderBorderSize)
                                    }
                                    if (theme.getText(cell.themeKeys, null) == null) {
                                        textView.apply {
                                            setTextColor(primaryColor)
                                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                                        }
                                    } else {
                                        textView.themeKeys = cell.themeKeys
                                        textView.apply(theme)
                                    }
                                    textView.setBackgroundColor(headerBackgroundColor(theme))
                                }
                                BODY -> {
                                    if (theme.getText(cell.themeKeys, null) == null) {
                                        textView.apply {
                                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                                        }
                                    } else {
                                        textView.themeKeys = cell.themeKeys
                                        textView.apply(theme)
                                    }
                                    item.metadata?.get(columnIndex)?.let {
                                        textView.textAlignment = when (it) {
                                            ColumnType.TEXT -> TEXT_ALIGNMENT_TEXT_START
                                            ColumnType.NUMBER -> TEXT_ALIGNMENT_TEXT_END
                                        }
                                    }
                                    if (!usesSpans) {
                                        textView.setBackgroundColor(if (index % 2 == 0) tableOddRowBackground else tableEvenRowBackground)
                                    }
                                }
                                FOOT -> {
                                    textView.parent.apply {
                                        this as LinearLayout
                                        this.background = createShape(tableFooterBorder)
                                        this.setPadding(0, tableFooterBorderSize, 0, 0)
                                    }
                                    if (theme.getText(cell.themeKeys, null) == null) {
                                        textView.apply {
                                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                                        }
                                    } else {
                                        textView.themeKeys = cell.themeKeys
                                        textView.apply(theme)
                                    }
                                    textView.setBackgroundColor(tableContainerBackground)
                                }
                            }
                        }
                    }
                }
            }
        }
        val tableManager = CustomTableManager(item.tableRows)
        view.doOnPreDraw {
            view.findViewById<GridLayout>(R.id.table).children.forEach {
                it.minimumWidth = (calculateMinimumCellWidth(tableManager.tableWidth, view)).toInt()
            }
        }
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        view as HorizontalScrollView
        val tableItem = item as TableItem
        tableItem.let { table ->
            val tableManager = CustomTableManager(table.tableRows)
            val cells = mutableListOf<CellDimensions>()
            val searched = mutableListOf<String>()
            for ((y, row) in tableManager.table.withIndex()) {
                for ((x, cell) in row.withIndex()) {
                    cell?.run {
                        val results = searched.filter { this.id == it }
                        if (results.isEmpty()) {
                            searched.add(id)
                            val size = tableManager.getCellDimensions(this.id)
                            cells.add(CellDimensions(this.id, x, y, size.first, size.second, this.data))
                        }
                    }
                }
            }

            view.findViewById<ThemeableTextView>(R.id.caption).apply {
                text = table.caption?.text
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                table.caption?.let {
                    elementItemViewFactory.bindView(it, this, moduleId)
                    return@apply
                }
                layoutParams = LinearLayout.LayoutParams(0, 0)
            }

            view.findViewById<GridLayout>(R.id.table).apply {
                this.removeAllViews()
                columnCount = tableManager.tableWidth
                rowCount = tableManager.tableHeight
                orientation = GridLayout.HORIZONTAL
                cells.forEach {
                    addView(
                        LinearLayout(view.context).apply {
                            layoutParams = GridLayout.LayoutParams().apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    GridLayout.LayoutParams.MATCH_PARENT,
                                    GridLayout.LayoutParams.WRAP_CONTENT
                                )
                                columnSpec = GridLayout.spec(it.x, it.w, GridLayout.FILL)
                                rowSpec = GridLayout.spec(it.y, it.h, GridLayout.FILL)
                                minimumHeight = UI.dp2px(32f * it.h).toInt()
                            }
                            orientation = LinearLayout.VERTICAL

                            val cellContent = ThemeableTextView(this.context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                tag = it.elementItem?.id
                                text = it.elementItem?.text?.toString() ?: ""
                            }
                            it.elementItem?.run {
                                elementItemViewFactory.bindView(
                                    this,
                                    cellContent,
                                    moduleId
                                )
                            }
                            addView(cellContent)
                        })
                }
            }
        }
    }

    private fun calculateMinimumCellWidth(tableWidth: Int, view: View): Float =
        (view.measuredWidth.toFloat() - (view.paddingStart + view.paddingEnd)) / tableWidth

    private companion object {
        const val PRIMARY_COLOR = "primaryColor"
        const val DEFAULT_BACKGROUND = "defaultBackground"
        const val TABLE_CONTAINER_BACKGROUND = "tableContainerBackground"
        const val TABLE_ODD_ROW_BACKGROUND = "tableOddRowBackground"
        const val TABLE_EVEN_ROW_BACKGROUND = "tableEvenRowBackground"
        const val TABLE_BACKGROUND = "tableBackground"
        const val TABLE_HEADER_BACKGROUND = "tableHeaderBackground"
        const val TABLE_HEADER_BORDER = "tableHeaderBorder"
        const val TABLE_FOOTER_BORDER = "tableFooterBorder"
        const val TABLE_MARGIN_LEFT = "tableMarginLeft"
        const val TABLE_MARGIN_RIGHT = "tableMarginRight"
        const val TABLE_MARGIN_TOP = "tableMarginTop"
        const val TABLE_MARGIN_BOTTOM = "tableMarginBottom"
        const val TABLE_PADDING_LEFT = "tablePaddingLeft"
        const val TABLE_PADDING_RIGHT = "tablePaddingRight"
        const val TABLE_PADDING_TOP = "tablePaddingTop"
        const val TABLE_PADDING_BOTTOM = "tablePaddingBottom"
        const val TABLE_BORDER = "tableBorder"

        const val TABLE_CELL_PADDING_TOP = "tableCellPaddingTop"
        const val TABLE_CELL_PADDING_BOTTOM = "tableCellPaddingBottom"
        const val TABLE_CELL_PADDING_LEFT = "tableCellPaddingLeft"
        const val TABLE_CELL_PADDING_RIGHT = "tableCellPaddingRight"

        const val TABLE_CELL_CAPTION_TOP = "tableCellCaptionTop"
        const val TABLE_CELL_CAPTION_BOTTOM = "tableCellCaptionBottom"
        const val TABLE_CELL_CAPTION_LEFT = "tableCellCaptionLeft"
        const val TABLE_CELL_CAPTION_RIGHT = "tableCellCaptionRight"
    }
}