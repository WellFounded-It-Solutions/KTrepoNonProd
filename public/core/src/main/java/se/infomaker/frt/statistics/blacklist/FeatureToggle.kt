package se.infomaker.frt.statistics.blacklist

interface FeatureToggle {
    fun isEnabled(identifier: String): Boolean
}