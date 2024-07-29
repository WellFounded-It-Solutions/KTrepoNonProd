plugins {
    id("com.dipien.byebyejetifier") version "1.2.0"
    id("org.barfuin.gradle.taskinfo") version "1.3.1"
}

byeByeJetifier {
    excludedFilesFromScanning += listOf(
        // com.squareup.leakcanary:com.squareup.curtains:curtains:1.0.1
        "curtains/internal/WindowCallbackWrapper"
    )
}

buildscript {
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://developer.huawei.com/repo/") }
        jcenter()
    }
    dependencies {
        classpath(Dependencies.kotlinGradlePlugin)
        classpath(Dependencies.androidGradlePlugin)
        classpath(Dependencies.hiltGradlePlugin)
        classpath(Dependencies.googleServicesGradlePlugin)
        classpath(Dependencies.agConnectGradlePlugin)
        classpath(Dependencies.realmGradlePlugin)
        classpath(Dependencies.crashlyticsGradlePlugin)

    }
}

allprojects {
    repositories {
        maven { url = uri("https://artifacts.netcore.co.in/artifactory/android") }
        google()
        maven { setUrl("https://developer.huawei.com/repo/") }
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        jcenter()

        maven { setUrl("https://packagecloud.io/smartadserver/android/maven2")}
        maven { setUrl ( "https://europe-maven.pkg.dev/jxbrowser/releases/") }
        maven { setUrl ( "https://taboolapublic.jfrog.io/artifactory/mobile-release/") }
        maven { setUrl ( "https://instreamatic.com/nexus/repository/maven-public") }

    }
}

subprojects {
    if (project.isDevelopmentApp) {
        apply(plugin = "iap-development-app")
    }
    else if (project.isArtifact && !project.isBom) {
        apply(plugin = "iap-artifact")
    }
}

subprojects {
    if (project.isArtifact) {
        afterEvaluate {
            tasks.findByName("publishToMavenLocal")?.let { publishToMavenLocalTask ->
                val projectDependencies = project.configurations.named("api").dependencies.withType<ProjectDependency>()
                projectDependencies.forEach {
                    publishToMavenLocalTask.dependsOn(it.dependencyProject.tasks.named("publishToMavenLocal"))
                }
            }
        }
    }


}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}