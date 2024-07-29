package se.infomaker.livecontentui

import se.infomaker.livecontentmanager.parser.PropertyObject

interface PropertyObjectItemProvider {
    fun getPropertyObjectForPosition(position: Int): PropertyObject?
}