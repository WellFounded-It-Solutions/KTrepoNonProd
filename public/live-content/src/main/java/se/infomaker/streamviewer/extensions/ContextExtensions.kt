package se.infomaker.streamviewer.extensions

import android.content.Context
import se.infomaker.frtutilities.ConfigManager


internal fun Context.getModuleId(moduleName: String): String? {
    return ConfigManager.getInstance(this).mainMenuConfig?.mainMenuItems?.firstOrNull { it.moduleName == moduleName }?.id
}