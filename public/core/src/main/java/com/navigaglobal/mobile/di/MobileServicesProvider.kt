package com.navigaglobal.mobile.di

import dagger.MapKey

enum class MobileServicesProvider {
    GOOGLE, HUAWEI
}

@MapKey
annotation class MobileServicesProviderKey(val value: MobileServicesProvider)