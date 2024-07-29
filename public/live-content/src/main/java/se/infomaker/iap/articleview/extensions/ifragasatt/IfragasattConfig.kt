package se.infomaker.iap.articleview.extensions.ifragasatt

data class IfragasattConfig(val customerId: Int?,
                            val apiUrl: String?,
                            val baseUrl: String?,
                            val articleIdProperties: List<String>?,
                            val hideCommentsProperty: String?,
                            val imageUrlProvider: ImageUrlProviderConfig,
                            val articlePublishedTime: PublishedTimeConfig?,
                            val queryParameters: Map<String,String>?)

data class PublishedTimeConfig(val property: String?, val inputFormat: String?, val outputFormat: String?)

data class ImageUrlProviderConfig(val provider: String?,
                                  val baseUrl: String?,
                                  val heightProperty: String?,
                                  val cropProperty: String?,
                                  val widthProperty: String?,
                                  val uuidProperty: String?,
                                  val imageProviderProperty:String?)