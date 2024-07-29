package se.infomaker.iap.update.di

import android.content.Context
import android.content.SharedPreferences
import com.navigaglobal.mobile.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.infomaker.frtutilities.ktx.privatePreferences
import se.infomaker.iap.update.version.VersionService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    @Provides
    @VersionServiceBaseUrl
    fun provideVersionServiceBaseUrl(@ApplicationContext context: Context): String {
        val platform = context.getString(R.string.target_platform)
        return "https://api.khaleejtimes.com/appversion/get/google/"
        //Used in ios env
        //   return "https://app-update-versions.s3-eu-west-1.amazonaws.com/$platform/"

    }

    @Provides
    @Singleton
    @VersionServiceOkHttpClient
    fun provideVersionStoreOkHttpClient(okHttpClient: OkHttpClient, httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return okHttpClient.newBuilder()
            .addNetworkInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideVersionService(@VersionServiceBaseUrl baseUrl: String, @VersionServiceOkHttpClient okHttpClient: OkHttpClient): VersionService {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VersionService::class.java)
    }

    @Provides
    @VersionStorePreferences
    @Singleton
    fun provideVersionStorePreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.privatePreferences("version_info")
    }
}