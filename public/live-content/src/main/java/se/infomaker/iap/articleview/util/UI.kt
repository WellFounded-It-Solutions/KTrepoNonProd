package se.infomaker.iap.articleview.util

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import com.navigaglobal.mobile.livecontent.R
import timber.log.Timber
import java.util.ArrayList
import java.util.HashSet

object UI {
    fun px2dp(px: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        val dp = px / (metrics.densityDpi / 160f)
        return Math.round(dp).toFloat()
    }

    fun dp2px(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return Math.round(px).toFloat()
    }

    fun View.getAllChildren(): List<View> {
        val objects = HashSet<View>()
        addAllChildrenUnique(objects)
        return ArrayList(objects)
    }

    private fun View.addAllChildrenUnique(set: MutableSet<View>) {
        if (this !is ViewGroup) {
            set.add(this)
            return
        }
        set.add(this)
        for (i in 0 until this.childCount) {
            val child = this.getChildAt(i)
            set.add(this)
            child.addAllChildrenUnique(set)
        }
    }

    fun View.mapSubViews() :Map<String, View> {
        val namedViews = mutableMapOf<String, View>()
        this.getAllChildren()
                .filter { it.id != -1 }
                .forEach {
                    try {
                        val name = it.resources.getResourceEntryName(it.id)
                        if (name != null) {
                            namedViews.put(name, it)
                        }
                    } catch (e: Resources.NotFoundException) {
                        Timber.w(e, "did not find resource")
                    }
                }
        this.setTag(R.id.viewMap, namedViews)
        return namedViews
    }
}