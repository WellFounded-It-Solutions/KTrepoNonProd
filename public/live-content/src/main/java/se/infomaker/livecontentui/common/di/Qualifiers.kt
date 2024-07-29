package se.infomaker.livecontentui.common.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GlobalLiveContentUiConfig

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GlobalPropertyObjectParser