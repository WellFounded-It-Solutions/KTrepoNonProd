package com.navigaglobal.mobile.ktx

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.updateLayoutParams

fun <R> View.findAncestorOfType(klass: Class<R>): R? {
    (parent as? ViewGroup)?.let { parent ->
        parent.children.filterIsInstance(klass)
            .firstOrNull()
            ?.let { return it }
        return parent.findAncestorOfType(klass)
    }
    return null
}

fun <R> View.findDescendantsOfType(klass: Class<R>): List<R>? {
    if (klass.isInstance(this)) {
        @Suppress("UNCHECKED_CAST")
        return listOf(this as R)
    }
    val out = mutableListOf<R>()
    if (this is ViewGroup) {
        children.forEach { child ->
            child.findDescendantsOfType(klass)?.let {
                out.addAll(it)
            }
        }
    }
    return out
}

inline fun View.safeUpdateLayoutParams(crossinline block: ViewGroup.LayoutParams.() -> Unit) {
    safeUpdateLayoutParams<ViewGroup.LayoutParams>(block)
}

@JvmName("safeUpdateLayoutParamsTyped")
inline fun <reified T : ViewGroup.LayoutParams> View.safeUpdateLayoutParams(crossinline block: T.() -> Unit) {
    if (isInLayout) {
        post { updateLayoutParams(block) }
    }
    else {
        updateLayoutParams(block)
    }
}