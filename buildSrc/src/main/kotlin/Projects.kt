import org.gradle.api.Project

val Project.isArtifact: Boolean
    get() = project.parent?.let {
        it.name != "internal" && it.name != "apps" && it.name != rootProject.name
    } ?: false

val Project.isBom: Boolean
    get() = project.name == "bom"

val Project.isDevelopmentApp: Boolean
    get() = project.parent?.name == "apps"



fun Project.requireProperty(propertyName: String): String {
    val value = findProperty(propertyName)?.toString()
    return requireNotNull(value) { "Please define \"$propertyName\" in your gradle.properties file" }
}