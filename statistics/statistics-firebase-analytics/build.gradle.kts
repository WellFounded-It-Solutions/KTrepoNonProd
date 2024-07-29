dependencies {
    testImplementation("junit:junit:4.13.2")

    implementation(Dependencies.gson)

    implementation(platform(Dependencies.firebaseBom))
    implementation(Dependencies.firebaseAnalyticsKtx)

    api(project(":public:core"))

    implementation("com.netcore.android:smartech-sdk:3.4.3")
    implementation("com.netcore.android:smartech-nudges:10.1.2")
}