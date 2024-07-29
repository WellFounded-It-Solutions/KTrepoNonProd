package se.infomaker.iap.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.navigaglobal.mobile.R
import com.navigaglobal.mobile.ktx.findAncestorOfType
import com.navigaglobal.mobile.ktx.safeUpdateLayoutParams
import se.infomaker.iap.theme.view.ThemeableConstraintLayout
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class RecycleViewAwareConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ThemeableConstraintLayout(context, attrs), AppBarLayout.OnOffsetChangedListener {

    private val parallaxViews = mutableListOf<View>()
    private val fadeViews = mutableListOf<View>()

    private var recyclerviewParent: RecyclerView? = null
    private var scrollListener: RecyclerView.OnScrollListener? = null
    private val isInitialized = AtomicBoolean(false)

    private var attachedAppBarLayout: AppBarLayout? = null
    private val shouldCounterAppBarLayout: Boolean

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecycleViewAwareConstraintLayout)
        shouldCounterAppBarLayout = typedArray.getBoolean(R.styleable.RecycleViewAwareConstraintLayout_counterAppBarLayout, false)
        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        init()

        if (shouldCounterAppBarLayout) {
            findAncestorOfType(AppBarLayout::class.java)?.let {
                it.addOnOffsetChangedListener(this)
                attachedAppBarLayout = it
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recyclerviewParent?.apply {
            scrollListener?.let {
                removeOnScrollListener(it)
                scrollListener = null
            }
        }

        attachedAppBarLayout?.removeOnOffsetChangedListener(this)
    }

    private fun init() {
        if (!isInitialized.getAndSet(true)) {
            recyclerviewParent = findRecyclerviewParent()
            getViewsToListenToScroll()
        }
        val recyclerView = recyclerviewParent
        if (recyclerView == null || scrollListener != null) {
            return
        }

        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                onScrolled()
            }
        }
        scrollListener?.let {
            recyclerView.addOnScrollListener(it)
        }

        onScrolled()
    }

    private fun getViewsToListenToScroll() {
        (0 until childCount)
                .map { getChildAt(it) }
                .forEach { view ->
                    (view.layoutParams as RecycleViewAwareConstraintLayout.LayoutParams).let { layoutParams ->
                        if (layoutParams.parallax != 1f) {
                            parallaxViews.add(view)
                        }
                        if (layoutParams.fade) {
                            fadeViews.add(view)
                        }
                    }
                }
    }

    private fun onScrolled() {
        percentageHidden = calculatePercentageHidden()
        applyParallax(parallaxViews)
        applyFade(fadeViews)
        Timber.d("PercentHidden = ${calculatePercentageHidden()}")
    }

    private fun applyParallax(views: List<View>) {
        val topOffset = topOffset()
        views.forEach { view ->
            (view.layoutParams as RecycleViewAwareConstraintLayout.LayoutParams).let { layoutParams ->
                view.translationY = if (topOffset < 0) -topOffset.toFloat() * (1f - layoutParams.parallax) else 0f
            }
        }
    }

    var percentageHidden = calculatePercentageHidden()
    private fun applyFade(views: List<View>) {
        views.forEach { view ->
            view.alpha = 1 - percentageHidden
        }
    }

    private fun topOffset(): Int {
        var total = top
        var currentParent = parent
        while (currentParent != recyclerviewParent) {
            if (currentParent is View) {
                total += currentParent.top
            }
            currentParent = currentParent.parent
        }
        return total
    }

    private fun calculatePercentageHidden(): Float = -topOffset().toFloat() / measuredHeight

    private fun findRecyclerviewParent(view: View = this): RecyclerView? {
        return when (val parent = view.parent) {
            is RecyclerView -> parent
            is View -> findRecyclerviewParent(parent)
            else -> null
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): ConstraintLayout.LayoutParams = LayoutParams(context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        if (p is LayoutParams) {
            return LayoutParams(p)
        } else if (p is ViewGroup.MarginLayoutParams) {
            return LayoutParams(p)
        }
        return LayoutParams(p)
    }

    class LayoutParams(context: Context, attrs: AttributeSet) : ConstraintLayout.LayoutParams(context, attrs) {
        var parallax = 1f
        var fade = false

        init {
            context.obtainStyledAttributes(attrs, R.styleable.RecycleViewAwareConstraintLayout).apply {
                (0 until indexCount)
                        .map { getIndex(it) }
                        .forEach {
                            when (it) {
                                R.styleable.RecycleViewAwareConstraintLayout_parallax -> {
                                    parallax = getFloat(it, parallax)
                                }
                                R.styleable.RecycleViewAwareConstraintLayout_fade -> {
                                    fade = getBoolean(it, fade)
                                }
                            }
                        }
            }.recycle()
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        updateHeight(appBarLayout.measuredHeight)
    }

    private fun updateHeight(heightShift: Int) {
        val newHeight = originalHeight - heightShift
        if (newHeight != measuredHeight) {
            safeUpdateLayoutParams { height = newHeight }
        }
    }
}

private val View.originalHeight: Int
    get() {
        (getTag(R.id.original_height_tag) as? Int)?.let {
            return it
        }
        return measuredHeight.also {
            setTag(R.id.original_height_tag, it)
        }
    }