package se.infomaker.iap.map

object NoOpMapViewHolderFactory : MapViewHolderFactory {
    override fun create() = NoOpMapViewHolder
}