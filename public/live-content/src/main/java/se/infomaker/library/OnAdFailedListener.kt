package se.infomaker.library

/**
 * Common contract to get same callback no matter the provider the ad originates from.
 */
interface OnAdFailedListener {
    /**
     * Called when ad load has failed
     */
    fun onAdFailed()
}
