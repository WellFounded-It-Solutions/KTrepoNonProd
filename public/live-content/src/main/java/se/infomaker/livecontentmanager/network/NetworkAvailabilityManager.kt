package se.infomaker.livecontentmanager.network

interface NetworkAvailabilityManager {
    /**
     * Used to determine behavior depending on network access
     *
     * @return true if the device has a network connection
     */
    fun hasNetwork(): Boolean
}