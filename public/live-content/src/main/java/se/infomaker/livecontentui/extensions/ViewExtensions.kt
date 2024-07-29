@file:JvmName("ViewUtils")

package se.infomaker.livecontentui.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.updateLayoutParams

internal fun View.setPadding(horizontal: Int, vertical: Int) {
    setPadding(horizontal, vertical, horizontal, vertical)
}

internal fun View.findFragmentManager() = context.findFragmentManager()