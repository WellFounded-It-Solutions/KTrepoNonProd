package se.infomaker.streamviewer.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.globalConfig
import se.infomaker.frtutilities.ktx.privatePreferences
import se.infomaker.streamviewer.config.FollowConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FollowModule {

    private const val REMOTE_NOTIFICATION_CONFIG_KEY = "com.naviga.RemoteNotificationConfig"

    @Provides
    @Singleton
    @GlobalFollowConfig
    fun provideGlobalFollowConfig(configManager: ConfigManager): FollowConfig {
        return configManager.globalConfig()
    }

    @Provides
    @RemoteNotificationConfigPreferences
    fun provideRemoteNotificationConfigPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.privatePreferences(REMOTE_NOTIFICATION_CONFIG_KEY)
    }
}