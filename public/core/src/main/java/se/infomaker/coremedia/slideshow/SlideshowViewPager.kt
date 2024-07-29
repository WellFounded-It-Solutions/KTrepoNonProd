package se.infomaker.coremedia.slideshow

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import timber.log.Timber

class SlideshowViewPager(context: Context, attrs: AttributeSet?) : androidx.viewpager.widget.ViewPager(context, attrs) {
    private var pagerAdapter: SlideshowPagerAdapter? = null
        private set

    override fun setAdapter(adapter: androidx.viewpager.widget.PagerAdapter?) {
        super.setAdapter(adapter)
        pagerAdapter = adapter as SlideshowPagerAdapter?
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        //Don't allow swipe when zoomed in on image
        if (pagerAdapter?.getFragment(currentItem)?.isZoomedOut() == true) {
            return try {
                super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "Something went wrong when checking if we should intercept touch")
                false
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        //Don't allow swipe when zoomed in on image
        if (pagerAdapter?.getFragment(currentItem)?.isZoomedOut() == true) {
            return try {
                super.onTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "Something went wrong when touching")
                false
            }
        }
        return false
    }

    fun getCurrentFragment(): ImageFragment? {
        return pagerAdapter?.getFragment(currentItem)
    }
}