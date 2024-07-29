package com.navigaglobal.mobile.follow.migration.config

data class MigrationFollowConfig(
    val deleteSubscription: DeleteSubscriptionConfig? = null,
    val mutateSubscription: MutateSubscriptionConfig? = null
)

data class DeleteSubscriptionConfig(val properties: Set<String>? = null)
data class MutateSubscriptionConfig(val propertyMap: Map<String, String>? = null)