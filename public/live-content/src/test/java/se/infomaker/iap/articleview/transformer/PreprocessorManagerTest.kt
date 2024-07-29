package se.infomaker.iap.articleview.transformer

import com.google.gson.JsonObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.TestResourceProvider
import se.infomaker.iap.articleview.preprocessor.PreprocessorConfig
import se.infomaker.iap.articleview.preprocessor.PreprocessorManager
import se.infomaker.iap.articleview.transformer.NewsMLTransformerManagerTest.Companion.toJSONObject
import se.infomaker.iap.articleview.transformer.newsml.NewsMLTransformer
import se.infomaker.iap.articleview.transformer.newsml.parser.ElementParser
import se.infomaker.iap.articleview.transformer.newsml.parser.GroupParser
import se.infomaker.iap.articleview.transformer.newsml.parser.IdfParser
import se.infomaker.iap.articleview.transformer.newsml.parser.ObjectParser
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class PreprocessorManagerTest {

    @Test
    fun useCustomPreprocessor() {
        PreprocessorManager.registerPreprocessor("test", object: Preprocessor {
            override fun process(content: ContentStructure, config : String, resourceProvider: ResourceProvider) : ContentStructure {
                return ContentStructure(properties = content.properties)
            }
        })

        val properties = ResourceHelper.createProperties("newsML", "article.xml")
        val elementParser = ElementParser()
        val objectParser = ObjectParser(emptyMap())
        val groupParser = GroupParser(elementParser, objectParser)
        val parser = IdfParser(elementParser, objectParser, groupParser)
        val transformer = NewsMLTransformer(newsMLPropertyKey = "newsML", idfParser = parser)
        val articleStructure = transformer.transform(properties.toJSONObject())
        Assert.assertTrue(articleStructure.body.items.size > 0)
        val preprocessed = PreprocessorManager.preprocess(articleStructure, listOf(PreprocessorConfig("test", JsonObject())), TestResourceProvider())
        assertEquals(0, preprocessed.body.items.size)
    }
}