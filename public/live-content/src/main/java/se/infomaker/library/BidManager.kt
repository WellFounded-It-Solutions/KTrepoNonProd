package se.infomaker.library

object BidManager {
    private val providers: MutableSet<BidProvider> = mutableSetOf()

    fun registerProvider(viewProvider: BidProvider) {
        providers.add(viewProvider)
    }

    /**
     * Tries to get a bid for placement id using registered providers,
     * if provider fails before timeout, null is used in callback
     */
    fun getBid(placementId: String, timeout: Long = 10000, callback: (Bid?) -> Unit) {
        val provider = providers.firstOrNull {
            it.canHandlePlacement(placementId)
        }
        if(provider != null) {
            provider.getBid(placementId, timeout, callback)
        }
        else {
            callback.invoke(null)
        }
    }
}