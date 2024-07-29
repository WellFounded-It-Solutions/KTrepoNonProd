package se.infomaker.iap.articleview.item.template.binder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import se.infomaker.iap.articleview.item.Item

object TemplateBinder : Binder {
    private val binderMap = mutableMapOf<Class<out View>, Binder>()
    private val unsupportedMap = mutableSetOf<Class<out View>>()

    init {
        binderMap[TextView::class.java] = TextViewBinder()
        binderMap[ImageView::class.java] = ImageViewBinder()
    }

    override fun bind(view: View, item: Item?) {
        if (unsupportedMap.contains(view.javaClass)) {
            return
        }
        val binder = binderMap[view.javaClass]
        if (binder == null) {
            resolveBinder(view)
            bind(view, item)
            return
        }
        binder.bind(view, item)
    }

    private fun resolveBinder(view: View) {
        binderMap.forEach { (viewClass, binder) ->
            if (viewClass.isInstance(view)) {
                binderMap[view.javaClass] = binder
                return
            }
        }
        unsupportedMap.add(view.javaClass)
    }
}