package se.infomaker.streamviewer.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GlobalFollowConfig

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteNotificationConfigPreferences