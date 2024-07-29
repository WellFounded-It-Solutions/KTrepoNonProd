package se.infomaker.livecontentmanager.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerV2BaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerAuthProvider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerTokenServiceClientId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerTokenServiceClientSecret

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerClientCredentialsAuthorizationProvider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerBasicAuthUsername

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerBasicAuthPassword

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerBasicAuthAuthorizationProvider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerLoggingInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QueryStreamerOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InfocasterUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InfocasterEventNotifierBroadcastId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InfocasterLoggingInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InfocasterOkHttpClient