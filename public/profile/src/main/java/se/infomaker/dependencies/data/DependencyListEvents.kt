package se.infomaker.dependencies.data

sealed class DependencyListEvents {

    data class OpenLicense(val licenseUrl: String) : DependencyListEvents()

}



