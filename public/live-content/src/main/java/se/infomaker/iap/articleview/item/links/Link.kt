package se.infomaker.iap.articleview.item.links

data class Link(val attributes: Map<String, String>) {
    val themePrefix : String
        get() = if (type.startsWith("x-im/")) type.substring("x-im/".length) else type
    val type: String
        get() = attributes["type"] ?: ""
    val uuid: String
        get() = attributes["uuid"] ?: "00000000-0000-0000-0000-000000000000"
}
