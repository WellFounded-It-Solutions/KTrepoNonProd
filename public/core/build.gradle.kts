plugins {
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.android")
}

android {
    testOptions.unitTests.isIncludeAndroidResources = true
}

repositories {
    maven { url = uri("https://artifacts.netcore.co.in/artifactory/android") }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Test
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.androidxTestCore)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.mockito)
    testImplementation("org.json:json:20190722")

    // AndroidX
    implementation(Dependencies.appCompat)
    implementation(Dependencies.lifecycleProcess)
    implementation(Dependencies.lifecycleViewModelKtx)
    implementation(Dependencies.annotation)
    api(Dependencies.constraintLayout)
    api(Dependencies.recyclerView)
    implementation(Dependencies.swipeRefreshLayout)
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.preferenceKtx)
    implementation(Dependencies.androidxBrowser)
    implementation(Dependencies.workManagerKtx)

    // Google
    implementation(Dependencies.gson)
    api(Dependencies.material)

    // Firebase
    implementation(platform(Dependencies.firebaseBom))
    implementation(Dependencies.firebaseplayintegrity)
    implementation(Dependencies.firebaseAnalyticsKtx)
    implementation(Dependencies.firebaseCrashlyticsKtx)
    implementation(Dependencies.firebaseAuthKtx)
    implementation(Dependencies.firebaseFunctionsKtx)

    // RxJava
    implementation(Dependencies.rxJava2)
    implementation(Dependencies.rxAndroid2)
    implementation(Dependencies.rxRelay2)
    implementation(Dependencies.rxBinding3)

    // Glide
    implementation(Dependencies.glide)
    kapt(Dependencies.glideCompiler)

    // Square
    implementation(Dependencies.okio)
    implementation(Dependencies.okHttp)
    implementation(Dependencies.okHttpLoggingInterceptor)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)

    // Misc
    implementation("com.android.billingclient:billing:7.0.0")
    implementation("com.google.android.gms:play-services-auth:19.0.0") // SmartLockManager
    implementation("com.airbnb.android:mvrx:1.5.1")
    implementation("com.github.RedMadRobot:input-mask-android:6.1.0")
    implementation("com.google.android:flexbox:1.1.0")
    implementation("me.zhanghai.android.materialprogressbar:library:1.6.1")
    //  api("com.romandanylyk:pageindicatorview:1.0.3@aar")
    implementation("com.github.vivekpratapsinghnaviga:PageIndicatorView:1.0.4")

    implementation("com.github.YarikSOffice:lingver:1.2.1")
    api("com.samskivert:jmustache:1.15")
    implementation("com.github.jknack:handlebars:4.1.2")

    //Netcore
    implementation("com.netcore.android:smartech-sdk:3.5.4")
    implementation("com.netcore.android:smartech-nudges:10.2.0")

    implementation("androidx.core:core-splashscreen:1.0.0-beta02")

    implementation("com.google.android.play:integrity:1.3.0")

}

configurations.all {
    exclude(group = "com.google.android.gms", module = "play-services-safetynet")
}