package se.infomaker.iap.articleview.transformer.newsml

import se.infomaker.iap.articleview.transformer.newsml.parser.ContentPartParser
import se.infomaker.iap.articleview.transformer.newsml.parser.ElementParser
import se.infomaker.iap.articleview.transformer.newsml.parser.GroupParser
import se.infomaker.iap.articleview.transformer.newsml.parser.IdfParser
import se.infomaker.iap.articleview.transformer.newsml.parser.ImageParser
import se.infomaker.iap.articleview.transformer.newsml.parser.ItemParser
import se.infomaker.iap.articleview.transformer.newsml.parser.LinksParser
import se.infomaker.iap.articleview.transformer.newsml.parser.ObjectParser
import se.infomaker.iap.articleview.transformer.newsml.parser.ImageGalleryParser
import se.infomaker.iap.articleview.transformer.newsml.parser.FlowPlayerParser
import se.infomaker.iap.articleview.transformer.newsml.parser.HtmlEmbedParser
import se.infomaker.iap.articleview.transformer.newsml.parser.Screen9Parser
import se.infomaker.iap.articleview.transformer.newsml.parser.YouPlayParser
import se.infomaker.iap.articleview.transformer.newsml.parser.MapParser
import se.infomaker.iap.articleview.transformer.newsml.parser.TableParser

object NewsMLTransformerManager {
    private val parsers = mutableMapOf<String, ItemParser>()

    init {
        registerObjectParser("x-im/table", TableParser())
        registerObjectParser("x-im/mapembed", MapParser())
        registerObjectParser("x-im/imagegallery", ImageGalleryParser())
        registerObjectParser("x-im/image", ImageParser())
        registerObjectParser("x-im/content-part", ContentPartParser(ElementParser()))
        registerObjectParser("x-im/link", LinksParser())
        registerObjectParser("x-im/flowplayer", FlowPlayerParser())
        registerObjectParser("x-im/youplay", YouPlayParser())
        registerObjectParser("x-im/htmlembed", HtmlEmbedParser())
        registerObjectParser("s9-embednode", Screen9Parser())

    }

    fun registerObjectParser(type: String, parser: ItemParser) {
        parsers[type] = parser
    }

    fun createTransformer(newsMLPropertyKey: String): NewsMLTransformer {
        val elementParser = ElementParser()
        val objectParser = ObjectParser(parsers)
        val groupParser = GroupParser(elementParser, objectParser)
        val idfParser = IdfParser(elementParser, objectParser, groupParser)
        return NewsMLTransformer(idfParser, newsMLPropertyKey)
    }
}