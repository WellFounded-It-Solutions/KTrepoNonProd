package se.infomaker.livecontentui.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SharingBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SharingAllowedDomains

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SharingOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SharingLoggingInterceptor