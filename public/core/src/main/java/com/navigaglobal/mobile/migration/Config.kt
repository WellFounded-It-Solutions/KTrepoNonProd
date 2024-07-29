package com.navigaglobal.mobile.migration

import com.google.gson.JsonObject

data class Config(val migrations: Map<Int, List<Migration>>? = null) {
    data class Migration(
        val identifier: String,
        val config: JsonObject? = null
    )
}