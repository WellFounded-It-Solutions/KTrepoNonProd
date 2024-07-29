package com.navigaglobal.mobile.migration

import com.navigaglobal.mobile.di.ApplicationScope
import com.navigaglobal.mobile.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MigrationRunner @Inject constructor(
    private val migrationGuard: MigrationGuard,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val migrations: Map<Int, @JvmSuppressWildcards Set<@JvmSuppressWildcards Migration>>
) {
    fun runMigrations() {
        coroutineScope.launch {
            withContext(ioDispatcher) {
                migrations.toSortedMap().forEach { (version, versionedMigrations) ->
                    versionedMigrations.forEach { migration ->
                        if (migrationGuard.shouldRun(migration, version)) {
                            val result = migration.migrate()
                            if (result.isSuccess) {
                                migrationGuard.complete(migration, version)
                            }
                            else {
                                Timber.e(result.exceptionOrNull(), "${migration.identifier} migration, version [$version] failed.")
                                return@withContext
                            }
                        }
                    }
                }
            }
        }
    }
}