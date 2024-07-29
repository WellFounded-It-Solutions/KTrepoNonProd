package se.infomaker.iap.articleview.transformer

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import se.infomaker.iap.articleview.transformer.NewsMLTransformerManagerTest.Companion.toJSONObject
import se.infomaker.iap.articleview.transformer.newsml.NewsMLTransformer
import se.infomaker.iap.articleview.transformer.newsml.parser.ElementParser
import se.infomaker.iap.articleview.transformer.newsml.parser.GroupParser
import se.infomaker.iap.articleview.transformer.newsml.parser.IdfParser
import se.infomaker.iap.articleview.transformer.newsml.parser.ObjectParser
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)

class NewsMLTransformerTest {
    @Test
    fun transform() {
        val properties = ResourceHelper.createProperties("newsML", "article.xml")
        val elementParser = ElementParser()
        val objectParser = ObjectParser(emptyMap())
        val groupParser = GroupParser(elementParser, objectParser)
        val parser = IdfParser(elementParser, objectParser, groupParser)
        val transformer = NewsMLTransformer(newsMLPropertyKey = "newsML", idfParser = parser)
        val articleStructure = transformer.transform(properties.toJSONObject())
        assertNotNull(articleStructure)
    }
}