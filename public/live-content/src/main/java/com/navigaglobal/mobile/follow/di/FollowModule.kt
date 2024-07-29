package com.navigaglobal.mobile.follow.di

import com.google.gson.Gson
import com.navigaglobal.mobile.follow.migration.FollowMigration
import com.navigaglobal.mobile.follow.migration.MigrationCommandFactory
import com.navigaglobal.mobile.follow.migration.command.ConceptExistsCommandFactory
import com.navigaglobal.mobile.follow.migration.command.DeleteSubscriptionCommandFactory
import com.navigaglobal.mobile.follow.migration.command.MutateSubscriptionCommandFactory
import com.navigaglobal.mobile.follow.migration.config.MigrationFollowConfig
import com.navigaglobal.mobile.migration.Config
import com.navigaglobal.mobile.migration.Migration
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import java.util.AbstractMap

@Module
@InstallIn(SingletonComponent::class)
abstract class FollowModule {

    @Binds @IntoSet
    @DefaultMigrationCommandFactories
    abstract fun bindConceptExistsTaskFactory(conceptExistsTaskFactory: ConceptExistsCommandFactory): MigrationCommandFactory

    companion object {

        @Provides @ElementsIntoSet
        fun provideFollowMigration(
            configs: Map<Int, @JvmSuppressWildcards List<Config.Migration>>?,
            gson: Gson,
            @DefaultMigrationCommandFactories defaultFactories: Set<@JvmSuppressWildcards MigrationCommandFactory>
        ): Set<Map.Entry<Int, Set<Migration>>> {
            return configs?.filter { config ->
                config.value.any { migration -> migration.identifier == FollowMigration.IDENTIFIER }
            }?.mapNotNull { (version, versionConfigs) ->
                versionConfigs.firstOrNull { it.identifier == FollowMigration.IDENTIFIER }?.let { migrationConfig ->
                    val factories = defaultFactories.toMutableSet()
                    gson.fromJson(migrationConfig.config, MigrationFollowConfig::class.java)?.let { config ->
                        config.mutateSubscription?.let {
                            factories.add(MutateSubscriptionCommandFactory(it))
                        }
                        config.deleteSubscription?.let {
                            factories.add(DeleteSubscriptionCommandFactory(it))
                        }
                    }
                    AbstractMap.SimpleImmutableEntry(version, setOf(FollowMigration(factories)))
                }
            }?.toSet()
                ?: emptySet()
        }
    }
}