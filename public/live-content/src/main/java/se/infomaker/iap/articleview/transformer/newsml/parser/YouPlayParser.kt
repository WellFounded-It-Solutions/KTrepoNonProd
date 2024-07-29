package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser
import se.infomaker.iap.articleview.item.youplay.YouPlayItem

class YouPlayParser : ItemParser {


    override fun parse(parser: XmlPullParser): List<YouPlayItem> {

        val players = mutableListOf<YouPlayItem>()
        var lastCount = 0
        var done = false
        while (!done) {
            val playerAttributes = parser.getAttributes()
            if (playerAttributes["type"] == "x-im/youplay") {
                players.add(YouPlayItem(playerAttributes))
            }
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "title" -> {
                            players[lastCount].title = parser.nextText()
                        }
                        "description" -> {
                            players[lastCount].description = parser.nextText()
                        }
                        "embedCode" -> {
                            players[lastCount].embedCode = parser.nextText()
                        }
                        "width" -> {
                            players[lastCount].width = parser.nextText().toInt()
                        }
                        "height" -> {
                            players[lastCount].height = parser.nextText().toInt()
                        }
                        "minutes" -> {
                            players[lastCount].minutes = parser.nextText()
                        }
                        "seconds" -> {
                            players[lastCount].seconds = parser.nextText()
                        }
                        "link" -> {
                            val attributes = parser.getAttributes()
                            when (attributes["type"]) {
                                "image/jpg" -> {
                                    players[lastCount].thumbnailUrl = attributes["url"] as String
                                }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "object") {
                        done = true
                        lastCount ++
                    }
                }
            }
        }
        return players
    }
}