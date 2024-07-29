package se.infomaker.library

interface BidProvider {
    /**
     * Used to determine if the provider can fetch bids for placement
     */
    fun canHandlePlacement(placementId: String) : Boolean

    /**
     * Tries to get a bid for placementId and deliver to callback, if it takes longer then timeout millis
     * or the re the callback is called with null bid
     */
    fun getBid(placementId: String, timeout: Long, callback: (Bid?) -> Unit)
}
