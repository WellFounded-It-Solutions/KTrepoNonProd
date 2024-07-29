package se.infomaker.livecontentui.section.configuration

data class ExtraContent(
    val positions: List<Int>,
    val config: Config
) {
    data class Config(
        val propertyMapReference: String,
        val queryParams: Map<String, String>
    )
}