package se.infomaker.dependencies.data

data class DependencyListState(
    val isLoading: Boolean = false,
    val dependencies: List<Dependency> = emptyList(),
    val showLicenses: Boolean = false,
    val headerText: String? = null
)