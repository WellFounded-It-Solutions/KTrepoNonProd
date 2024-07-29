package com.navigaglobal.mobile.follow.migration.command

import com.navigaglobal.mobile.follow.migration.MigrationCommand
import com.navigaglobal.mobile.follow.migration.MigrationCommandFactory
import com.navigaglobal.mobile.follow.migration.config.DeleteSubscriptionConfig
import se.infomaker.storagemodule.model.Subscription
import timber.log.Timber

class DeleteSubscriptionCommandFactory(
    private val config: DeleteSubscriptionConfig
) : MigrationCommandFactory {

    private val invalidProperties by lazy { config.properties ?: emptySet() }

    override fun create(subscriptions: List<Subscription>): Result<List<MigrationCommand>> {
        return subscriptions.filter { it.containsInvalidProperty }
            .mapNotNull { subscription ->
                val uuid = subscription.uuid
                if (uuid != null) {
                    Timber.d("Creating command to delete subscription ${subscription.name}, " +
                            "contains invalid property: ${subscription.getValue("field")}")
                    MigrationCommand.DeleteSubscriptionCommand(uuid)
                }
                else null
            }
            .let { Result.success(it) }
    }

    private val Subscription.containsInvalidProperty
        get() = getValue("field")?.let { property ->
            invalidProperties.contains(property)
        } ?: true
}
