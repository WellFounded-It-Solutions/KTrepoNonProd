plugins {
    id("iap-library")
}

dependencies {
    implementation(Dependencies.appCompat)

    implementation(Dependencies.material)

    api(project(":public:core"))
}