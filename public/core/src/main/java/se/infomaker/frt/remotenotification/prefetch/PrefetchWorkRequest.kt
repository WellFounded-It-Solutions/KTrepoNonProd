package se.infomaker.frt.prefetch

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import se.infomaker.frt.module.ModuleIntegrationProvider
import se.infomaker.frt.moduleinterface.prefetch.Prefetcher
import se.infomaker.frtutilities.ForegroundDetector
import timber.log.Timber

class PrefetchWorkRequest(context: Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {

    companion object {
        const val tag = "prefetchWorkRequest"
    }

    private val prefetcherWorkers = ModuleIntegrationProvider.getInstance(context).integrationList
            .mapNotNull { integration -> (integration as? Prefetcher)?.getPrefetchWorker() }

    override suspend fun doWork(): Result {
        if(!ForegroundDetector.observable().blockingSingle(false)) {
            Timber.d("App in foreground, no need to prefetch.")
            return Result.success()
        }
        prefetcherWorkers.forEach {
            it.prefetch(applicationContext)
        }
        return Result.success()
    }
}