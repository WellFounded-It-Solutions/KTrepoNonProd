package se.infomaker.frtutilities.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.infomaker.frtutilities.ConfigManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilitiesModule {

    @Provides
    @Singleton
    fun provideConfigManager(@ApplicationContext context: Context): ConfigManager {
        return ConfigManager.getInstance(context)
    }
}