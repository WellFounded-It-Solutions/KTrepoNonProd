package com.navigaglobal.mobile.follow.migration

import se.infomaker.storagemodule.model.Subscription

interface MigrationCommandFactory {
    fun create(subscriptions: List<Subscription>): Result<List<MigrationCommand>>
}