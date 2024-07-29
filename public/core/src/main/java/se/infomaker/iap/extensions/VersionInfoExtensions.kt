package se.infomaker.iap.extensions

import se.infomaker.iap.update.UpdateType
import se.infomaker.iap.update.version.VersionInfo

fun VersionInfo.asUpdateType(current: Long): UpdateType {
    return when {
        obsolete -> UpdateType.OBSOLETE
        current < required -> UpdateType.REQUIRED
        current < recommended -> UpdateType.RECOMMENDED
        else -> UpdateType.NONE
    }
}