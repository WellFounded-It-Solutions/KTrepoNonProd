package se.infomaker.frtutilities

data class ModuleInformation @JvmOverloads constructor(
    val identifier: String? = null,
    val title: String? = null,
    val name: String? = null,
    val promotion: String? = null
)

data class NewModuleInformation @JvmOverloads constructor(
    val identifier: String? = null,
    val title: String? = null,
    val name: String? = null,
    val parent: String? = null,
    val promotion: String? = null
)