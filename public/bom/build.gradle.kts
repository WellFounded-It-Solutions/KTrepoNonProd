plugins {
    `java-platform`
    id("iap-publishing")
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            from(components["javaPlatform"])

            groupId = project.requireProperty("GROUP")
            version = project.requireProperty("VERSION_NAME")

            pom {
                name.set("bom")
            }
        }
    }
}

dependencies.constraints {
    rootProject.subprojects.forEach { subproject ->
        if (subproject.isArtifact && !subproject.isBom) {
            api(subproject)
        }
    }
}