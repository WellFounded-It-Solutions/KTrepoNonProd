package se.infomaker.iap.articleview.item

import org.json.JSONObject
import org.junit.Test
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.ContentViewModel
import se.infomaker.iap.articleview.TestResourceProvider
import se.infomaker.iap.articleview.item.author.AuthorItem
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.image.ImageItem
import se.infomaker.iap.articleview.item.links.LinksItem
import se.infomaker.iap.articleview.preprocessor.propertyelement.PropertyElementPreprocessor
import se.infomaker.iap.articleview.select.TestingSpannableStringBuilder

class PropertyElementPreprocessorTest {

    var preprocessor = PropertyElementPreprocessor()

    companion object {

        val CONFIG = "{\n" +
                "           \"properties\": [" +
                "               {\n" +
                "                   \"property\": \"teaserHeadline\",\n" +
                "                   \"type\": \"articleTeaserHeadline\"" +
                "               },\n" +
                "               {\n" +
                "                   \"property\": \"categories.name\",\n" +
                "                   \"type\": \"category\"" +
                "               },\n" +
                "           ]\n" +
                "   }"
    }

    @Test
    fun test1() {
        val contentStructure = ContentStructure(body = ContentViewModel(
                items = mutableListOf(
                        ElementItem("myFirstText", listOf(), mapOf("type" to "ZZTOP"), TestingSpannableStringBuilder("This is my first text")),
                        ImageItem("myImage", "x-im/image", "", 0, 0, "", "", mapOf(), mutableListOf()),
                        ElementItem("mySecondText", listOf(), mapOf("type" to "body"), TestingSpannableStringBuilder("This is my second text")),
                        ElementItem("myThirdText", listOf(), mapOf(), TestingSpannableStringBuilder("This is my third text")),
                        ElementItem("mySecondSecondText", listOf(), mapOf("type" to "body"), TestingSpannableStringBuilder("This is my second second text")),
                        ImageItem("myImage2", "x-im/image", "", 0, 0, "", "", mapOf(), mutableListOf()),
                        LinksItem(mapOf("type" to "list"), listOf()),
                        ElementItem("myFourthText", listOf(), mapOf("type" to "smelly cat"), TestingSpannableStringBuilder("This is my fourth text")),
                        AuthorItem("myAuthor"),
                        ElementItem("dateline", listOf(), mapOf("type" to "dateline"), TestingSpannableStringBuilder("Göte'la'borg")),
                        AuthorItem("mySecondAuthor"))), properties = JSONObject(CONFIG))
        val content = preprocessor.process(contentStructure, CONFIG, TestResourceProvider())
        val test = CONFIG
    }
}