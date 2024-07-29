dependencies {
    implementation(Dependencies.annotation)

    implementation("com.google.android.gms:play-services-tagmanager:17.0.1")
    implementation("com.google.android.gms:play-services-tagmanager-v4-impl:17.0.1") // TODO Remove this at a later date.

    api(project(":public:core"))
    api(project(":statistics:statistics-firebase-analytics"))
}