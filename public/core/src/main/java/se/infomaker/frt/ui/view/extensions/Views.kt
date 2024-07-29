package se.infomaker.frt.ui.view.extensions

import android.view.View

fun View.safeRequestLayout() {
    if (isInLayout) {
        post { requestLayout() }
    }
    else {
        requestLayout()
    }
}