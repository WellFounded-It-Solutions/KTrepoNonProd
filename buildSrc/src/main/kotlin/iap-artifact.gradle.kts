import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    id("iap-library")
    id("iap-publishing")
    id("com.vanniktech.maven.publish.base")
}

mavenPublishing {
    group = project.requireProperty("GROUP")
    version = project.requireProperty("VERSION_NAME")

    pomFromGradleProperties() // Nothing yet, but maybe... Someday... 

    configure(AndroidSingleVariantLibrary(
        publishJavadocJar = false
    ))
}