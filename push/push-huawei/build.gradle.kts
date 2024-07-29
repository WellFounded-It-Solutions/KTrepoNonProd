dependencies {
    implementation(Dependencies.coroutinesCore)

    implementation(Dependencies.datastorePreferences)

    implementation("com.huawei.hms:push:6.1.0.300")

    api(project(":public:core"))
}