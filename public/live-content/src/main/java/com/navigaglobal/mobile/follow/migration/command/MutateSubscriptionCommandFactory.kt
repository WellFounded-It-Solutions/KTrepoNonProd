package com.navigaglobal.mobile.follow.migration.command

import com.navigaglobal.mobile.follow.migration.MigrationCommand
import com.navigaglobal.mobile.follow.migration.MigrationCommandFactory
import com.navigaglobal.mobile.follow.migration.config.MutateSubscriptionConfig
import se.infomaker.storagemodule.model.Subscription
import timber.log.Timber

class MutateSubscriptionCommandFactory(
    private val config: MutateSubscriptionConfig
) : MigrationCommandFactory {

    private val migrationPropertyMap by lazy { config.propertyMap ?: emptyMap() }

    override fun create(subscriptions: List<Subscription>): Result<List<MigrationCommand>> {
        return subscriptions.mapNotNull { subscription ->
            val uuid = subscription.uuid
            val migrationProperty = subscription.migrationProperty
            if (uuid != null && migrationProperty != null) {
                Timber.d("Creating command to mutate subscription ${subscription.name} " +
                        "from ${subscription.getValue("field")} to $migrationProperty")
                MigrationCommand.MutateSubscriptionCommand(uuid, mapOf("field" to migrationProperty))
            }
            else null
        }.let {
            Result.success(it)
        }
    }

    private val Subscription.migrationProperty
        get() = getValue("field")?.let { property ->
            migrationPropertyMap[property]
        }
}