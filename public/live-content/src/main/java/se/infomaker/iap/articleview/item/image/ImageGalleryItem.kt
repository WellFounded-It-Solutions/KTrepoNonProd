package se.infomaker.iap.articleview.item.image

import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.item.element.ElementItem
import java.util.UUID

data class ImageGalleryItem(
    val id: String,
    val type: String,
    val text: String,
    val images: List<ImageItem>,
    val textElement: ElementItem? = null
) : Item(id) {
    override val typeIdentifier = ImageGalleryItem::class.java
    val themeKey = type

    override val selectorType = "imagegallery"
    override val matchingQuery = mapOf<String, String>()

    companion object {
        fun builder(init: Builder.() -> Unit) = Builder(init)
    }

    class Builder() {
        constructor(init: Builder.() -> Unit) : this() {
            init()
        }

        var type: String = "default"
        var uuid: String = UUID.randomUUID().toString()
        var text: String = ""
        var images = mutableListOf<ImageItem?>()
        var textElement: ElementItem? = null

        fun build(): ImageGalleryItem? {
            val images = images.filterNotNull()
            if (images.isNotEmpty()) {
                return ImageGalleryItem(
                    id = uuid,
                    type = type,
                    text = text,
                    images = images,
                    textElement = textElement
                )
            }
            return null
        }
    }
}