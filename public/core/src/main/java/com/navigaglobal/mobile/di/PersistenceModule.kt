package com.navigaglobal.mobile.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.infomaker.frtutilities.ktx.privatePreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    private const val MIGRATION_PREFERENCES = "com.navigaglobal.mobile.migration_preferences"

    @Provides
    @Singleton
    fun provideDefaultSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.privatePreferences()
    }

    @Provides
    @MigrationPreferences
    fun provideMigrationPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.privatePreferences(MIGRATION_PREFERENCES)
    }

    @Provides
    @InstallationIdentifierPreferences
    fun provideInstallationIdentifierPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.privatePreferences("Identifier")
    }
}