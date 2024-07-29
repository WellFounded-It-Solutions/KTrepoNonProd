package se.infomaker.frt.statistics.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import se.infomaker.frt.statistics.StatisticsConfig
import se.infomaker.frt.statistics.blacklist.BlackList
import se.infomaker.frt.statistics.blacklist.BlackListBackend
import se.infomaker.frt.statistics.blacklist.BlackListSharedPreferencesStore
import se.infomaker.frt.statistics.blacklist.Store
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.privatePreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StatisticsModule {

    @Binds
    abstract fun bindBlackListStore(blackListStore: BlackListSharedPreferencesStore): Store<BlackList>

    companion object {

        @Provides
        @StatisticsDisablerBaseUrl
        fun provideStatisticsDisablerBaseUrl(config: StatisticsConfig): String? {
            return config.statisticsDisablerBaseUrl
        }

        @Provides
        fun provideStatisticsConfig(configManager: ConfigManager): StatisticsConfig {
            return configManager.getConfig("core", StatisticsConfig::class.java)
        }

        @Provides
        @Singleton
        @BlackListOkHttpClient
        fun provideBlackListOkHttpClient(okHttpClient: OkHttpClient, httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
            return okHttpClient.newBuilder()
                .addNetworkInterceptor(httpLoggingInterceptor)
                .build()
        }

        @Provides
        @Singleton
        fun provideBlackListBackend(@StatisticsDisablerBaseUrl baseUrl: String?, @BlackListOkHttpClient okHttpClient: OkHttpClient): BlackListBackend {
            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create()
        }

        @Provides
        @Singleton
        @BlackListPreferences
        fun provideBlackListPreferences(@ApplicationContext context: Context): SharedPreferences {
            return context.privatePreferences("blacklist")
        }
    }
}