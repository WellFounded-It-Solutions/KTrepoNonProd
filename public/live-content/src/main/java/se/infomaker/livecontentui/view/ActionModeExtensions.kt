package se.infomaker.livecontentui.view

import android.graphics.PorterDuff
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.appcompat.view.StandaloneActionMode
import androidx.appcompat.widget.ActionBarContextView
import androidx.appcompat.widget.AppCompatImageView


fun ActionMode.setBackgroundColor(color: Int){
    (this as? StandaloneActionMode).let {
        val contextView = it?.javaClass?.getDeclaredField("mContextView")
        contextView?.isAccessible = true

        (contextView?.get(it) as? View)?.setBackgroundColor(color)
    }
}


fun ActionMode.setCloseButtonColor(color: Int){
    (this as? StandaloneActionMode).let {
        val contextView = it?.javaClass?.getDeclaredField("mContextView")
        contextView?.isAccessible = true
        val parent = contextView?.get(it) as? ActionBarContextView
        parent?.initForMode(this)
        val closeButton = parent?.javaClass?.getDeclaredField("mClose")
        closeButton?.isAccessible = true
        val closeView = closeButton?.get(parent) as? AppCompatImageView
        closeView?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

fun ActionMode.setCloseButtonImageResource(resId: Int){
    (this as? StandaloneActionMode).let {
        val contextView = it?.javaClass?.getDeclaredField("mContextView")
        contextView?.isAccessible = true
        val parent = contextView?.get(it) as? ActionBarContextView
        parent?.initForMode(this)
        val closeButton = parent?.javaClass?.getDeclaredField("mClose")
        closeButton?.isAccessible = true
        val closeView = closeButton?.get(parent) as? AppCompatImageView
        closeView?.setImageResource(resId)
    }
}