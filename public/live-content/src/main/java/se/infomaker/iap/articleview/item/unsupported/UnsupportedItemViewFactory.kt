package se.infomaker.iap.articleview.item.unsupported

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.util.UI
import se.infomaker.iap.theme.Theme

class UnsupportedItemViewFactory : ItemViewFactory {
    override fun typeIdentifier(): Any {
        return UnsupportedItem::class.java
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        if (isDebuggable(parent.context)) {
            val textView = TextView(parent.context)
            val padding = UI.dp2px(16F).toInt()
            textView.setPadding(padding, padding, padding, padding)
            textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            return textView
        } else {
            View(parent.context).let {
                it.layoutParams = ViewGroup.LayoutParams(0, 0)
                return it
            }
        }
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        if (isDebuggable(view.context) && view is TextView) {
            view.setTextColor(Color.RED)
        }
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        if (isDebuggable(view.context) && view is TextView && item is UnsupportedItem) {
            view.text = "Unsupported item type: ${item.type}"
        }
    }

    fun isDebuggable(context: Context): Boolean {
        return 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    }
}