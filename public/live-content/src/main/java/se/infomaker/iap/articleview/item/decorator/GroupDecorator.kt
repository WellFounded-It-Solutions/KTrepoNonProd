package se.infomaker.iap.articleview.item.decorator

import android.view.View
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor

data class GroupDecorator(val attributes: Map<String, String>) : ItemDecorator {
    companion object {
        val TYPE_KEY = "type"
    }
    override fun decorate(item: Item, itemView: View, theme: Theme) {
        backgroundColor()?.let { val color = theme.getColor(it, ThemeColor.TRANSPARENT)
            if (color != ThemeColor.TRANSPARENT) {
                itemView.setBackgroundColor(color.get())
            }
        }
    }

    private fun backgroundColor() : String? = if (attributes.containsKey(TYPE_KEY)) "${attributes[TYPE_KEY]}Background" else null
}