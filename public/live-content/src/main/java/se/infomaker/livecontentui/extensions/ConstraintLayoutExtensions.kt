package se.infomaker.livecontentui.extensions

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.core.view.children

internal fun ConstraintSet.cloneChild(parent: ConstraintLayout, child: View) {
    clone(parent)
    parent.children.map { it.id }.filterNot { it == child.id }.forEach { clear(it) }
}

internal fun Group.setOnAllClickListener(onClickListener: View.OnClickListener) {
    referencedIds?.forEach {
        rootView.findViewById<View>(it).setOnClickListener(onClickListener)
    }
}