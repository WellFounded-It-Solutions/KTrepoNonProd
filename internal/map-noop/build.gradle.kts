plugins {
    id("iap-library")
}

android {
    buildFeatures {
        viewBinding = false
    }
}

dependencies {
    api(project(":public:live-content"))
}