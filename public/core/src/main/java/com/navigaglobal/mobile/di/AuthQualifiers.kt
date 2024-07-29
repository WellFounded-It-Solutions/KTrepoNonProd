package com.navigaglobal.mobile.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenServiceBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenServiceOkHttpClient