package com.navigaglobal.mobile.follow.migration.command

import com.navigaglobal.mobile.follow.migration.MigrationCommand
import com.navigaglobal.mobile.follow.migration.MigrationCommandFactory
import com.navigaglobal.mobile.follow.migration.extensions.objectsExist
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService
import se.infomaker.storagemodule.model.Subscription
import timber.log.Timber
import javax.inject.Inject

class ConceptExistsCommandFactory @Inject constructor(
    private val openContentService: OpenContentService
) : MigrationCommandFactory {

    override fun create(subscriptions: List<Subscription>): Result<List<MigrationCommand>> {
        val uuidRecords = subscriptions.map { Triple(it.name, it.uuid, it.getValue("value")) }
            .mapNotNull { (name, uuid, conceptUuid) ->
                if (uuid != null && conceptUuid != null) SubscriptionRecord(name, uuid, conceptUuid) else null
            }
        return try {
            val validUuids = openContentService.objectsExist(uuidRecords.map { it.conceptUuid }).getOrThrow()
            uuidRecords.filterNot { validUuids.contains(it.conceptUuid) }
                .map {
                    Timber.d("Creating command to delete subscription ${it.name}, " +
                            "${it.conceptUuid} does not exist.")
                    MigrationCommand.DeleteSubscriptionCommand(it.uuid)
                }
                .let { Result.success(it) }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}

private data class SubscriptionRecord(val name: String?, val uuid: String, val conceptUuid: String)