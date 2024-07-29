package se.infomaker.frtutilities.ktx

import se.infomaker.frtutilities.ConfigManager

inline fun <reified T> ConfigManager.globalConfig(): T =
    getConfig("global", T::class.java)