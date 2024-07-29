package se.infomaker.frt.statistics.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StatisticsDisablerBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BlackListPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BlackListOkHttpClient