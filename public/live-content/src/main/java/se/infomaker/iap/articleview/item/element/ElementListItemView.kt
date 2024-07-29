package se.infomaker.iap.articleview.item.element

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import se.infomaker.frtutilities.ResourceManager
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.iap.articleview.ktx.hide
import se.infomaker.iap.articleview.ktx.show
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableTextView

class ElementListItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    companion object {
        const val BULLET_PREFERRED_HEIGHT = 8f
    }

    val unorderedIndicator: ThemeableImageView
    val orderedIndicator: ThemeableTextView
    val element: ThemeableTextView

    init {
        unorderedIndicator = ThemeableImageView(context).apply {
            themeKey = "unorderedElementListIcon"
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            fallbackDrawable = R.drawable.element_list_bullet
            adjustViewBounds = true
        }

        addView(unorderedIndicator)

        orderedIndicator = ThemeableTextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.END
        }

        addView(orderedIndicator)

        element = ThemeableTextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        addView(element)
    }

    fun handleIndicator(item: ElementListItem, resourceManager: ResourceManager, position: Int) {
        if (item.listType == ElementListItem.ListType.ORDERED) {
            unorderedIndicator.hide()
            orderedIndicator.show()

            // TODO ResourceManager with format string.
            orderedIndicator.text = "${position + 1}."
        }
        else {
            unorderedIndicator.show()
            orderedIndicator.hide()
        }
    }
}