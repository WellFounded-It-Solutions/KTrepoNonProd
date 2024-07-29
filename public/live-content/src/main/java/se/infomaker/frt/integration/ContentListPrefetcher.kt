package se.infomaker.frt.integration

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import se.infomaker.frt.moduleinterface.prefetch.PrefetchWorker
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.livecontentmanager.query.QueryFilter
import se.infomaker.livecontentui.LiveContentStreamProvider
import se.infomaker.livecontentui.config.LiveContentUIConfig
import timber.log.Timber
import javax.inject.Inject

class ContentListPrefetcher(
    private val streamProvider: LiveContentStreamProvider,
    private val moduleIdentifier: String
) : PrefetchWorker {

    override fun prefetch(context: Context): Boolean {
        Timber.d("ContentListPrefetcher, prefetch for module=$moduleIdentifier.")
        val gson: Gson = GsonBuilder().serializeNulls().create()
        val config = ConfigManager.getInstance(context).getConfig(moduleIdentifier, LiveContentUIConfig::class.java, gson)
        streamProvider.provide(config.liveContent, config.getProperties(), emptyList<QueryFilter>()).searchMore()
        return true
    }
}