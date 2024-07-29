package se.infomaker.frt.moduleinterface.prefetch

interface Prefetcher {
    fun getPrefetchWorker(): PrefetchWorker
}