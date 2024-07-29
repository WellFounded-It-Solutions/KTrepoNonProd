package se.infomaker.iap.articleview.select.move

import org.json.JSONObject
import org.junit.Test
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.ContentViewModel
import se.infomaker.iap.articleview.TestResourceProvider
import se.infomaker.iap.articleview.item.author.AuthorItem
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.image.ImageItem
import se.infomaker.iap.articleview.item.links.LinksItem
import se.infomaker.iap.articleview.preprocessor.select.move.MovePreprocessor
import se.infomaker.iap.articleview.select.TestingSpannableStringBuilder
import se.infomaker.iap.articleview.select.jsonFormat
import se.infomaker.iap.articleview.select.mapOf
import kotlin.test.assertEquals

class MovePreprocessorTest {
    companion object {
        val in_: ContentStructure
            get() {
                return ContentStructure(
                        body = ContentViewModel(
                                items = mutableListOf(
                                        ElementItem("myFirstText", listOf(), mapOf("type" to "headline"), TestingSpannableStringBuilder("This is my first text")),
                                        ImageItem("myImage", "x-im/image", "", 0, 0, "", "", mapOf(), mutableListOf()),
                                        ElementItem("mySecondText", listOf(), mapOf("type" to "body"), TestingSpannableStringBuilder("This is my second text")),
                                        ElementItem("myThirdText", listOf(), mapOf(), TestingSpannableStringBuilder("This is my third text")),
                                        ElementItem("mySecondSecondText", listOf(), mapOf("type" to "body"), TestingSpannableStringBuilder("This is my second second text")),
                                        ImageItem("myImage2", "x-im/image", "", 0, 0, "", "", mapOf(), mutableListOf()),
                                        LinksItem(mapOf("type" to "list"), listOf()),
                                        ElementItem("myFourthText", listOf(), mapOf("type" to "smelly cat"), TestingSpannableStringBuilder("This is my fourth text")),
                                        AuthorItem("myAuthor"),
                                        AuthorItem("mySecondAuthor"))),
                        properties = JSONObject()).copy()
            }
    }

    @Test
    fun testMoveSingleIndex() {
        var structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
                "subset" to "first"
            }
            "insert" to mapOf {
                "method" to "index"
                "position" to 0
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[1], structure.body.items[0])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
                "subset" to "first"
            }
            "insert" to mapOf {
                "method" to "index"
                "position" to 5
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[1], structure.body.items[5])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
                "subset" to "last"
            }
            "insert" to mapOf {
                "method" to "index"
                "position" to 8
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[5], structure.body.items[8])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "author"
                "subset" to "first"
            }
            "insert" to mapOf {
                "method" to "index"
                "position" to 0
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[8], structure.body.items[0])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "author"
                "subset" to "first"
            }
            "insert" to mapOf {
                "method" to "index"
                "position" to 10 //Just outside range, list will not update
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items, structure.body.items)
    }

    @Test
    fun testMoveSingleRelative() {
        var structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
                "subset" to "last"
            }
            "insert" to mapOf {
                "method" to "relative"
                "position" to "after"
                "select" to mapOf {
                    "subset" to "first"
                }
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[5], structure.body.items[1])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "element"
                "subset" to "1"
            }
            "insert" to mapOf {
                "method" to "relative"
                "position" to "after"
                "select" to mapOf {
                    "type" to "element"
                    "subset" to "last"
                    "matching" to mapOf {
                        "type" to "body"
                    }
                }
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[2], structure.body.items[4])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "author"
                "subset" to "first"
            }
            "insert" to mapOf {
                "method" to "relative"
                "position" to "before"
                "select" to mapOf {
                    "type" to "element"
                    "subset" to "first"
                    "matching" to mapOf {
                        "type" to "body"
                    }
                }
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[8], structure.body.items[2])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "author"
                "subset" to "first"
            }
            "insert" to mapOf {
                "method" to "relative"
                "position" to "efter" //Note, this is wrong, defaulting to before
                "select" to mapOf {
                    "type" to "element"
                    "subset" to "first"
                    "matching" to mapOf {
                        "type" to "body"
                    }
                }
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[8], structure.body.items[2])
    }

    @Test
    fun testMoveMultiple() {
        var structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "author"
            }
            "insert" to mapOf {
                "method" to "relative"
                "select" to mapOf {
                    "type" to "element"
                    "subset" to "first"
                    "matching" to mapOf {
                        "type" to "body"
                    }
                }
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[8], structure.body.items[2])
        assertEquals(in_.body.items[9], structure.body.items[3])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "element"
                "matching" to mapOf {
                    "type" to "body"
                }
            }
            "insert" to mapOf {
                "method" to "relative"
                "select" to mapOf {
                    "type" to "element"
                    "subset" to "first"
                }
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[2], structure.body.items[0])
        assertEquals(in_.body.items[4], structure.body.items[1])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "element"
            }
            "insert" to mapOf {
                "method" to "index"
                "position" to 4
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[0], structure.body.items[4])
        assertEquals(in_.body.items[2], structure.body.items[5])
        assertEquals(in_.body.items[3], structure.body.items[6])
        assertEquals(in_.body.items[4], structure.body.items[7])
        assertEquals(in_.body.items[7], structure.body.items[8])
    }

    @Test
    fun testFallback() {
        var structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "author"
            }
            "insert" to mapOf {
                "method" to "index"
                "position" to 40
                "fallback" to mapOf {
                    "method" to "index"
                    "position" to 0
                }
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[8], structure.body.items[0])
        assertEquals(in_.body.items[9], structure.body.items[1])

        structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
            }
            "insert" to mapOf {
                "method" to "relative"
                "select" to mapOf {
                    "type" to "thisTypeDoesNotExist!!!"
                }
                "fallback" to mapOf {
                    "method" to "relative"
                    "position" to "after"
                    "select" to mapOf {
                        "subset" to "last"
                    }
                }
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[1], structure.body.items[8])
        assertEquals(in_.body.items[5], structure.body.items[9])
    }

    @Test
    fun fallbackInFallback() {
        val structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "image"
            }
            "insert" to mapOf {
                "method" to "relative"
                "select" to mapOf {
                    "type" to "thisTypeDoesNotExist!!!"
                }
                "fallback" to mapOf {
                    "method" to "index"
                    "position" to 15
                    "fallback" to mapOf {
                        "method" to "relative"
                        "position" to "before"
                        "select" to mapOf {
                        }
                    }
                }
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items[1], structure.body.items[0])
        assertEquals(in_.body.items[5], structure.body.items[1])
    }

    @Test(expected = Exception::class)
    fun testInvalidConfig() {
        var structure = MovePreprocessor().process(in_, mapOf {
            "select" to mapOf {
                "type" to "author"
            }
            "insert" to mapOf {
                "method" to "index"
                "position" to "a"
            }
        }.jsonFormat(), TestResourceProvider())
        assertEquals(in_.body.items, structure.body.items)
    }
}