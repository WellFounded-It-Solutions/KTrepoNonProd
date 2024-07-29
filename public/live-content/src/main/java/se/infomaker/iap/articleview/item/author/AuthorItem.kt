package se.infomaker.iap.articleview.item.author

import se.infomaker.iap.articleview.item.Item

/*
<link rel="author" title="Google Images" type="x-im/author" id="00000000-0000-0000-0000-000000000000"/>
 */

data class AuthorItem(val id: String, val fields: List<Field> = mutableListOf(), val imagePath: String? = null) : Item(id) {
    override val typeIdentifier = AuthorItem::class.java
    val themeKey = "author"

    var imageStyle = ImageStyle.CIRCLE
    var following = false
    override val selectorType = "author"

    override val matchingQuery = mapOf<String, String>()

    companion object {
        val NO_AUTHOR = AuthorItem("NO_AUTHOR")

        enum class ImageStyle {
            ROUNDED_CORNER,
            SQUARE,
            CIRCLE,
        }

        fun getAsImageStyle(style: String? = "circle"): ImageStyle = when (style) {
            "square" -> ImageStyle.SQUARE
            "roundedCorners" -> ImageStyle.ROUNDED_CORNER
            "circle" -> ImageStyle.CIRCLE
            else -> ImageStyle.CIRCLE
        }
    }

    data class Field(val content: String, val identifier: String? = null, val type: FieldType = FieldType.normal)
    enum class FieldType {
        normal,
        email,
        phone,
        map
    }
}