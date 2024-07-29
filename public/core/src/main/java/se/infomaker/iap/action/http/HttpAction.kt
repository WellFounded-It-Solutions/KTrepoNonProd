package se.infomaker.iap.action.http

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.navigaglobal.mobile.R
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.ActionValueProvider
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import se.infomaker.iap.action.createOperation
import se.infomaker.iap.action.display.flow.mustachify
import se.infomaker.iap.action.display.resourceManager
import se.infomaker.iap.action.flatMapped
import timber.log.Timber
import javax.inject.Inject

class HttpAction @Inject constructor(
    private val okHttpClient: OkHttpClient
) : ActionHandler {

    override fun isLongRunning(): Boolean = true

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        //If we don't have a url, let's just return and get on with our lives
        var parameters = operation.parameters
        operation.parameters.optString("configuration", null)?.let { configFile ->
            try {
                val asset = operation.resourceManager(context).getAsset("configuration/$configFile", JsonObject::class.java)
                // Make this better baby
                parameters =  JSONObject(asset.toString())
            } catch (e: Throwable) {
                Timber.e(e, "Failed to parse configuration")
            }
        }

        val url = parameters.optString("url", null)?.mustachify(operation.values) ?: let {
            onResult(Result(false, operation.values, "No url set"))
            return
        }

        val gson = Gson()

        val body: JsonObject = parameters.optJSONObject("body")?.mustachify(operation.values)?.toJsonObject(gson) ?: JsonObject()
        val queryParams: Map<String, String> = parameters.optJSONObject("queryParams")?.mustachify(operation.values)?.toJsonObject(gson)?.toMap() ?: mapOf()
        val headers: Map<String, String> = parameters.optJSONObject("headers")?.mustachify(operation.values)?.toJsonObject(gson)?.toMap() ?: mapOf()
        try {
            Retrofit.Builder()
                    .baseUrl(if (url.endsWith("/")) url else "$url/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(AbstractHTTP::class.java)
                    .mapCall(method = parameters.optString("method", "GET"),
                            body = body,
                            headers = headers,
                            queryParams = queryParams)
                    .enqueue(object : Callback<ReturnType> {
                        override fun onResponse(call: Call<ReturnType>, response: Response<ReturnType>) {
                            val keyPath = "${parameters["id"]?.toString() ?: "http"}.response"
                            val responseMap = mutableMapOf("$keyPath.statusCode" to response.raw().code.toString())

                            if (response.isSuccessful) {
                                response.body().flatMapped(keyPath, responseMap)
                            } else {
                                response.errorBody()?.string()?.let { error ->
                                    try {
                                        gson.fromJson(error, JsonObject::class.java).flatMapped(keyPath, responseMap)
                                    } catch (e: JsonSyntaxException) {
                                        responseMap.put("$keyPath.error", error)
                                    }
                                }
                            }
                            val valueProvider = ActionValueProvider(operation.values, responseMap)

                            val responseAction = parameters.responseAction()
                            //If we have specified a response action, run it, else just return result success
                            if (responseAction != null) {
                                responseAction.createOperation(valueProvider, operation.moduleID ?: "global").perform(context, onResult)
                            } else {
                                onResult(Result(true, valueProvider, null))
                            }
                        }

                        override fun onFailure(call: Call<ReturnType>?, t: Throwable?) {
                            Timber.d("Got failure")
                            onResult(Result(false, operation.values, context.resources.getString(R.string.http_action_failure)))
                        }
                    })
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Failed to build ")
            onResult(Result(false, operation.values, "Could not complete request"))
        }
    }

    override fun canPerform(context: Context, operation: Operation): Boolean {
        operation.parameters.optString("url", null) ?: let {
            return false
        }
        return true
    }
}

private fun JSONObject.responseAction(): JSONObject? = optJSONObject("responseAction")

private fun JSONObject.tryGetJsonObject(key: String, gson: Gson): JsonObject? {
    return optJSONObject(key)?.let {
        return@let gson.fromJson(it.toString(), JsonObject::class.java)
    }
}

private fun JSONObject.toJsonObject(gson: Gson): JsonObject? {
    return gson.fromJson(this.toString(), JsonObject::class.java)
}

private fun AbstractHTTP.mapCall(method: String, headers: Map<String, String>, body: JsonObject, queryParams: Map<String, String>): Call<ReturnType> {
    return when (method.toUpperCase()) {
        "POST" -> post(headers = headers, queryParams = queryParams, body = body)
        "PUT" -> put(headers = headers, queryParams = queryParams, body = body)
        "DELETE" -> delete(headers = headers, queryParams = queryParams)
    //GET
        else -> get(headers = headers, queryParams = queryParams)
    }
}

private fun JsonObject.toMap(): Map<String, String> {
    return this.entrySet().filter { (_, value) ->
        value.isJsonPrimitive
    }.map { (key, value) ->
        key to value.asString.toString()
    }.toMap()
}
