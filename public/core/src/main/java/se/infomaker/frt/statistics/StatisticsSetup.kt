package se.infomaker.frt.statistics

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import se.infomaker.frt.statistics.blacklist.BlackListManager
import se.infomaker.frtutilities.AbstractInitContentProvider
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ConfigPropertyFinder
import se.infomaker.frtutilities.runtimeconfiguration.OnConfigChangeListener
import timber.log.Timber
import java.util.HashSet

class StatisticsSetup : AbstractInitContentProvider(), OnConfigChangeListener {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface StatisticsSetupEntryPoint {
        fun config(): StatisticsConfig
        fun configManager(): ConfigManager
        fun blackListManager(): BlackListManager
    }

    private val Context.statisticsEntryPoint
        get() = EntryPointAccessors.fromApplication(this, StatisticsSetupEntryPoint::class.java)

    override fun init(context: Context) {
        val entryPoint = context.statisticsEntryPoint
        setupStatistics(context, entryPoint)
        entryPoint.configManager().addOnConfigChangeListener(this)
    }

    private fun setupStatistics(context: Context, entryPoint: StatisticsSetupEntryPoint) {
        val statisticsConfig = entryPoint.config()
        if (statisticsConfig.statisticsProviders.isNullOrEmpty()) {
            return
        }

        if (!statisticsConfig.statisticsDisablerBaseUrl.isNullOrEmpty()) {
            StatisticsManager.getInstance().setBlackList(entryPoint.blackListManager())
        }

        statisticsConfig.statisticsProviders.forEach { statisticsProvider ->
            val statisticsProviderConfigPropertyFinder = ConfigPropertyFinder(statisticsProvider)
            val providerName = statisticsProviderConfigPropertyFinder.getProperty(String::class.java, "provider")
            val config = statisticsProviderConfigPropertyFinder.getProperty(Map::class.java, "config") as Map<String, Any>

            try {
                val clazz = Class.forName("$STATISTICS_SERVICE_PACKAGE_NAME.$providerName$STATISTICS_SERVICE_SUFFIX")
                val statisticsService = clazz.newInstance() as StatisticsManager.StatisticsService
                statisticsService.init(context, config)
                StatisticsManager.getInstance().registerService(statisticsService)
            }
            catch (e: Exception) {
                Timber.e(e, "Failed to register statistics service %s", providerName)
            }
        }
    }

    override fun onChange(updated: MutableList<String>, removed: MutableList<String>): Set<String> {
        context?.let { ctx ->
            if (updated.contains(CORE_CONFIG_ASSETS_PATH)) {
                StatisticsManager.getInstance().clearServices()
                setupStatistics(ctx, ctx.statisticsEntryPoint)
            }
        }
        return emptySet()
    }

    companion object {
        private const val STATISTICS_SERVICE_PACKAGE_NAME = "se.infomaker.frt.statistics"
        private const val STATISTICS_SERVICE_SUFFIX = "Service"
        private const val CORE_CONFIG_ASSETS_PATH = "shared/configuration/core_config.json"
    }
}