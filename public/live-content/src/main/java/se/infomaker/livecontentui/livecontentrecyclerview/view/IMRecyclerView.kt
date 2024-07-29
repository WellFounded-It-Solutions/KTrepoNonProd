package se.infomaker.livecontentui.livecontentrecyclerview.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import com.navigaglobal.mobile.livecontent.R


class IMRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RxRecyclerView(context, attrs) {

    val loopScroll: Boolean
    val bindKeyPath: String?
    val itemsBindKeyPath: String?
    val itemLayout: Int
    val snapTo: String?
    val childWidth: Float?
    val stretchToFill: Boolean

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.IMRecyclerView)
        loopScroll = typedArray.getBoolean(R.styleable.IMRecyclerView_loopScroll, false)
        bindKeyPath = typedArray.getString(R.styleable.IMRecyclerView_bindKeyPath)
        itemsBindKeyPath = typedArray.getString(R.styleable.IMRecyclerView_itemsBindKeyPath)
        itemLayout = typedArray.getResourceId(R.styleable.IMRecyclerView_itemLayout, R.layout.section_package_list_item_default)
        snapTo = typedArray.getString(R.styleable.IMRecyclerView_snapTo)
        val configuredChildWith = typedArray.getDimension(R.styleable.IMRecyclerView_childWidth, -1f)
        childWidth = if (configuredChildWith > 0) configuredChildWith else null
        stretchToFill = typedArray.getBoolean(R.styleable.IMRecyclerView_stretchToFill, false)
        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        when(snapTo) {
            "start" -> Gravity.START
            "end" -> Gravity.END
            "center" -> Gravity.CENTER
            else -> null
        }?.let { gravity ->
            onFlingListener = null
            GravityPagerSnapHelper(gravity).attachToRecyclerView(this)
        }
    }
}