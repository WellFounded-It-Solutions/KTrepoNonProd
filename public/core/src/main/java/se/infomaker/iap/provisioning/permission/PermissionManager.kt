package se.infomaker.iap.provisioning.permission

import se.infomaker.frtutilities.ResourceManager
import timber.log.Timber

class PermissionManager(resourceManager: ResourceManager) {
    companion object {
        private const val PERMISSIONS_RESOURCE = "configuration/permissions.json";
    }

    private var permissionConfig: PermissionConfig = resourceManager.getAsset(PERMISSIONS_RESOURCE, PermissionConfig::class.java) ?: PermissionConfig()

    val allProducts = permissionConfig.values.mapNotNull { it.provisioning }.flatten()

    fun productsForPermission(permission: String): List<String>? {
        val definition = permissionConfig[permission]?.provisioning ?: emptyList()
        if (definition.isEmpty()) {
            Timber.w("Permission is not defined for: $permission. Will never have permission!")
        }
        return definition
    }
}
