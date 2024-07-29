package se.infomaker.livecontentui.view.appbar

import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.navigaglobal.mobile.ktx.findDescendantsOfType
import com.navigaglobal.mobile.livecontent.databinding.TranslucentAppBarBinding
import se.infomaker.iap.provisioning.ui.dp2px
import se.infomaker.livecontentui.livecontentdetailview.pageadapters.UpdatableContentFragment
import se.infomaker.livecontentui.offline.TransparentOfflineBannerLayout
import se.infomaker.livecontentui.section.detail.SectionDetailPagerAdapter
import kotlin.math.max
import kotlin.math.min

class TranslucentAppBarCoordinator(private val root: ViewGroup, private val binding: TranslucentAppBarBinding): LifecycleObserver, View.OnAttachStateChangeListener {

    private var viewPager: ViewPager? = null
    private var pageChangeListener: ViewPagerPageChangeListener? = null
    private var recyclerView: RecyclerView? = null
    private var currentGradientHeight = MAX_GRADIENT_HEIGHT
    private var currentAlpha = 0
    private var scrolled = 0
        set(value) {
            field = max(value, 0)
        }

    private val onScroll = object : RecyclerView.OnScrollListener(), Reusable {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy == 0) {
                scrolled = recyclerView.computeVerticalScrollOffset()
            }
            else {
                scrolled += dy
            }
            if (!recyclerView.canScrollVertically(-1)) {
                update(0)
            }
            else {
                updateBackground(scrolled.asBackgroundAlpha())
                updateGradientHeight(scrolled.asGradientHeight())
            }
        }

        override fun reuse() {
            scrolled = 0
            update(animate = true)
        }
    }

    init {
        val gradientStartColor = ColorUtils.setAlphaComponent(GRADIENT_COLOR, GRADIENT_DEFAULT_START_ALPHA)
        binding.toolbar.background = ColorDrawable(gradientStartColor)
        binding.gradientEnd.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(ColorUtils.setAlphaComponent(GRADIENT_COLOR, GRADIENT_DEFAULT_START_ALPHA), Color.TRANSPARENT))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        binding.root.addOnAttachStateChangeListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        binding.root.removeOnAttachStateChangeListener(this)
    }

    override fun onViewAttachedToWindow(v: View) {
        root.findDescendantsOfType(ViewPager::class.java)?.firstOrNull()?.let {
            pageChangeListener = ViewPagerPageChangeListener(it, onScroll)
        } ?: run {
            root.findDescendantsOfType(RecyclerView::class.java)?.firstOrNull()?.let {
                it.addOnScrollListener(onScroll)
                recyclerView = it
            }
        }
    }

    private fun update(progress: Int = scrolled, animate: Boolean = false) {
        updateBackground(progress, animate)
        updateGradientHeight(progress.asGradientHeight(), animate)
    }

    private fun updateBackground(alpha: Int, animate: Boolean = false) {

        if (alpha == currentAlpha) {
            return
        }

        if (animate) {
            val animator = ValueAnimator.ofInt(currentAlpha, alpha)
            animator.addUpdateListener {
                val color = ColorUtils.setAlphaComponent(GRADIENT_COLOR, (it.animatedValue as Int).asGradientStartAlpha())
                (binding.toolbar.background as? ColorDrawable)?.color = color
            }
            animator.duration = 300
            animator.start()
        }
        else {
            val color = ColorUtils.setAlphaComponent(GRADIENT_COLOR, alpha.asGradientStartAlpha())
            (binding.toolbar.background as? ColorDrawable)?.color = color
        }

        (binding.offlineBanner as? TransparentOfflineBannerLayout)?.updateBackground(alpha, animate)

        currentAlpha = alpha
    }

    private fun updateGradientHeight(gradientHeight: Int, animate: Boolean = false) {
        if (gradientHeight == currentGradientHeight) {
            return
        }

        val layoutParams = binding.gradientEnd.layoutParams
        layoutParams?.height = gradientHeight
        binding.gradientEnd.layoutParams = layoutParams

        currentGradientHeight = gradientHeight
    }

    override fun onViewDetachedFromWindow(v: View) {
        recyclerView?.removeOnScrollListener(onScroll)
        pageChangeListener?.let { viewPager?.removeOnPageChangeListener(it) }
    }

    @Deprecated(message = "Rely on Lifecycle callbacks through LifecycleObserver.")
    fun destroy() {
        onDestroy()
    }

    private fun Int.asGradientStartAlpha(): Int {
        return min(this + GRADIENT_DEFAULT_START_ALPHA, MAX_ALPHA)
    }

    private fun Int.asBackgroundAlpha(): Int {
        return (max(min(toFloat() / TARGET_SCROLL_DISTANCE, 1f), 0f) * MAX_ALPHA).toInt()
    }

    private fun Int.asGradientHeight(): Int {
        return (max(1f - min(toFloat() / TARGET_SCROLL_DISTANCE, 1f), 0f) * MAX_GRADIENT_HEIGHT).toInt()
    }

    companion object {
        private val MAX_ELEVATION = 4.dp2px()
        private val MAX_GRADIENT_HEIGHT = 40.dp2px()
        private val TARGET_SCROLL_DISTANCE = Resources.getSystem().displayMetrics.heightPixels / 2
        private const val GRADIENT_COLOR = Color.BLACK
        private const val MAX_ALPHA = 255
        private const val GRADIENT_DEFAULT_START_ALPHA = 102
    }
}

private interface Reusable {
    fun reuse()
}

private class ViewPagerPageChangeListener(
        private val viewPager: ViewPager,
        private val scrollListener: RecyclerView.OnScrollListener
): ViewPager.SimpleOnPageChangeListener(), ViewPager.OnAdapterChangeListener,
        View.OnAttachStateChangeListener, SectionDetailPagerAdapter.OnFragmentCreatedListener,
        LifecycleObserver {

    private var recyclerView: RecyclerView? = null
    private var selectedPage = max(viewPager.currentItem, 0)
    private var lateFragment: Fragment? = null

    init {
        viewPager.addOnPageChangeListener(this)
        viewPager.addOnAttachStateChangeListener(this)
        (viewPager.adapter as? SectionDetailPagerAdapter)?.setOnFragmentCreatedListener(selectedPage, this)
                ?: run {
                    viewPager.addOnAdapterChangeListener(this)
                }
    }

    override fun onAdapterChanged(viewPager: ViewPager, oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?) {
        (newAdapter as? SectionDetailPagerAdapter)?.setOnFragmentCreatedListener(selectedPage, this)
    }

    override fun onPageSelected(position: Int) {
        setupScrollListener(position)
        selectedPage = position
    }

    private fun setupScrollListener(position: Int) {
        (viewPager.adapter as? SectionDetailPagerAdapter)?.let { adapter ->
            adapter.getFragment(position)?.let {
                setupScrollListener(it)
            } ?: run {
                adapter.setOnFragmentCreatedListener(position, this)
            }
        }
    }

    private fun setupScrollListener(fragment: Fragment?) {
        (fragment as? UpdatableContentFragment)?.recyclerView?.let {
            this.recyclerView?.removeOnScrollListener(scrollListener)
            it.addOnScrollListener(scrollListener)
            (scrollListener as? Reusable)?.reuse()
            this.recyclerView = it
        }
    }

    override fun onFragmentCreated(fragment: Fragment) {
        lateFragment = fragment
        fragment.lifecycle.addObserver(this)
        (viewPager.adapter as? SectionDetailPagerAdapter)?.clearOnFragmentCreatedListener()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onFragmentResumed() {
        lateFragment?.lifecycle?.removeObserver(this)
        setupScrollListener(lateFragment)
    }

    override fun onViewAttachedToWindow(v: View) {
        // NOP
    }

    override fun onViewDetachedFromWindow(v: View) {
        recyclerView?.removeOnScrollListener(scrollListener)
        (viewPager.adapter as? SectionDetailPagerAdapter)?.clearOnFragmentCreatedListener()
        viewPager.removeOnPageChangeListener(this)
        viewPager.removeOnAdapterChangeListener(this)
        viewPager.removeOnAttachStateChangeListener(this)
    }
}