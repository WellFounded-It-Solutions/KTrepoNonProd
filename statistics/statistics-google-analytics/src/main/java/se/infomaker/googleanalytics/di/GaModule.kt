package se.infomaker.googleanalytics.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import se.infomaker.googleanalytics.dispatcher.GoogleAnalyticsApi
import se.infomaker.googleanalytics.dispatcher.UserAgentInterceptor
import se.infomaker.googleanalytics.register.GaDatabase
import se.infomaker.googleanalytics.register.HitDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GaModule {

    @Provides
    @Singleton
    fun provideGaDatabase(@ApplicationContext context: Context): GaDatabase {
        return Room
            .databaseBuilder(context, GaDatabase::class.java, "GAHitsRegister.db")
            .build()
    }

    @Provides
    fun provideHitDao(gaDatabase: GaDatabase): HitDao {
        return gaDatabase.hitDao()
    }

    @Provides
    @Singleton
    @GaOkHttpClient
    fun provideGaOkHttpClient(
        okHttpClient: OkHttpClient,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        userAgentInterceptor: UserAgentInterceptor
    ): OkHttpClient {
        return okHttpClient.newBuilder()
            .addNetworkInterceptor(httpLoggingInterceptor)
            .addInterceptor(userAgentInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideGaApi(@GaOkHttpClient okHttpClient: OkHttpClient): GoogleAnalyticsApi {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl("https://www.google-analytics.com")
            .build()
            .create()
    }
}