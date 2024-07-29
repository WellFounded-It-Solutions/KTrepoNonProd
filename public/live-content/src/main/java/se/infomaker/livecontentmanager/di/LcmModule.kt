package se.infomaker.livecontentmanager.di

import com.navigaglobal.mobile.di.TokenServiceBaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.globalConfig
import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentmanager.config.LiveContentConfigWrapper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LcmModule {

    @Provides
    @Singleton
    @GlobalLiveContentConfig
    fun provideGlobalLiveContentConfig(configManager: ConfigManager): LiveContentConfig {
        val wrapper = configManager.globalConfig<LiveContentConfigWrapper>()
        return wrapper.liveContent
    }

    @Provides
    @TokenServiceBaseUrl
    fun provideTokenBaseUrl(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
        return liveContentConfig.tokenServiceUrl
    }
}