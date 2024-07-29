dependencies {
    implementation(Dependencies.appCompat)
    implementation(Dependencies.coreKtx)

    implementation(Dependencies.gmsAds)

    api(project(":public:core"))
    api(project(":public:live-content"))
}