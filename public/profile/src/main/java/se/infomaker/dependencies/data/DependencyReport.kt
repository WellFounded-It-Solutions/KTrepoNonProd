package se.infomaker.dependencies.data

data class DependencyReport(var dependencies: List<Dependency>)

data class Dependency(
    val moduleName: String?, val moduleUrl: String?, val moduleVersion: String?,
    val moduleLicense: String?, val moduleLicenseUrl: String?,
    val artifactName: String = moduleName?.split(DELIMITER)?.lastOrNull() ?: EMPTY_STRING,
    val groupName: String = moduleName?.split(DELIMITER)?.firstOrNull() ?: EMPTY_STRING,
) {
    companion object {
        const val EMPTY_STRING: String = "N/A"
        const val DELIMITER = ":"
    }

    val license: License? = processLicense(moduleLicense, moduleLicenseUrl)

    private fun processLicense(license: String?, url: String?): License? {
        if (!license.isNullOrEmpty() && !url.isNullOrEmpty()) {
            return License(license, url)
        }
        return null
    }

    fun hasLicense(): Boolean = license != null

    fun getVersion(): String = moduleVersion ?: EMPTY_STRING

    data class License(val name: String, val url: String)
}

data class DependencyReportDTO(var dependencies: List<DependencyDTO>) {
    fun toDomain(): DependencyReport = DependencyReport(dependencies.map {
        Dependency(
            it.moduleName,
            it.moduleUrl,
            it.moduleVersion,
            it.moduleLicense,
            it.moduleLicenseUrl
        )
    })
}

data class DependencyDTO(
    val moduleName: String?, val moduleUrl: String?, val moduleVersion: String?,
    val moduleLicense: String?, val moduleLicenseUrl: String?
)