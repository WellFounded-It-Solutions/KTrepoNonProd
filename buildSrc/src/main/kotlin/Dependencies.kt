object Versions {
    const val compileSdk = 33
    const val minSdk = 21
    const val targetSdk = 33

    const val kotlin = "1.7.10"
    const val coroutines = "1.6.3"

    const val activity = "1.5.1"
    const val lifecycle = "2.5.1"

    const val hilt = "2.43.2"
    const val glide = "4.13.2"
    const val room = "2.4.3"
    const val okHttp = "4.10.0"
    const val retrofit = "2.9.0"

    const val compose = "1.2.1"
    const val composeCompiler = "1.3.0"
}

object Dependencies {

    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.3.0"
    const val hiltGradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}"
    const val realmGradlePlugin = "io.realm:realm-gradle-plugin:5.9.1"
    const val googleServicesGradlePlugin = "com.google.gms:google-services:4.3.13"
    const val agConnectGradlePlugin = "com.huawei.agconnect:agcp:1.6.0.300"
    const val crashlyticsGradlePlugin = "com.google.firebase:firebase-crashlytics-gradle:2.9.1"
    const val mavenPublishGradlePlugin = "com.vanniktech:gradle-maven-publish-plugin:0.21.0"

    const val java8DesugarLibs = "com.android.tools:desugar_jdk_libs:1.1.5"

    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"

    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    const val coroutinesRxJava2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${Versions.coroutines}"

    const val firebaseBom = "com.google.firebase:firebase-bom:30.3.1"
    const val firebaseplayintegrity = "com.google.firebase:firebase-appcheck-playintegrity"
    const val firebaseMessagingKtx = "com.google.firebase:firebase-messaging-ktx"
    const val firebaseAnalyticsKtx = "com.google.firebase:firebase-analytics-ktx"
    const val firebaseCrashlyticsKtx = "com.google.firebase:firebase-crashlytics-ktx"
    const val firebaseAuthKtx = "com.google.firebase:firebase-auth-ktx"
    const val firebaseFunctionsKtx = "com.google.firebase:firebase-functions-ktx"

    const val timber = "com.jakewharton.timber:timber:5.0.1"

    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    const val appCompat = "androidx.appcompat:appcompat:1.5.0"
    const val lifecycleProcess = "androidx.lifecycle:lifecycle-process:${Versions.lifecycle}"
    const val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val datastorePreferences = "androidx.datastore:datastore-preferences:1.0.0"
    const val annotation = "androidx.annotation:annotation:1.4.0"
    const val recyclerView = "androidx.recyclerview:recyclerview:1.2.1"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    const val cardView = "androidx.cardview:cardview:1.0.0"
    const val androidxBrowser = "androidx.browser:browser:1.4.0"
    const val webkit = "androidx.webkit:webkit:1.5.0"
    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.room}"
    const val roomRxJava2 = "androidx.room:room-rxjava2:${Versions.room}"
    const val coreKtx = "androidx.core:core-ktx:1.8.0"
    const val activityKtx = "androidx.activity:activity-ktx:${Versions.activity}"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:1.5.2"
    const val preferenceKtx = "androidx.preference:preference-ktx:1.2.0"
    const val workManagerKtx = "androidx.work:work-runtime-ktx:2.7.1"

    const val gson = "com.google.code.gson:gson:2.9.1"
    const val material = "com.google.android.material:material:1.6.1"
    const val hilt = "com.google.dagger:hilt-android:${Versions.hilt}"
    const val hiltCompiler = "com.google.dagger:hilt-compiler:${Versions.hilt}"
    const val gmsAds = "com.google.android.gms:play-services-ads:21.1.0"

    const val okio = "com.squareup.okio:okio:3.2.0"
    const val okHttp = "com.squareup.okhttp3:okhttp:${Versions.okHttp}"
    const val okHttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okHttp}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitConverterGson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val retrofitConverterScalars = "com.squareup.retrofit2:converter-scalars:${Versions.retrofit}"
    const val retrofitAdapterRxJava2 = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"

    const val rxJava2 = "io.reactivex.rxjava2:rxjava:2.2.13"
    const val rxKotlin2 = "io.reactivex.rxjava2:rxkotlin:2.3.0"
    const val rxAndroid2 = "io.reactivex.rxjava2:rxandroid:2.1.1"
    const val rxRelay2 = "com.jakewharton.rxrelay2:rxrelay:2.1.1"
    const val rxBinding3 = "com.jakewharton.rxbinding3:rxbinding:3.1.0"

    const val junit = "junit:junit:4.13.2"
    const val androidxTestCore = "androidx.test:core:1.4.0"
    const val robolectric = "org.robolectric:robolectric:4.7.2"
    const val mockito = "org.mockito:mockito-core:4.0.0"
}