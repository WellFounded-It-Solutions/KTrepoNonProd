package se.infomaker.iap.articleview.view

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import timber.log.Timber
import java.util.HashMap

/**
 * Extract all views with id from view and put in map with id string as key
 * @param view to extract views from
 * @return map of views in view (including the view)
 */
fun View.extractViews(): Map<String, View> {
    val viewMap = HashMap<String, View>()
    extractViews(viewMap)
    return viewMap
}

private fun View.extractViews(target: MutableMap<String, View>) {
    try {
        if (id != View.NO_ID) {
            val name = resources.getResourceEntryName(id)
            if (name != null) {
                target[name] = this
            }
        }
    } catch (e: Resources.NotFoundException) {
        Timber.e(e, "Could not find view")
    }

    if (this is ViewGroup) {
        for (i in 0 until this.childCount) {
            getChildAt(i).extractViews(target)
        }
    }
}