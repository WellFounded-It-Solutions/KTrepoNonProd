package se.infomaker.frtutilities.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.google.android.material.appbar.AppBarLayout
import com.navigaglobal.mobile.R
import com.navigaglobal.mobile.ktx.findAncestorOfType
import se.infomaker.frtutilities.AppBarOwner
import se.infomaker.frtutilities.ktx.findActivity
import timber.log.Timber

class ScrollingOffsetCounterFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs), AppBarLayout.OnOffsetChangedListener {

    private val viewIdsToCounterOffset = mutableSetOf<Int>()
    private val viewsToCounterOffset = mutableSetOf<View>()
    private val appBarLayout by lazy {
        (findActivity() as? AppBarOwner)?.appBarLayout ?: findAncestorOfType(AppBarLayout::class.java).also {
            if (it == null) Timber.w("No AppBarLayout found in view hierarchy with context: ${context::class.java}")
        }
    }

    private var currentOffset = 0

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ScrollingOffsetCounterFrameLayout)
            typedArray.getString(R.styleable.ScrollingOffsetCounterFrameLayout_counterOffsetIds)?.let { ids ->
                resolveIdsToCounterOffset(ids)
            }
            typedArray.recycle()
        }
    }

    private fun resolveIdsToCounterOffset(ids: String) {
        ids.split(",")
                .map { it.trim() }
                .map { context.resources.getIdentifier(it, "id", context.packageName) }
                .filter { it > 0 }
                .forEach { addIdToCounterOffset(it) }
    }

    fun addIdToCounterOffset(identifier: Int) {
        viewIdsToCounterOffset.add(identifier)
    }

    @Deprecated(
            message = "Avoid adding view this way, instead add the ID and the ScrollingOffsetCounterFrameLayout will handle the rest.",
            replaceWith = ReplaceWith("addIdToCounterOffset(id) and View.addView()", imports = arrayOf("se.infomaker.frtutilities.view", "android.view.View"))
    )
    fun addViewAsCounterOffset(view: View) {
        super.addView(view)
        addIdToCounterOffset(view.id)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        appBarLayout?.addOnOffsetChangedListener(this)
        resolveViewsToBeOffset()
    }

    private fun resolveViewsToBeOffset() {
        viewIdsToCounterOffset
                .filterNot { viewsToCounterOffset.map { view -> view.id }.contains(it) }
                .mapNotNull { findViewById(it) }
                .forEach {
                    viewsToCounterOffset.add(it)
                }
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        resolveViewsToBeOffset()
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)
        when (child) {
            is ViewGroup -> {
                child.children.filter { viewIdsToCounterOffset.contains(it.id) }.forEach { viewsToCounterOffset.remove(it) }
            }
            else -> {
                if (viewIdsToCounterOffset.contains(child.id)) viewsToCounterOffset.remove(child)
            }
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        handleOffset(-verticalOffset)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        appBarLayout?.let {
            offsetChildren(-it.totalScrollRange - it.currentOffset)
        }
    }

    private fun offsetChildren(offset: Int) {
        viewsToCounterOffset.forEach { ViewCompat.offsetTopAndBottom(it, offset) }
    }

    private fun handleOffset(offset: Int) {
        val newOffset = offset - currentOffset
        offsetChildren(newOffset)
        currentOffset = offset
    }

    override fun onDetachedFromWindow() {
        viewsToCounterOffset.clear()
        appBarLayout?.removeOnOffsetChangedListener(this)
        super.onDetachedFromWindow()
    }
}

private val AppBarLayout.currentOffset: Int
    get() = ((layoutParams as CoordinatorLayout.LayoutParams).behavior as AppBarLayout.Behavior).topAndBottomOffset