android {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeCompiler
    }
}

dependencies {
    implementation(Dependencies.constraintLayout)
    implementation(Dependencies.androidxBrowser)
    implementation(Dependencies.activityKtx)
    implementation(Dependencies.fragmentKtx)

    implementation(Dependencies.material)
    implementation(Dependencies.gson)

    implementation(Dependencies.coroutinesAndroid)
    implementation(Dependencies.coroutinesRxJava2)

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.accompanist:accompanist-appcompat-theme:0.23.1")
    implementation("androidx.compose.runtime:runtime:${Versions.compose}")
    implementation("androidx.compose.runtime:runtime-livedata:${Versions.compose}")
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
    implementation("androidx.compose.material:material:${Versions.compose}")
    implementation("androidx.compose.foundation:foundation:${Versions.compose}")
    implementation("androidx.compose.compiler:compiler:${Versions.composeCompiler}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    implementation("androidx.activity:activity-compose:${Versions.activity}")

    api(project(":public:core"))
}