package se.infomaker.frtutilities

import com.google.android.material.appbar.CollapsingToolbarLayout

/**
 * A component that, in addition to the responsibilities of [AppBarOwner],
 * manages other navigation items.
 */
interface NavigationChromeOwner : AppBarOwner {

    /**
     * The [CollapsingToolbarLayout] controlled by this component.
     */
    val collapsingToolbarLayout: CollapsingToolbarLayout?

    /**
     * Causes all currently hidden navigation chrome to be shown.
     */
    fun expandNavigationChrome()
}