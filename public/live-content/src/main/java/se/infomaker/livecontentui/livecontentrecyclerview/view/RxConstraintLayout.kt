package se.infomaker.livecontentui.livecontentrecyclerview.view

import android.content.Context
import android.util.AttributeSet
import com.jakewharton.rxbinding3.view.clicks as rxBindingClicks
import io.reactivex.Observable
import se.infomaker.iap.theme.view.ThemeableConstraintLayout
import com.navigaglobal.mobile.livecontent.R

class RxConstraintLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ThemeableConstraintLayout(context, attrs), OnClickObservable {

    private val clickIdentifier: String?

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RxConstraintLayout)
        clickIdentifier = typedArray.getString(R.styleable.RxConstraintLayout_clickIdentifier)
        typedArray.recycle()
    }

    override fun clicks(): Observable<ViewClick>? {
        clickIdentifier?.let {
            rxBindingClicks().map { _ ->
                ViewClick(it)
            }
        }?.let {
            return it
        }
        return null
    }
}