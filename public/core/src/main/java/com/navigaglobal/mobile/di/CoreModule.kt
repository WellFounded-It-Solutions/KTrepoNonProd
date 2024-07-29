package com.navigaglobal.mobile.di

import android.content.Context
import com.google.gson.Gson
import com.navigaglobal.mobile.migration.Config
import com.navigaglobal.mobile.migration.Migration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import se.infomaker.frt.module.ModuleIntegrationProvider
import se.infomaker.frt.moduleinterface.ModuleIntegration
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.globalConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    @Multibinds abstract fun bindMigrationEntries(): Set<Map.Entry<Int, Set<Migration>>>

    companion object {

        @Provides
        @Singleton
        fun provideGson(): Gson {
            return Gson()
        }

        @Provides
        @Singleton
        fun provideModuleIntegrations(@ApplicationContext context: Context): List<ModuleIntegration> {
            return ModuleIntegrationProvider.getInstance(context).integrationList
        }

        @Provides
        fun provideMigrationsConfigs(configManager: ConfigManager): Map<Int, List<Config.Migration>>? {
            return configManager.globalConfig<Config>().migrations
        }

        /**
         * https://dagger.dev/dev-guide/multibindings.html
         * Maps whose keys are not known at compile time
         */
        @Provides
        fun provideMigrations(entries: Set<@JvmSuppressWildcards Map.Entry<Int, @JvmSuppressWildcards Set<@JvmSuppressWildcards Migration>>>): Map<Int, Set<Migration>> {
            return entries.map { entry ->
                entry.key to entry.value
            }.toMap()
        }
    }
}