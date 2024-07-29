package se.infomaker.frtutilities

import com.google.gson.annotations.SerializedName
import se.infomaker.frtutilities.mainmenutoolbarsettings.ToolbarConfig
import se.infomaker.frtutilities.mainmenutoolbarsettings.TopAppBarConfig
import java.util.ArrayList

data class MainMenuItem(
    val id: String,
    val moduleName: String,
    @SerializedName("menuLocation") private val _menuLocation: String? = null,
    @SerializedName("defaultSelected") private val _defaultSelected: Boolean? = null,
    @SerializedName("prefetchEnabled") private val _prefetchEnabled: Boolean? = null,
    @SerializedName("toolbarConfig") private val _toolbarConfig: ToolbarConfig? = null,
    @SerializedName("topAppBar") private val _topAppBar: TopAppBarConfig? = null,
    val title: String? = null,
    val requiresPermission: String? = null,
    val subMenu: ArrayList<MainMenuItem>? = null,
    val promotion: String? = null,
    val color: String? = null
) {

    val toolbarTitle: String?
        get() = _topAppBar?.title?.text ?: title

    val menuLocation: String
        get() = _menuLocation ?: DEFAULT_MENU_LOCATION

    val isDefaultSelected: Boolean
        get() = _defaultSelected ?: false

    val prefetchEnabled: Boolean
        get() = _prefetchEnabled ?: false

    val toolbarConfig: ToolbarConfig
        get() = _topAppBar?.asToolbarConfig() ?: _toolbarConfig ?: DEFAULT_TOOLBAR_CONFIG

    companion object {
        const val MENU_LOCATION_DRAWER = "drawer"
        const val MENU_LOCATION_BOTTOM = "bottom"
        const val DEFAULT_MENU_LOCATION = MENU_LOCATION_BOTTOM
        val DEFAULT_TOOLBAR_CONFIG = ToolbarConfig()
    }
}