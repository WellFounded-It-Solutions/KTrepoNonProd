package se.infomaker.livecontentmanager.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentCacheDir

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentAuthProvider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentTokenServiceClientId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentTokenServiceClientSecret

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentClientCredentialsAuthorizationProvider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentBasicAuthUsername

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentBasicAuthPassword

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentBasicAuthAuthorizationProvider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentLoggingInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenContentOkHttpClient