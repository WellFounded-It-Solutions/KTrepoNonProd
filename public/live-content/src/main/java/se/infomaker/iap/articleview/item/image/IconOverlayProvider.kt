package se.infomaker.iap.articleview.item.image

import android.widget.FrameLayout

interface IconOverlayProvider {

    /**
     * Provides overlay to be drawn on top of image
     */
    fun getIconOverlay(): FrameLayout

}