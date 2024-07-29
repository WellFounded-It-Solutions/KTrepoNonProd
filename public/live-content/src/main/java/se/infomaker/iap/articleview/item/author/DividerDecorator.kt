package se.infomaker.iap.articleview.item.author

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import se.infomaker.frtutilities.ResourceManager
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.decorator.ItemDecorator
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.size.ThemeSize

class DividerDecorator(val template: String?, val themeKey: String, val before: Boolean, val after: Boolean) : ItemDecorator {
    override fun decorate(item: Item, itemView: View, theme: Theme) {
        itemView.putDividers(DividerDecorationConfig(drawableResourceName = template, themeKey = themeKey, before = before, after = after))
    }
}

/**
 * @param drawableResourceName the name of the resource to use as divider
 * @param themeKey Type of the item, used for getting the correct theme sizes
 * @param before Should we draw a line before the item?
 * @param after Should we draw a line after the item?
 */
data class DividerDecorationConfig(val drawableResourceName: String?, var theme: Theme? = null, val themeKey: String, val before: Boolean = true, val after: Boolean = true)

fun View.putDividers(config: DividerDecorationConfig?) {
    this.setTag(R.id.dividerDecoration, config)
}

fun View.clearDividers() {
    this.setTag(R.id.dividerDecoration, null)
}

/**
 * This is a [RecyclerView.ItemDecoration] that paints dividers between RecyclerView items
 * @param dividerDrawable This is the drawable to use as divider if no drawableResourceName is provided from config
 * @param fallbackTheme This is the theme to use if no theme is provided from [DividerDecorationConfig]
 */
class RecyclerViewDividerDecorator @JvmOverloads constructor(
    private val dividerDrawable: Drawable,
    private val fallbackTheme: Theme?,
    private val resourceManager: ResourceManager? = null
) : RecyclerView.ItemDecoration() {
    val drawableMap = mutableMapOf<String, Drawable>()
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val viewsToDecorate: List<View> = (0 until parent.childCount)
                .map { parent.getChildAt(it) }
                .filter { it.getTag(R.id.dividerDecoration) is DividerDecorationConfig }

        viewsToDecorate.forEach { view ->
            val decorationConfig = view.getTag(R.id.dividerDecoration) as DividerDecorationConfig

            val theme = decorationConfig.theme ?: fallbackTheme

            val suffixItems = listOf("${decorationConfig.themeKey}Separator", "separator").suffixItems("Color")
            val drawableKey = "${decorationConfig.drawableResourceName}:$suffixItems"

            val divider: Drawable = if (drawableMap.containsKey(drawableKey)) {
                drawableMap[drawableKey] ?: dividerDrawable
            } else {
                val divider: Drawable = decorationConfig.drawableResourceName?.let { drawableResourceName ->
                    val identifier = getDrawableResourceIdentifier(view.context, drawableResourceName)
                    if (identifier == 0) {
                        dividerDrawable
                    } else {
                        ResourcesCompat.getDrawable(view.context.resources, identifier, null) ?: dividerDrawable
                    }
                } ?: dividerDrawable

                theme?.getColor(suffixItems, null)?.get()?.let { color ->
                    DrawableCompat.setTint(divider, color)
                }
                drawableMap.put(drawableKey, divider)

                divider
            }

            val inset =
                    theme?.getSize(listOf("${decorationConfig.themeKey}Separator", "separator").suffixItems("Inset"), ThemeSize(0f))?.sizePx?.toInt()
                            ?: 0

            val left = parent.paddingLeft + inset
            val right = parent.width - parent.paddingRight - inset

            fun drawLine(top: Int) {
                val bottom = top + divider.intrinsicHeight
                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }

            if (decorationConfig.before) {
                drawLine(top = view.top)
            }
            if (decorationConfig.after) {
                drawLine(top = view.bottom)
            }
        }
    }

    private fun getDrawableResourceIdentifier(context: Context, drawableName: String): Int {
        resourceManager?.let {
            return it.getDrawableIdentifier(drawableName)
        }
        return context.resources.getIdentifier(drawableName, "drawable", null)
    }

    private fun List<String?>.suffixItems(suffix: String): List<String> = this.filterNotNull().map { it + suffix }
}
