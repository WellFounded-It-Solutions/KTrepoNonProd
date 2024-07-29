package se.infomaker.iap.articleview.item.image

import android.os.Handler
import androidx.viewpager.widget.ViewPager
import android.view.View
import android.view.ViewGroup
import com.navigaglobal.mobile.livecontent.R


class ParallaxImagePageTransformer : ViewPager.PageTransformer {
    internal var parallaxMap = mutableMapOf<ViewGroup, List<View>>()

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width

        if (view is ViewGroup && position >= -1 && position <= 1) { // [-1,1]
            getImageViewList(view)?.forEach { imageView ->
                imageView.translationX = -position * (pageWidth / 2) //Half the normal speed
            }
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.alpha = 1f
        }
    }

    /**
     * Finds all child views with tag using R.id.parallax set to true
     */
    private fun ViewGroup.findAllChildrenWithTag(out: MutableList<View> = mutableListOf()): List<View> {
        (0 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    if (it.getTag(R.id.parallax) == true) {
                        out.add(it)
                    } else if (it is ViewGroup) {
                        out.addAll(it.findAllChildrenWithTag(out))
                    }
                }
        return out
    }

    private fun getImageViewList(vg: ViewGroup): List<View>? {
        if (parallaxMap.containsKey(vg)) {
            return parallaxMap[vg]
        }

        val list = vg.findAllChildrenWithTag()
        parallaxMap.put(vg, list)
        Handler().postDelayed({ parallaxMap.remove(vg) }, 5000)

        return list
    }
}