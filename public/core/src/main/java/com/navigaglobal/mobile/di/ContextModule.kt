package com.navigaglobal.mobile.di

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.infomaker.frtutilities.ktx.getAppName
import se.infomaker.frtutilities.ktx.getVersionCode
import se.infomaker.frtutilities.ktx.getVersionName
import se.infomaker.frtutilities.ktx.isDebuggable

@Module
@InstallIn(SingletonComponent::class)
object ContextModule {

    @Provides
    @IsDebuggable
    fun provideIsDebuggable(@ApplicationContext context: Context): Boolean {
        return context.isDebuggable
    }

    @Provides
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager {
        return context.assets
    }

    @Provides
    @PackageName
    fun providePackageName(@ApplicationContext context: Context): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.packageName
    }

    @Provides
    @VersionCode
    fun provideVersionCode(@ApplicationContext context: Context): Long {
        return context.getVersionCode()
    }

    @Provides
    @VersionName
    fun provideVersionName(@ApplicationContext context: Context): String {
        return context.getVersionName()
    }

    @Provides
    @AppName
    fun provideAppName(@ApplicationContext context: Context): String {
        return context.getAppName().toString()
    }
}