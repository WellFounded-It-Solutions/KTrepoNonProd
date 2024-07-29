package se.infomaker.iap.articleview.item.ad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.ItemViewFactory
import se.infomaker.iap.articleview.item.ItemViewFactory2
import se.infomaker.iap.articleview.view.LifecycleProxy
import se.infomaker.iap.theme.Theme
import se.infomaker.library.OnAdFailedListener

class AdItemViewFactory(private val adViewProvider: AdViewProvider) : ItemViewFactory, ItemViewFactory2 {

    override fun typeIdentifier() = TYPE_IDENTIFIER

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme): View {
        return createView(parent, resourceManager, theme) {}
    }

    override fun createView(parent: ViewGroup, resourceManager: ResourceManager, theme: Theme, onFailure: () -> Unit): View {
        var identifier = resourceManager.getLayoutIdentifier("ad_layout")
        if (identifier == 0) {
            identifier = R.layout.default_ad_layout
        }
        return LayoutInflater.from(parent.context).inflate(identifier, parent, false)
    }

    override fun themeView(view: View, item: Item, theme: Theme) {
        themeView(view, item, theme, null)
    }

    override fun themeView(view: View, item: Item, theme: Theme, position: Int?, onFailure: (Item) -> Unit) {
        theme.apply(view)
    }

    override fun bindView(item: Item, view: View, moduleId: String) {
        bindView(item, view, moduleId, null)
    }

    override fun bindView(item: Item, view: View, moduleId: String, position: Int?, onFailure: (Item) -> Unit) {
        if (item is AdItem) {
            view.findViewById<TextView>(R.id.adItemText)?.apply {
                text = context.getString(R.string.ad_text)
            }

            val lifecycle = (view as? LifecycleProxy)?.getLifecycle()
            view.findViewById<ViewGroup>(R.id.adItemAdWrapper)?.apply {
                removeAllViews()
                val adView = adViewProvider.provideView(this, lifecycle, item, position, object : OnAdFailedListener {
                    override fun onAdFailed() {
                        onFailure.invoke(item)
                    }
                })
                if (adView != null) {
                    addView(adView)
                }
                else {
                    onFailure.invoke(item)
                }
            }
        }
    }

    companion object {
        val TYPE_IDENTIFIER = AdItem::class.java
    }
}