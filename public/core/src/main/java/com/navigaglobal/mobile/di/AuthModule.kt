package com.navigaglobal.mobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import com.navigaglobal.mobile.auth.TokenService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    @TokenServiceOkHttpClient
    fun provideTokenServiceOkHttpClient(okHttpClient: OkHttpClient, httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return okHttpClient.newBuilder()
            .addNetworkInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    fun provideTokenService(@TokenServiceBaseUrl tokenServiceUrl: String?, @TokenServiceOkHttpClient okHttpClient: OkHttpClient): TokenService? {
        if (tokenServiceUrl == null) return null
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(tokenServiceUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }
}