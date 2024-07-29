package com.navigaglobal.mobile.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsDebuggable

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PackageName

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VersionCode

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VersionName

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppName

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InstallationIdentifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InstallationIdentifierPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MigrationPreferences