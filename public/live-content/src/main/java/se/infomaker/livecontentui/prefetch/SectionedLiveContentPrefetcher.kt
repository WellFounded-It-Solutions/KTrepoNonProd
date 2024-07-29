package se.infomaker.livecontentui.prefetch

import android.content.Context
import se.infomaker.frt.moduleinterface.prefetch.PrefetchWorker
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.livecontentui.section.SectionManager
import se.infomaker.livecontentui.section.configuration.SectionedLiveContentUIConfig
import se.infomaker.livecontentui.section.datasource.DataSource
import se.infomaker.livecontentui.section.datasource.DataSourceProvider
import timber.log.Timber

class SectionedLiveContentPrefetcher(private val dataSourceProvider: DataSourceProvider, private val moduleIdentifier: String) : PrefetchWorker {

    override fun prefetch(context: Context): Boolean {
        Timber.d("SectionedLiveContentPrefetcher, prefetch for module=$moduleIdentifier.")
        val dataSourceProviders = mutableSetOf<DataSource>()
        val moduleName = ModuleInformationManager.getInstance().getModuleName(moduleIdentifier)
        val moduleTitle = ModuleInformationManager.getInstance().getModuleTitle(moduleIdentifier)
        val config = ConfigManager.getInstance(context.applicationContext).getConfig(moduleIdentifier, SectionedLiveContentUIConfig::class.java)
        val sections = SectionManager.getInstance().create(dataSourceProvider, config, moduleTitle)
        sections?.forEachIndexed { index, _ ->
            dataSourceProvider.getSource(config?.sections?.get(index), null)?.let { dataSource ->
                dataSourceProviders.add(dataSource)
            }
        }

        dataSourceProviders.forEach { provider ->
            provider.update()
        }
        return true
    }
}