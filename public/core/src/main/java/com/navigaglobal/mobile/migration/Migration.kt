package com.navigaglobal.mobile.migration

interface Migration {
    val identifier: String
    suspend fun migrate(): Result<Unit>
}