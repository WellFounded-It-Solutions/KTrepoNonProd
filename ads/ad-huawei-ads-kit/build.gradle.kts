dependencies {
    implementation(Dependencies.appCompat)
    implementation(Dependencies.coreKtx)

    implementation("com.huawei.hms:ads-prime:3.4.61.300")

    api(project(":public:core"))
    api(project(":public:live-content"))
}