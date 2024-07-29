plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = Versions.compileSdk

    defaultConfig {
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        versionCode = 10000
        versionName = "1.0.0-development"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    add("coreLibraryDesugaring", Dependencies.java8DesugarLibs)

    implementation(Dependencies.kotlinStdlib)

    implementation(Dependencies.hilt)
    kapt(Dependencies.hiltCompiler)

    implementation(Dependencies.appCompat)
    implementation(Dependencies.coreKtx)

    implementation(Dependencies.gson)
    implementation(Dependencies.material)

    implementation(Dependencies.okHttp)
    implementation(Dependencies.okHttpLoggingInterceptor)

    implementation(Dependencies.glide)
    kapt(Dependencies.glideCompiler)

    implementation(Dependencies.timber)

   // implementation(project(":internal:development-resources"))
}