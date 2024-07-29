package se.infomaker.iap.articleview.item.embed

data class EmbedLinkSettings(val baseUrl: String, val data:String?, val linkText:String? = null, val mime:String = "text/html", val encoding:String = "UTF-8")