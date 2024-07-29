package se.infomaker.iap.articleview.preprocessor.date

data class DatePreprocessorConfig(var properties: List<String> = listOf(),
                                  var inputFormat: String = "yyyy-MM-dd'T'HH:mm:ssX",
                                  var outputFormat: String? = null, // "yyyy-MM-dd HH:mm",
                                  var id: String = "")