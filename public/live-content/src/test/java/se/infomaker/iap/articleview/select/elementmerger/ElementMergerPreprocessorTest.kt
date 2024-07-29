package se.infomaker.iap.articleview.select.elementmerger

import org.json.JSONObject
import org.junit.Test
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.ContentViewModel
import se.infomaker.iap.articleview.TestResourceProvider
import se.infomaker.iap.articleview.item.author.AuthorItem
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.image.ImageItem
import se.infomaker.iap.articleview.item.links.LinksItem
import se.infomaker.iap.articleview.item.mergedelement.ElementMergerPreprocessor
import se.infomaker.iap.articleview.item.mergedelement.ElementMergerPreprocessorConfig
import se.infomaker.iap.articleview.item.mergedelement.MergedElementItem
import se.infomaker.iap.articleview.preprocessor.select.move.Insert
import se.infomaker.iap.articleview.preprocessor.select.move.InsertIndexConfig
import se.infomaker.iap.articleview.preprocessor.select.move.InsertRelativeConfig
import se.infomaker.iap.articleview.select.TestingSpannableStringBuilder
import se.infomaker.iap.articleview.select.jsonFormat
import se.infomaker.iap.articleview.select.mapOf
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElementMergerPreprocessorTest {
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
                                        ElementItem("dateline", listOf(), mapOf("type" to "dateline"), TestingSpannableStringBuilder("Göte'la'borg")),
                                        AuthorItem("mySecondAuthor"))),
                        properties = JSONObject()).copy()
            }
    }

    @Test
    fun testMergeTwoDistinct() {
        val structure = ElementMergerPreprocessor().process(in_, mapOf {
            "selectors" to listOf(
                    mapOf {
                        "matching" to mapOf {
                            "type" to "dateline"
                        }
                    },
                    mapOf {
                        "subset" to "first"
                        "matching" to mapOf {
                            "type" to "body"
                        }
                    }
            )
            "insert" to mapOf {
                "method" to "index"
                "position" to 0
            }
            "separator" to " | "
        }.jsonFormat(), TestResourceProvider())
        val temp = in_.body.items
        val mergedElementItem = MergedElementItem(listOf(temp[9] as ElementItem, temp[2] as ElementItem), " | ")
        temp.removeAt(9)
        temp.removeAt(2)
        temp.add(0, mergedElementItem)
        assertEquals(temp, structure.body.items)
    }

    @Test
    fun testMergeThreeDistinct() {
        val structure = ElementMergerPreprocessor().process(in_, mapOf {
            "selectors" to listOf(
                    mapOf {
                        "matching" to mapOf {
                            "type" to "dateline"
                        }
                    },
                    mapOf {
                        "subset" to "first"
                        "matching" to mapOf {
                            "type" to "body"
                        }
                    },
                    mapOf {
                        "subset" to "first"
                        "matching" to mapOf {
                            "type" to "headline"
                        }
                    }
            )
            "insert" to mapOf {
                "method" to "index"
                "position" to 0
            }
            "separator" to " | "
        }.jsonFormat(), TestResourceProvider())
        val temp = in_.body.items
        val mergedElementItem = MergedElementItem(listOf(temp[9] as ElementItem, temp[2] as ElementItem, temp[0] as ElementItem), " | ")
        temp.removeAt(9)
        temp.removeAt(2)
        temp.removeAt(0)
        temp.add(0, mergedElementItem)
        assertEquals(temp, structure.body.items)
    }

    @Test
    fun testMergeMultipleInOneSelector() {
        val structure = ElementMergerPreprocessor().process(in_, mapOf {
            "selectors" to listOf(
                    mapOf {
                        "matching" to mapOf {
                            "type" to "body"
                        }
                    }
            )
            "insert" to mapOf {
                "method" to "index"
                "position" to 0
            }
            "separator" to " - "
        }.jsonFormat(), TestResourceProvider())
        val temp = in_.body.items
        val mergedElementItem = MergedElementItem(listOf(temp[2] as ElementItem, temp[4] as ElementItem), " - ")
        temp.removeAt(4)
        temp.removeAt(2)
        temp.add(0, mergedElementItem)
        assertEquals(temp, structure.body.items)
    }

    @Test
    fun testParseConfig() {
        val configJson = mapOf {
            "selectors" to listOf(
                    mapOf {
                        "matching" to mapOf {
                            "type" to "dateline"
                        }
                    },
                    mapOf {
                        "subset" to "first"
                        "matching" to mapOf {
                            "type" to "body"
                        }
                    }
            )
            "insert" to mapOf {
                "method" to "index"
                "position" to 10
                "fallback" to mapOf {
                    "method" to "relative"
                    "position" to "after"
                    "select" to mapOf {
                        "subset" to "first"
                    }
                }
            }
            "separator" to " | "
        }.jsonFormat()
        val gson = Insert.registerInsertTypeAdapter().create()
        val config = gson.fromJson(configJson, ElementMergerPreprocessorConfig::class.java)
        assertEquals("dateline", config.selectors[0].matching["type"])
        assertEquals("body", config.selectors[1].matching["type"])
        assertEquals("first", config.selectors[1].subset)
        assertEquals(" | ", config.separator)
        assertEquals(10, (config.insert as InsertIndexConfig).position)
        assertTrue { (config.insert.fallback is InsertRelativeConfig) }
    }

    @Test
    fun testSelectSameMultipleTimes() {
        val structure = ElementMergerPreprocessor().process(in_, mapOf {
            "selectors" to listOf(
                    mapOf {
                        "subset" to "first"
                        "matching" to mapOf {
                            "type" to "body"
                        }
                    },
                    mapOf {
                        "subset" to "2"
                    }
            )
            "insert" to mapOf {
                "method" to "index"
                "position" to 0
            }
            "separator" to " | "
        }.jsonFormat(), TestResourceProvider())
        val temp = in_.body.items
        val mergedElementItem = MergedElementItem(listOf(temp[2] as ElementItem), " | ")
        temp.removeAt(2)
        temp.add(0, mergedElementItem)
        assertEquals(temp, structure.body.items)
    }

    @Test
    fun testMergeOnlyOneItem() {
        val structure = ElementMergerPreprocessor().process(in_, mapOf {
            "selectors" to listOf(
                    mapOf {
                        "subset" to "first"
                        "matching" to mapOf {
                            "type" to "body"
                        }
                    }
            )
            "insert" to mapOf {
                "method" to "index"
                "position" to 0
            }
            "separator" to " | "
        }.jsonFormat(), TestResourceProvider())
        val temp = in_.body.items
        val mergedElementItem = MergedElementItem(listOf(temp[2] as ElementItem), " | ")
        temp.removeAt(2)
        temp.add(0, mergedElementItem)
        assertEquals(temp, structure.body.items)
    }

    @Test
    fun testNoSeparator() {
        val configJson = mapOf {
            "selectors" to listOf(
                    mapOf {
                        "matching" to mapOf {
                            "type" to "dateline"
                        }
                    },
                    mapOf {
                        "subset" to "first"
                        "matching" to mapOf {
                            "type" to "body"
                        }
                    }
            )
            "insert" to mapOf {
                "method" to "index"
                "position" to 10
                "fallback" to mapOf {
                    "method" to "relative"
                    "position" to "after"
                    "select" to mapOf {
                        "subset" to "first"
                    }
                }
            }
        }.jsonFormat()
        val gson = Insert.registerInsertTypeAdapter().create()
        val config = gson.fromJson(configJson, ElementMergerPreprocessorConfig::class.java)
        assertEquals("dateline", config.selectors[0].matching["type"])
        assertEquals("body", config.selectors[1].matching["type"])
        assertEquals("first", config.selectors[1].subset)
        assertEquals(" ", config.separator)
        assertEquals(10, (config.insert as InsertIndexConfig).position)
        assertTrue { (config.insert.fallback is InsertRelativeConfig) }
    }
}