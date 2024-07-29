dependencies {
    // AndroidX
    implementation(Dependencies.roomRuntime)
    kapt(Dependencies.roomCompiler)

    // Google
    implementation(Dependencies.gson)

    // RxJava
    implementation(Dependencies.rxJava2)
    implementation(Dependencies.rxRelay2)

    // Square
    implementation(Dependencies.okHttp)
    implementation(Dependencies.okHttpLoggingInterceptor)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterScalars)

    implementation("com.google.android.gms:play-services-analytics:17.0.0")

    api(project(":public:core"))
}