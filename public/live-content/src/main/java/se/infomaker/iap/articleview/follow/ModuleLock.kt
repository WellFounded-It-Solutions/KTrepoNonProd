package se.infomaker.iap.articleview.follow

import android.content.Context
import io.reactivex.Observable
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.MainMenuItem
import se.infomaker.iap.provisioning.ProvisioningManager
import se.infomaker.iap.provisioning.ProvisioningManagerProvider

class ModuleLock private constructor(context: Context, val menuItem: MainMenuItem) {
    private val provisioningManager : ProvisioningManager = ProvisioningManagerProvider.provide(context)

    companion object {
        private var followLock: ModuleLock? = null

        @Synchronized
        fun followLock(context: Context): ModuleLock? {
            if (followLock == null) {
                ConfigManager.getInstance(context.applicationContext).mainMenuConfig
                        .mainMenuItems.firstOrNull { it.moduleName == "Follow" || it.moduleName == "NearMe" }?.let { menuItem ->
                    followLock = ModuleLock(context.applicationContext, menuItem)
                }
            }
            return followLock
        }
    }

    fun isAlwaysOpen(): Boolean {
        return menuItem.requiresPermission == null
    }

    fun isOpen(): Observable<Boolean> {
        menuItem.requiresPermission?.let { permission ->
            return provisioningManager.canDisplayContentWithPermission(permission)
        }
        return Observable.just(true)
    }
}