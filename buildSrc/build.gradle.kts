plugins {
    `kotlin-dsl`
}

repositories {
    google()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("com.android.tools.build:gradle:7.3.0")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.43.2")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.21.0")
    implementation("com.google.gms:google-services:4.3.13")
    implementation("com.google.firebase:firebase-crashlytics-gradle:2.9.1")
}