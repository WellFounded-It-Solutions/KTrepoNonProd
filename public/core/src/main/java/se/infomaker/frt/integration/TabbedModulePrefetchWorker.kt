package se.infomaker.frt.integration

import android.content.Context
import se.infomaker.frt.moduleinterface.prefetch.PrefetchWorker
import se.infomaker.frt.moduleinterface.prefetch.Prefetcher
import timber.log.Timber

class TabbedModulePrefetchWorker(private val moduleId: String, private val prefetchers: List<Prefetcher>) : PrefetchWorker {
    override fun prefetch(context: Context): Boolean {
        Timber.d("Prefetching $moduleId tabs.")
        prefetchers.forEach {
            it.getPrefetchWorker().prefetch(context)
        }
        return true
    }
}
