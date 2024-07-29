package com.navigaglobal.mobile.follow.migration

import com.navigaglobal.mobile.migration.Migration
import se.infomaker.storagemodule.Storage

class FollowMigration(
    private val commandFactories: Set<@JvmSuppressWildcards MigrationCommandFactory>
) : Migration {

    override val identifier = IDENTIFIER

    override suspend fun migrate(): Result<Unit> {
        return try {
            val commands = commandFactories.createAll().getOrThrow()
            Result.success(commands.handleAll())
        }
        catch (t: Throwable) {
            Result.failure(t)
        }
    }

    companion object {
        const val IDENTIFIER = "follow"
    }
}

private fun Set<MigrationCommandFactory>.createAll(): Result<List<MigrationCommand>> {
    return try {
        flatMap { it.create(Storage.getSubscriptions()).getOrThrow() }
            .let { Result.success(it) }
    }
    catch (t: Throwable) {
        Result.failure(t)
    }
}

private fun List<MigrationCommand>.handleAll() {
    sortedBy { it is MigrationCommand.DeleteSubscriptionCommand }
        .forEach { command ->
            when (command) {
                is MigrationCommand.DeleteSubscriptionCommand -> Storage.delete(command.uuid)
                is MigrationCommand.MutateSubscriptionCommand -> {
                    Storage.updateSubscriptionValues(command.uuid, command.mutatedValues)
                }
            }
        }
}
