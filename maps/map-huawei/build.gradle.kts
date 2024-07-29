dependencies {
    implementation(Dependencies.appCompat)
    implementation(Dependencies.coreKtx)

    implementation("com.huawei.hms:maps:6.2.0.301")

    api(project(":public:core"))
    api(project(":public:live-content"))
}