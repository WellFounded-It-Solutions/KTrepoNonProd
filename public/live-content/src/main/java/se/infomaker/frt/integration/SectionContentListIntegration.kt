package se.infomaker.frt.integration

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.infomaker.frt.moduleinterface.prefetch.PrefetchWorker
import se.infomaker.frt.moduleinterface.prefetch.Prefetcher
import se.infomaker.livecontentui.di.DataSourceProviderFactory
import se.infomaker.livecontentui.prefetch.SectionedLiveContentPrefetcher
import se.infomaker.livecontentui.section.configuration.SectionedLiveContentUIConfig

import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ModuleInformationManager


class SectionContentListIntegration(private val context: Context, private val moduleIdentifier: String) : ContentListIntegration(context, moduleIdentifier), Prefetcher {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SectionContentListIntegrationEntryPoint {
        fun dataSourceProviderFactory(): DataSourceProviderFactory
    }

    override fun getPrefetchWorker(): PrefetchWorker {
        val moduleName = ModuleInformationManager.getInstance().getModuleName(moduleIdentifier)
        val config = ConfigManager.getInstance(context).getConfig(moduleName, moduleIdentifier, SectionedLiveContentUIConfig::class.java)
        val entryPoint = EntryPointAccessors.fromApplication(context, SectionContentListIntegrationEntryPoint::class.java)
        return SectionedLiveContentPrefetcher(entryPoint.dataSourceProviderFactory().create(config), moduleIdentifier)
    }
}