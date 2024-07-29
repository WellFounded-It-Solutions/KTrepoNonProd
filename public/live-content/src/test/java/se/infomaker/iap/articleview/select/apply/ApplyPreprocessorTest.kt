package se.infomaker.iap.articleview.select.apply

import org.json.JSONObject
import org.junit.Test
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.ContentViewModel
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.TestResourceProvider
import se.infomaker.iap.articleview.item.author.AuthorItem
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.image.ImageItem
import se.infomaker.iap.articleview.item.links.LinksItem
import se.infomaker.iap.articleview.preprocessor.PreprocessorManager
import se.infomaker.iap.articleview.preprocessor.select.apply.ApplyPreprocessor
import se.infomaker.iap.articleview.select.TestingSpannableStringBuilder
import se.infomaker.iap.articleview.select.jsonFormat
import se.infomaker.iap.articleview.select.mapOf
import kotlin.test.assertEquals

class ApplyPreprocessorTest {
    companion object {
        val in_: ContentStructure
            get() {
                return ContentStructure(
                        body = ContentViewModel(
                                items = mutableListOf(
                                        ElementItem("myFirstText", listOf(), mapOf("type" to "headline"), TestingSpannableStringBuilder("This is my first text")),
                                        ImageItem(id = "myImage", type = "x-im/image", uri = "", width = 0, height = 0, text = "", alttext = "", crops = mapOf(), authors = mutableListOf()),
                                        ElementItem("mySecondText", listOf(), mapOf("type" to "body"), TestingSpannableStringBuilder("This is my second text")),
                                        ElementItem("myThirdText", listOf(), mapOf(), TestingSpannableStringBuilder("This is my third text")),
                                        ElementItem("mySecondSecondText", listOf(), mapOf("type" to "body"), TestingSpannableStringBuilder("This is my second second text")),
                                        ImageItem(id = "myImage2", type = "x-im/image", uri = "", width = 0, height = 0, text = "", alttext = "", crops = mapOf(), authors = mutableListOf()),
                                        LinksItem(mapOf("type" to "list"), listOf()),
                                        ElementItem("myFourthText", listOf(), mapOf("type" to "smelly cat"), TestingSpannableStringBuilder("This is my fourth text")),
                                        AuthorItem("myAuthor"),
                                        AuthorItem("mySecondAuthor"))),
                        properties = JSONObject()).copy()
            }

        init {
            PreprocessorManager.registerPreprocessor("delete", RemovePreprocessor())
            PreprocessorManager.registerPreprocessor("addElement", AddElementPreprocessor())
            PreprocessorManager.registerPreprocessor("replace", ReplaceWithElementPreprocessor())
            PreprocessorManager.registerPreprocessor("changeElement", ChangeElementPreprocessor())
            PreprocessorManager.registerPreprocessor("reverse", ReverseListPreprocessor())
        }
    }

    @Test
    fun testApplyMoveItems() {
        val structure = ApplyPreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
            }
            "preprocessors" to listOf(
                    mapOf {
                        "name" to "move"
                        "config" to mapOf {
                            "select" to mapOf {
                                "subset" to "last"
                            }
                            "insert" to mapOf {
                                "method" to "index"
                                "position" to 0
                            }
                        }
                    }
            )

        }.jsonFormat(), TestResourceProvider())

        assertEquals(in_.body.items[1], structure.body.items[5])
        assertEquals(in_.body.items[5], structure.body.items[1])
    }

    @Test
    fun testApplyRemoveItem() {
        val structure = ApplyPreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
            }
            "preprocessors" to listOf(
                    mapOf {
                        "name" to "delete"
                        "config" to mapOf {
                        }
                    }
            )

        }.jsonFormat(), TestResourceProvider())

        assertEquals(8, structure.body.items.size)
        val temp = in_.body.items
        temp.removeAt(1)
        temp.removeAt(5)
        assertEquals(temp, structure.body.items)
    }

    @Test
    fun testApplyAddElement() {
        val structure = ApplyPreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
            }
            "preprocessors" to listOf(
                    mapOf {
                        "name" to "addElement"
                        "config" to mapOf {
                        }
                    }
            )

        }.jsonFormat(), TestResourceProvider())
        assertEquals(11, structure.body.items.size)
        val temp = in_.body.items
        temp.add(AddElementPreprocessor.element)
        assertEquals(temp, structure.body.items)
    }

    @Test
    fun testApplyReplace() {
        val structure = ApplyPreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
            }
            "preprocessors" to listOf(
                    mapOf {
                        "name" to "replace"
                        "config" to mapOf {
                        }
                    }
            )

        }.jsonFormat(), TestResourceProvider())
        val temp = in_.body.items
        temp.removeAt(1)
        temp.add(1, AddElementPreprocessor.element)
        temp.removeAt(5)
        temp.add(5, AddElementPreprocessor.element)
        assertEquals(temp, structure.body.items)
    }

    @Test
    fun testApplyChangeElement() {
        val structure = ApplyPreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "element"
            }
            "preprocessors" to listOf(
                    mapOf {
                        "name" to "changeElement"
                        "config" to mapOf {
                        }
                    }
            )

        }.jsonFormat(), TestResourceProvider())
        assertEquals(ChangeElementPreprocessor.themeKeys, (structure.body.items[0] as ElementItem).themeKeys)
        assertEquals(ChangeElementPreprocessor.themeKeys, (structure.body.items[2] as ElementItem).themeKeys)
        assertEquals(ChangeElementPreprocessor.themeKeys, (structure.body.items[3] as ElementItem).themeKeys)
        assertEquals(ChangeElementPreprocessor.themeKeys, (structure.body.items[4] as ElementItem).themeKeys)
        assertEquals(ChangeElementPreprocessor.themeKeys, (structure.body.items[7] as ElementItem).themeKeys)
    }

    @Test
    fun testApplyReverse() {
        val structure = ApplyPreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "element"
            }
            "preprocessors" to listOf(
                    mapOf {
                        "name" to "reverse"
                        "config" to mapOf {
                        }
                    }
            )

        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[7], structure.body.items[0])
        assertEquals(in_.body.items[4], structure.body.items[2])
        assertEquals(in_.body.items[3], structure.body.items[3])
        assertEquals(in_.body.items[2], structure.body.items[4])
        assertEquals(in_.body.items[0], structure.body.items[7])
    }
}

class RemovePreprocessor : Preprocessor {
    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        content.body.items.clear()
        return content
    }
}

class AddElementPreprocessor : Preprocessor {
    companion object {
        val element: ElementItem
            get() = ElementItem("MyAddedElement", listOf(), mapOf("type" to "added"), TestingSpannableStringBuilder("This is my added text"))
    }

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        content.body.items.add(element)
        return content
    }
}

class ReplaceWithElementPreprocessor : Preprocessor {
    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        (0 until content.body.items.size).forEach {
            content.body.items.removeAt(it)
            content.body.items.add(it, AddElementPreprocessor.element)
        }
        return content
    }
}

class ChangeElementPreprocessor : Preprocessor {
    companion object {
        val themeKeys = listOf("myFirstKey", "mySecondKey")
    }

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        content.body.items.filter { it is ElementItem }.forEach {
            (it as ElementItem).themeKeys = themeKeys
        }
        return content
    }
}

class ReverseListPreprocessor : Preprocessor {
    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        content.body.items.reverse()
        return content
    }
}