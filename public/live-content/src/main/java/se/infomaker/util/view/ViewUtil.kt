package se.infomaker.util.view

import android.view.View
import android.view.ViewGroup

/**
 * Returns a views children.
 * If view has no children, an empty list is returned.
 */
fun View.getChildren(): List<View> {
    if (this is ViewGroup) {
        return (0..childCount - 1).map { getChildAt(it) }
    }
    return emptyList()
}