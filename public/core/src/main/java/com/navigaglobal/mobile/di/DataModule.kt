package com.navigaglobal.mobile.di

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.UUID

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @InstallationIdentifier
    fun provideInstallationIdentifier(@InstallationIdentifierPreferences sharedPreferences: SharedPreferences): String {
        var uuid = sharedPreferences.getString("uuid", null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPreferences.edit {
                putString("uuid", uuid)
            }
        }
        return uuid
    }
}