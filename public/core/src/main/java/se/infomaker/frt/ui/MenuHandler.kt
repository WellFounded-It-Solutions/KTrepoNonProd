package se.infomaker.frt.ui

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.navigaglobal.mobile.R
import se.infomaker.frt.module.ModuleIntegrationProvider
import se.infomaker.frt.moduleinterface.ModuleIconProvider
import se.infomaker.frtutilities.MainMenuItem
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.provisioning.ui.dp2px
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.ktx.brandColor
import se.infomaker.iap.theme.ktx.chromeColor

class MenuHandler(
        activity: Activity,
        private val bottomNavigation: BottomNavigationView,
        private val navigationView: NavigationView,
        var drawerLayout: DrawerLayout,
        toolbar: Toolbar,
        var menuItems: List<MainMenuItem>
) {

    private val menuItemListeners = mutableListOf<SelectMenuItemListener>()
    private val drawerMenuItems = mutableMapOf<MenuItem, MainMenuItem>()
    private val bottomNavigationMenuItems = mutableMapOf<MenuItem, MainMenuItem>()
    private val applicationContext = activity.applicationContext
    private var currentMenuItem: MainMenuItem? = null

    private val themeManager = ThemeManager.getInstance(activity)
    private val theme
        get() = themeManager.appTheme

    val drawerToggle: ActionBarDrawerToggle? by lazy {
        if (drawerMenuItems.isEmpty() || (bottomNavigationMenuItems.size + drawerMenuItems.size) < 2) null
        else ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
    }

    private val shouldShowBottomNavigation
        get() = bottomNavigationMenuItems.size > 1

    init {
        load()
    }

    fun reload() {
        val current = currentMenuItem ?: menuItems[0]
        bottomNavigation.menu.clear()
        load()
        setSelectedItem(current)
    }

    private fun load() {
        val tintColors = intArrayOf(theme.getColor("bottomNavigationItemSelected", theme.brandColor).get(), theme.getColor("bottomNavigationItemDeselected", ThemeColor.GRAY).get())
        bottomNavigation.itemIconTintList = ColorStateList(BOTTOM_NAV_ITEM_STATES, tintColors)
        bottomNavigation.itemTextColor = ColorStateList(BOTTOM_NAV_ITEM_STATES, tintColors)
        bottomNavigation.setBackgroundColor(theme.getColor("bottomNavigationColor", theme.chromeColor).get())
        if (menuItems.size == 1) {
            currentMenuItem = menuItems[0]
        } else {
            menuItems.filter { it.menuLocation != "none" }.forEachIndexed { index, menuItem ->
                when (menuItem.menuLocation) {
                    MainMenuItem.MENU_LOCATION_DRAWER -> {
                        addMenuItemToDrawer(menuItem, index)
                    }
                    else -> addMenuItemToBottomMenu(menuItem, index)
                }
            }

            bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
                bottomNavigationItemSelected(menuItem)
                true
            }

            navigationView.setNavigationItemSelectedListener { menuItem ->
                drawerMenuItemSelected(menuItem)
                true
            }

            if (bottomNavigationMenuItems.isNotEmpty()) {
                drawerMenuItems.forEach {
                    it.key.isChecked = false
                }
                bottomNavigationMenuItems
                        .forEach { if (it.value.isDefaultSelected) currentMenuItem = it.value }
                if (currentMenuItem == null) {
                    currentMenuItem = bottomNavigationMenuItems[bottomNavigationMenuItems.keys.first()]
                }
                bottomNavigation.children.map { it.findViewById<ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view) }
                    .forEach { iconView ->
                        iconView?.updateLayoutParams {
                            height = 24.dp2px()
                            width = 48.dp2px()
                        }
                    }
            }
        }
        if (drawerToggle == null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
        if (shouldShowBottomNavigation) {
            bottomNavigation.visibility = View.VISIBLE
        }
        else {
            bottomNavigation.visibility = View.GONE
        }
    }

    fun selectModule(moduleId: String) {
        bottomNavigationMenuItems.forEach {
            if (it.value.id == moduleId) {
                bottomNavigation.selectedItemId = it.key.itemId
            }
        }
        drawerMenuItems.forEach {
            if (it.value.id == moduleId) {
                drawerMenuItemSelected(it.key)
            }
        }
    }

    fun getMenuItemFromModuleId(moduleId: String) : MainMenuItem? {
        drawerMenuItems.forEach {
            if (it.value.id == moduleId) {
                return it.value
            }
        }
        bottomNavigationMenuItems.forEach {
            if (it.value.id == moduleId) {
                return it.value
            }
        }
        return null
    }

    fun setSelectedModule(moduleId: String) {
        drawerMenuItems.forEach {
            if (it.value.id == moduleId) {
                setSelectedItem(it.value)
                return
            }
        }
        bottomNavigationMenuItems.forEach {
            if (it.value.id == moduleId) {
                setSelectedItem(it.value)
                return
            }
        }
    }

    fun getCurrentMenuItem(): MainMenuItem? = currentMenuItem

    private fun addMenuItemToBottomMenu(mainMenuItem: MainMenuItem, index: Int) {
        val drawableRes = mainMenuItem.getIconIdentifier(applicationContext)
        val drawable = if (drawableRes == 0) {
            ColorDrawable(Color.TRANSPARENT)
        }
        else {
            AppCompatResources.getDrawable(bottomNavigation.context, drawableRes)
        }
        val color = if (mainMenuItem.color.isNullOrEmpty()) {
            theme.getColor("tabBackground", ThemeColor.WHITE).get()
        }
        else {
            theme.getColor(mainMenuItem.color, null)?.get() ?: Color.parseColor(mainMenuItem.color)
        }
        val menuItem = bottomNavigation.menu.add(0, index, index, mainMenuItem.title)
        drawable?.let {
            menuItem.icon = DrawableCompat.wrap(it).mutate().also { wrappedDrawable -> DrawableCompat.setTint(wrappedDrawable, color) }
        }
        bottomNavigationMenuItems[menuItem] = mainMenuItem
    }

    private fun addMenuItemToDrawer(mainMenuItem: MainMenuItem, index: Int) {
        val menuItem = navigationView.menu.add(0, index, index, mainMenuItem.title)
        menuItem.setIcon(mainMenuItem.getIconIdentifier(applicationContext))
        drawerMenuItems[menuItem] = mainMenuItem

        if (currentMenuItem == null) {
            currentMenuItem = mainMenuItem
            menuItem.isChecked = true
        }
    }

    fun setSelectedItem(menuItem: MainMenuItem) {
        bottomNavigationMenuItems.forEach {
            if (it.value == menuItem) {
                bottomNavigation.selectedItemId = it.key.itemId
            }
        }
    }

    fun addSelectMenuItemListener(menuItemListener: SelectMenuItemListener) {
        menuItemListeners.add(menuItemListener)
    }

    private fun bottomNavigationItemSelected(menuItem: MenuItem) {
        bottomNavigationMenuItems[menuItem]?.let {
            notifyMenuItemSelected(it, true)
        }
    }

    private fun drawerMenuItemSelected(menuItem: MenuItem) {
        drawerMenuItems[menuItem]?.let {
            setCurrentlySelectedDrawerItem(menuItem)
            notifyMenuItemSelected(it, bottomNavigationMenuItems.isEmpty())
        }
    }

    /**
     * Sets the currently selected drawer item, but only if we use the drawer as top level navigation
     */
    private fun setCurrentlySelectedDrawerItem(menuItem: MenuItem) {
        if (bottomNavigationMenuItems.isEmpty()) {
            drawerMenuItems.forEach {
                it.key.isChecked = false
            }
            menuItem.isChecked = true
        }
    }

    private fun notifyMenuItemSelected(menuItem: MainMenuItem, topLevel: Boolean = true) {
        currentMenuItem = menuItem
        menuItemListeners.forEach { it.bottomMenuItemSelected(menuItem, topLevel) }
    }

    interface SelectMenuItemListener {
        fun bottomMenuItemSelected(menuItem: MainMenuItem, topLevel: Boolean)
    }

    companion object {
        private val BOTTOM_NAV_ITEM_STATES = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
    }
}

private fun MainMenuItem.getIconIdentifier(context: Context): Int {
    val resourceManager = ResourceManager(context, id)
    var identifier = resourceManager.getDrawableIdentifier("module_icon")
    if (identifier == 0) {
        (ModuleIntegrationProvider.getInstance(context).integrationList.firstOrNull { it.id == id } as? ModuleIconProvider)?.let {
            identifier = it.moduleIcon
        }
    }
    if (identifier == 0) {
        identifier = resourceManager.getDrawableIdentifier(id)
    }
    return identifier
}
