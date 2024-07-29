package se.infomaker.livecontentmanager.di

import android.content.Context
import com.navigaglobal.mobile.di.IsDebuggable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.navigaglobal.mobile.auth.AuthorizationProvider
import com.navigaglobal.mobile.auth.AuthorizationProviderManager
import com.navigaglobal.mobile.auth.BasicAuthAuthorizationProvider
import com.navigaglobal.mobile.auth.ClientCredentialsAuthorizationProvider
import com.navigaglobal.mobile.auth.TokenService
import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentmanager.network.NetworkAvailabilityManager
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentBuilder
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService
import timber.log.Timber
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpenContentModule {

    @Provides
    @OpenContentBaseUrl
    fun provideOpenContentBaseUrl(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String {
        return liveContentConfig.opencontent ?: throw NullPointerException("No configured Open Content URL.")
    }

    @Provides
    @OpenContentBasicAuthUsername
    fun provideOpenContentUsername(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
        return liveContentConfig.opencontentUsername
    }

    @Provides
    @OpenContentBasicAuthPassword
    fun provideOpenContentPassword(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
        return liveContentConfig.opencontentPassword
    }

    @Provides
    @OpenContentTokenServiceClientId
    fun provideOpenContentTokenServiceClientId(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
        return liveContentConfig.opencontentClientId
    }

    @Provides
    @OpenContentTokenServiceClientSecret
    fun provideOpenContentTokenServiceClientSecret(@GlobalLiveContentConfig liveContentConfig: LiveContentConfig): String? {
        return liveContentConfig.opencontentClientSecret
    }

    @Provides
    @OpenContentCacheDir
    fun provideOpenContentCacheDir(@ApplicationContext context: Context): File {
        return File(context.cacheDir, "offline").also { it.mkdirs() }
    }

    @Provides
    @OpenContentBasicAuthAuthorizationProvider
    fun provideBasicAuthorizationProvider(
        @OpenContentBasicAuthUsername username: String?,
        @OpenContentBasicAuthPassword password: String?
    ): AuthorizationProvider? {
        if (username == null || password == null) return null
        return BasicAuthAuthorizationProvider(username, password)
    }

    @Provides
    @OpenContentClientCredentialsAuthorizationProvider
    fun provideClientCredentialsAuthProvider(
        @OpenContentTokenServiceClientId clientId: String?,
        @OpenContentTokenServiceClientSecret clientSecret: String?,
        tokenService: TokenService?
    ): AuthorizationProvider? {
        if (clientId == null || clientSecret == null || tokenService == null) return null
        return ClientCredentialsAuthorizationProvider(clientId, clientSecret, tokenService)
    }

    @Provides
    @OpenContentAuthProvider
    fun provideOpenContentAuthProvider(
        @OpenContentBaseUrl openContentBaseUrl: String,
        @OpenContentClientCredentialsAuthorizationProvider clientCredentialsAuth: AuthorizationProvider?,
        @OpenContentBasicAuthAuthorizationProvider basicAuthAuthProvider: AuthorizationProvider?
    ): AuthorizationProvider {
        return AuthorizationProviderManager.get(openContentBaseUrl)
            ?: clientCredentialsAuth
            ?: basicAuthAuthProvider
            ?: throw NullPointerException("No valid Open Content credentials configured.")
    }

    @Provides
    @OpenContentLoggingInterceptor
    fun provideQueryStreamerLoggingInterceptor(@IsDebuggable isDebuggable: Boolean): HttpLoggingInterceptor? {
        return if (isDebuggable) {
            HttpLoggingInterceptor { message ->
                Timber.tag("OpenContent")
                Timber.d("OpenContentMessage: %s", message)
            }.also { it.level = HttpLoggingInterceptor.Level.BODY }
        }
        else null
    }

    @Provides
    @Singleton
    @OpenContentOkHttpClient
    fun provideQueryStreamerOkHttpClient(okHttpClient: OkHttpClient, @OpenContentLoggingInterceptor interceptor: HttpLoggingInterceptor?): OkHttpClient {
        val builder = okHttpClient.newBuilder()

        interceptor?.let {
            builder.addNetworkInterceptor(it)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideOpenContentService(
        @OpenContentBaseUrl openContentBaseUrl: String,
        @OpenContentCacheDir openContentCacheDir: File,
        networkAvailabilityManager: NetworkAvailabilityManager,
        @OpenContentAuthProvider authProvider: AuthorizationProvider,
        @OpenContentOkHttpClient okHttpClient: OkHttpClient
    ): OpenContentService {
        return OpenContentBuilder()
            .setBaseUrl(openContentBaseUrl)
            .setCacheDir(openContentCacheDir)
            .setNetworkAvailabilityManager(networkAvailabilityManager)
            .setAuthorizationProvider(authProvider)
            .setClient(okHttpClient)
            .build()
    }
}