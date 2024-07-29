package se.infomaker.livecontentui.config

data class FeatureConfiguration(val permissions: List<Permission>?)

data class Permission(val permission: String, val property: String?, val freeLimit: MeteredAccess?)

data class MeteredAccess(val limit: Int, var unit: String? /*day/month/week/day/hour/minute*/, var interval: Int?)
