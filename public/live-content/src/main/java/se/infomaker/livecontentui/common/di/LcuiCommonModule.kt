package se.infomaker.livecontentui.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.globalConfig
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser
import se.infomaker.livecontentmanager.parser.PropertyObjectParser
import se.infomaker.livecontentui.config.LiveContentUIConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LcuiCommonModule {

    @Provides
    @Singleton
    @GlobalLiveContentUiConfig
    fun provideGlobalLiveContentUiConfig(configManager: ConfigManager): LiveContentUIConfig {
        return configManager.globalConfig()
    }

    @Provides
    @GlobalPropertyObjectParser
    fun provideGlobalPropertyObjectParser(@GlobalLiveContentUiConfig liveContentUiConfig: LiveContentUIConfig): PropertyObjectParser {
        val typePropertyMap = liveContentUiConfig.liveContent.typePropertyMap ?: emptyMap()
        val typeDescriptionTemplate = liveContentUiConfig.liveContent.typeDescriptionTemplate ?: emptyMap()
        return DefaultPropertyObjectParser(typePropertyMap, typeDescriptionTemplate, liveContentUiConfig.liveContent.transformSettings)
    }
}