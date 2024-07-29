android {

    buildFeatures {
        viewBinding=true
    }

}


dependencies {

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:rules:1.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}")
    testImplementation("androidx.test:core:1.3.0")
    testImplementation("org.json:json:20190722")
    testImplementation(Dependencies.robolectric)

    // AndroidX
    implementation(Dependencies.constraintLayout)
    implementation(Dependencies.coreKtx)

    // Google
    implementation(Dependencies.gson)
    implementation(Dependencies.material)
    implementation("com.google.android:flexbox:1.1.0")

    // RxJava
    implementation(Dependencies.rxJava2)
    implementation(Dependencies.rxKotlin2)
    implementation(Dependencies.rxAndroid2)
    implementation(Dependencies.rxRelay2)

    // Networking
    implementation(Dependencies.okHttp)
    implementation(Dependencies.okHttpLoggingInterceptor)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)
    implementation(Dependencies.retrofitAdapterRxJava2)

    // Permission
    implementation("com.karumi:dexter:6.2.3")

    // Prayer
    implementation("com.github.msarhan:ummalqura-calendar:2.0.2")

    api("com.vuukle.android:ads-mediation:2.1.3")
    api ("com.vuukle.android:ads-mediation-banner:2.1.3")
    api ("com.vuukle.android:ads-rtb:2.1.3")

    // Infomaker
    api(project(":public:core"))
    api(project(":public:live-content"))

    //Glide
    // Image loading
    implementation(Dependencies.glide)
    kapt(Dependencies.glideCompiler)
    implementation("androidx.activity:activity-ktx:1.3.1")


}