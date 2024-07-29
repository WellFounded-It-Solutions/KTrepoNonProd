package se.infomaker.iap.articleview.view

import androidx.lifecycle.Lifecycle

/**
 * Mark a component as a proxy for a {@link androidx.lifecycle.Lifecycle}.
 * Holding on to it for the throughout its own lifecycle to be accessed by
 * other components.
 */
interface LifecycleProxy {

    /**
     * Get the {@link androidx.lifecycle.Lifecycle}, if any is present.
     */
    fun getLifecycle(): Lifecycle?

    /**
     * Set the {@link androidx.lifecycle.Lifecycle} to proxy for later.
     */
    fun setLifecycle(lifecycle: Lifecycle?)
}