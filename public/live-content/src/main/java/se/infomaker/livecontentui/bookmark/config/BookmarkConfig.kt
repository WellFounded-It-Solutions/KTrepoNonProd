package se.infomaker.livecontentui.bookmark.config

import com.google.gson.JsonObject
import se.infomaker.livecontentui.config.AdsConfig

data class BookmarkConfig(
    val bookmarkFeedbackAction: JsonObject? = null,
    val ads: AdsConfig? = null
)