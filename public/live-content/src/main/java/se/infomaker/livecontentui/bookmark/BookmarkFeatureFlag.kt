package se.infomaker.livecontentui.bookmark

import android.content.Context
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.runtimeconfiguration.OnConfigChangeListener

/**
 * Resolve and monitor state of bookmark feature flag
 */
class BookmarkFeatureFlag {
    companion object : OnConfigChangeListener {
        private var isListeningToChanges = false
        private var currentStatus: Boolean? = null

        /**
         * @return true if the bookmarks are enabled
         */
        fun isEnabled(context: Context) : Boolean {
            if (!isListeningToChanges) {
                ConfigManager.getInstance(context).addOnConfigChangeListener(this)
                isListeningToChanges = true
            }
            currentStatus?.let {
                return it
            }
            val hasBookmarks = ConfigManager.getInstance(context).mainMenuConfig.mainMenuItems.any {
                it.moduleName == "Bookmarks"
            }
            currentStatus = hasBookmarks
            return hasBookmarks
        }

        override fun onChange(updated: MutableList<String>?, removed: MutableList<String>?): MutableSet<String> {
            currentStatus = null
            return mutableSetOf()
        }
    }
}