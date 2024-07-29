package se.infomaker.frtutilities.mainmenutoolbarsettings

data class TopAppBarConfig(
    val visibility: String?,
    val logo: Logo?,
    val title: Title?,
    val items: List<ToolbarConfig.ButtonConfig>?
) {

    fun asToolbarConfig(): ToolbarConfig {
        val logoPosition = if (logo == null) "none" else logo.position ?: "center"
        return ToolbarConfig.builder()
            .visibility(visibility)
            .logoResource(logo?.icon)
            .logoPosition(logoPosition)
            .titlePosition(title?.position)
            .buttons(items)
            .build()
    }

    data class Logo(val icon: String?, val position: String?)

    data class Title(val text: String?, val position: String?)
}