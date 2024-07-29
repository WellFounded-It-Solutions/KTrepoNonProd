include(

    ":internal:development-resources",
    ":internal:map-noop",
    ":internal:push-noop",
    ":ads:ad-google-mobile-ads",
    ":ads:ad-huawei-ads-kit",
    ":app-extensions:app-extension-khaleej-times",
    ":consent:consent-funding-choices",
    ":maps:map-google",
    ":maps:map-huawei",
    ":push:push-google",
    ":push:push-huawei",
    ":statistics:statistics-firebase-analytics",
    ":statistics:statistics-google-analytics",
    ":statistics:statistics-google-tag-manager",
    ":statistics:statistics-huawei-analytics-kit",
    ":public:bom",
    ":public:core",
    ":public:live-content",
    ":public:profile"
    )

pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()

        gradlePluginPortal()
        google()

        maven {
            // Taboola:
            url = uri("https://taboolapublic.jfrog.io/artifactory/mobile-release")
        }

    }
}

include(":public:ktshows")
