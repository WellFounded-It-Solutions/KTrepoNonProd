package se.infomaker.livecontentui

import org.json.JSONObject
import se.infomaker.livecontentui.config.BindingOverride

/**
 * A [ViewBehaviour] encapsulates behaviour related to views provided by a
 * [androidx.recyclerview.widget.RecyclerView.Adapter].
 *
 * A key is provided of a specific type and used to resolve what kind of view is to be
 * inflated, what overrides to apply and how it is styled, using theme overlays.
 *
 * For each key a view type is associated and it is up to the specific implementation to
 * keep track of said view type and connect it to the proper layout and overrides.
 */
interface ViewBehaviour<T> {

    /**
     * Returns a view type represented by an [Int] based on the [key] passed in.
     *
     * The returned view type can be used by the caller to query a layout resource
     * and/or a list of [BindingOverride]s to be applied to an inflated view.
     */
    fun viewTypeForKey(key: T): Int

    /**
     * Returns a layout resource that can be used by a [android.view.LayoutInflater] based
     * on the passed in [viewType] retrieved from [viewTypeForKey].
     */
    fun layoutResourceForViewType(viewType: Int): Int

    /**
     * Returns an optional list of [BindingOverride]s to be applied to a view, based on the
     * passed in [viewType] retrieved from [viewTypeForKey].
     */
    fun bindingOverridesForViewType(viewType: Int): List<BindingOverride>?

    /**
     * Returns a list of 0..n theme files to be applied to a view when binding content,
     * based on the key.
     */
    fun themesForKey(key: T): List<String>?

    /**
     * TODO
     */
    fun presentationContextForKey(key: T): JSONObject?
}