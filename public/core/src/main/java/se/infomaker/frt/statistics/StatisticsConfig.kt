package se.infomaker.frt.statistics

data class StatisticsConfig(
    val statisticsProviders: List<Map<String, Any>>?,
    val statisticsDisablerBaseUrl: String?
)