package se.infomaker.iap.articleview.item.embed

import org.json.JSONObject
import org.junit.Test
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.TestResourceProvider

class EmbedPreprocessorTest {

    var preprocessor = HtmlEmbedItemPreprocessor()

    companion object {

//        val CONFIG = "{\n" +
//                "            \"baseUrl\": \"https://www.norran.se\",\n" +
//                "            \"aspectRatio\": \"2:1\"\n" +
//                "      }"



//        val CONFIG = "{\n" +
//                        "\"baseUrl\": \"https://www.norran.se\",\n" +
//                "        \"aspectRatio\": \"2:1\",\n" +
//                "        \"embed\": [\n" +
//                "          {\n" +
//                "            \"method\": \"external\"\n" +
//                "          }\n" +
//                "        ]\n" +
//                "      }"



        val CONFIG = "{\n" +
                "            \"baseUrl\": \"https://www.norran.se\",\n" +
                "            \"aspectRatio\": \"2:1\",\n" +
                "           \"embed\": [" +
                "               {\n" +
                "                   \"method\": \"external\",\n" +
                "                   \"title\": \"Externt innehåll: TT karta\",\n" +
                "                   \"pattern\": \"tt-map-\"\n" +
                "               },\n" +
                "               {\n" +
                "                   \"method\": \"internal\",\n" +
                "                   \"pattern\": \"google.com/maps\"\n" +
                "               }\n" +
                "           ]\n" +
                "   }"
    }

    @Test
    fun test1() {

        val contentStructure = ContentStructure(properties = JSONObject(CONFIG))
        val content = preprocessor.process(contentStructure, CONFIG, TestResourceProvider())
        val test = CONFIG
    }
}