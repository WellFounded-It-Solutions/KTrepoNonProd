package se.infomaker.streamviewer.extensions

import se.infomaker.frtutilities.ResourceManager

internal fun ResourceManager.getDrawableIdentifierOrFallback(resourceName: String, fallbackIdentifier: Int): Int {
    val identifier = getDrawableIdentifier(resourceName)
    return if (identifier != 0) identifier else fallbackIdentifier
}