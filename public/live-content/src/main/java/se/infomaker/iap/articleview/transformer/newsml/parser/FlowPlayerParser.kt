package se.infomaker.iap.articleview.transformer.newsml.parser

import org.xmlpull.v1.XmlPullParser

import se.infomaker.iap.articleview.item.flowplayer.FlowPlayerItem

class FlowPlayerParser : ItemParser {

    override fun parse(parser: XmlPullParser): List<FlowPlayerItem> {

        val players = mutableListOf<FlowPlayerItem>()
        var lastCount = 0
        var done = false
        while (!done) {
            val playerAttributes = parser.getAttributes()
            if (playerAttributes["type"] == "x-im/flowplayer") {
                players.add(FlowPlayerItem(playerAttributes))
            }
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "title" -> {
                            players[lastCount].title = parser.nextText()
                        }
                        "embedCode" -> {
                            players[lastCount].embedCode = parser.nextText()
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