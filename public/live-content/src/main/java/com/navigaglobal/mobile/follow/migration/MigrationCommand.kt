package com.navigaglobal.mobile.follow.migration

sealed class MigrationCommand(val uuid: String) {

    class DeleteSubscriptionCommand(uuid: String) : MigrationCommand(uuid)

    class MutateSubscriptionCommand(uuid: String, val mutatedValues: Map<String, String>) : MigrationCommand(uuid)
}