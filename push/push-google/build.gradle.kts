dependencies {
    implementation(Dependencies.coroutinesCore)

    implementation(platform(Dependencies.firebaseBom))
    implementation(Dependencies.firebaseMessagingKtx)

    implementation(Dependencies.okHttp)
    implementation(Dependencies.okHttpLoggingInterceptor)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverterGson)

    api(project(":public:core"))
}