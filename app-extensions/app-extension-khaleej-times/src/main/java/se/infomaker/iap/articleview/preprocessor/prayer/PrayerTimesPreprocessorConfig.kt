package se.infomaker.iap.articleview.preprocessor.prayer


data class Location(val defaultSelected: Boolean?, val name: String, val offsetInMinutes: Int)

data class PrayerTimesPreprocessorConfig(var locations: List<Location>)