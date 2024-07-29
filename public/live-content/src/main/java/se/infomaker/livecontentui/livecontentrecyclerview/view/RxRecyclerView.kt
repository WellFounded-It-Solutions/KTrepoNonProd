package se.infomaker.livecontentui.livecontentrecyclerview.view

import android.content.Context
import android.util.AttributeSet
import io.reactivex.Observable
import se.infomaker.iap.theme.view.ThemeableRecyclerView
import com.navigaglobal.mobile.livecontent.R

open class RxRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ThemeableRecyclerView(context, attrs), OnClickObservable {
    private val itemClickIdentifier: String?

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RxRecyclerView)
        itemClickIdentifier = typedArray.getString(R.styleable.RxRecyclerView_itemClickIdentifier)
        typedArray.recycle()
    }

    override fun clicks(): Observable<ViewClick>? {
        itemClickIdentifier?.let {
            (adapter as? OnClickObservable)?.clicks()?.map { itemClick ->
                ViewClick(it, itemClick.contentId)
            }
        }?.let {
            return it
        }
        return null
    }
}