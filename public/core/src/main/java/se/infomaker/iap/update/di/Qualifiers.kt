package se.infomaker.iap.update.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class VersionServiceBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class VersionStorePreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class VersionServiceOkHttpClient