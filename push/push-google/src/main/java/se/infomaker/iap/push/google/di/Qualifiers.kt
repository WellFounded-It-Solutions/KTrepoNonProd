package se.infomaker.iap.push.google.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PushRegistrationLoggingInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PushRegistrationOkHttpClient