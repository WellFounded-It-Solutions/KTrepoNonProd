package se.infomaker.iap.articleview.ktx

import android.view.View

internal fun View.isVisible(): Boolean = this.visibility == View.VISIBLE

internal fun View.hide() {
    this.visibility = View.GONE
}

internal fun View.show() {
    this.visibility = View.VISIBLE
}