package com.navigaglobal.mobile.follow.migration.extensions

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import okio.IOException
import org.json.JSONObject
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.livecontentmanager.config.LiveContentConfig
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentmanager.parser.PropertyObjectParser
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService
import timber.log.Timber
import se.infomaker.livecontentmanager.query.lcc.opencontent.Result as OpenContentResult

@WorkerThread
internal fun OpenContentService.objectsExist(uuids: List<String>): Result<List<String>> {
    if (uuids.isEmpty()) return Result.success(emptyList())

    val queryParams = mapOf(
        "q" to uuids.joinToString("\" OR uuid:\"", prefix = "uuid:\"", postfix = "\""),
        "properties" to "uuid",
        "limit" to uuids.size.toString()
    )
    val response = search(queryParams).blockingGet()
    val result = Gson().fromJson(response.body(), OpenContentResult::class.java)
    return if (result != null) {
        Result.success(result.hits.hits.map { it.id })
    }
    else {
        Result.failure(IOException("Could not check if objects exist. Code [${response.code()}], message: ${response.message()}"))
    }
}

@WorkerThread
internal fun OpenContentService.objects(
    uuids: List<String>,
    liveContentConfig: LiveContentConfig,
    objectParser: PropertyObjectParser
): Result<List<PropertyObject>> {
    if (uuids.isEmpty()) return Result.success(emptyList())

    val queryParams = mapOf(
        "q" to "${liveContentConfig.search.baseQuery} AND ${uuids.joinToString("\" OR uuid:\"", prefix = "uuid:\"", postfix = "\"")}",
        "properties" to liveContentConfig.defaultProperties,
        "contenttype" to liveContentConfig.search.contentType,
        "limit" to uuids.size.toString()
    )
    return try {
        val response = search(queryParams).blockingGet()
        if (response.body() != null) {
            val result = objectParser.fromSearch(JSONUtil.wrap("payload.data.result", JSONObject(response.body().toString())), liveContentConfig.defaultPropertyMap)
            Result.success(result)
        }
        else {
            Timber.e("Failed to fetch objects with error: ${response.code()} - ${response.message()}")
            Result.failure(NullPointerException("response.body() was null."))
        }
    } catch (ioe: IOException) {
        Result.failure(ioe)
    }
}