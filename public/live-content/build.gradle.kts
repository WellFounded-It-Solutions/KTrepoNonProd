plugins {
    id("kotlin-parcelize")
    id("realm-android")
}


android {
    defaultConfig {
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    androidTestImplementation(Dependencies.junit)
    androidTestImplementation(Dependencies.androidxTestCore)
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.room:room-testing:${Versions.room}")

    testImplementation(Dependencies.junit)
    testImplementation(kotlin("test"))
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.mockito)
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")

    // AndroidX
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.fragmentKtx)
    implementation(Dependencies.appCompat)
    implementation(Dependencies.swipeRefreshLayout)
    implementation(Dependencies.androidxBrowser)
    implementation(Dependencies.webkit)
    implementation(Dependencies.roomRuntime)
    implementation(Dependencies.roomRxJava2)
    implementation(Dependencies.roomKtx)
    kapt(Dependencies.roomCompiler)

    // Google
    implementation(Dependencies.gson)
    implementation(Dependencies.material)

    // RxJava
    implementation(Dependencies.rxJava2)
    implementation(Dependencies.rxAndroid2)
    implementation(Dependencies.rxKotlin2)
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
    implementation(Dependencies.retrofitAdapterRxJava2)

    //socket.io
    implementation("io.socket:socket.io-client:1.0.0") {
        exclude(group = "org.json", module = "json")
    }

    // Misc
    implementation("com.google.android.gms:play-services-base:18.0.1")
    implementation("jp.wasabeef:glide-transformations:4.1.0")
    implementation("com.samskivert:jmustache:1.15")
   // implementation("com.romandanylyk:pageindicatorview:1.0.3@aar")
    implementation("com.github.vivekpratapsinghnaviga:PageIndicatorView:1.0.4")

    // Infomaker
    api(project(":public:core"))
}