package com.navigaglobal.mobile.migration

import android.content.SharedPreferences
import androidx.core.content.edit
import com.navigaglobal.mobile.di.MigrationPreferences
import javax.inject.Inject

class MigrationGuard @Inject constructor(
    @MigrationPreferences private val preferences: SharedPreferences
) {
    fun shouldRun(migration: Migration, version: Int): Boolean {
        val completedVersion = preferences.getInt(migration.persistenceKey, 0)
        return completedVersion < version
    }

    fun complete(migration: Migration, completedVersion: Int) {
        preferences.edit {
            putInt(migration.persistenceKey, completedVersion)
        }
    }

    private val Migration.persistenceKey: String
        get() = "$identifier$MIGRATION_PERSISTENCE_SUFFIX"

    companion object Keys {
        private const val MIGRATION_PERSISTENCE_SUFFIX = "_migration_completed_version"
    }
}