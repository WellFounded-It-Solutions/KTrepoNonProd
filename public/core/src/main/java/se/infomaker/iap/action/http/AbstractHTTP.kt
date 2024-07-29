package se.infomaker.iap.action.http

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.QueryMap
import retrofit2.http.Url

typealias ReturnType = JsonObject

interface AbstractHTTP {
    @GET
    fun get(@Url url: String = "",
            @HeaderMap headers: Map<String, String> = mapOf(),
            @QueryMap queryParams: Map<String, String> = mapOf()
    ): Call<ReturnType>

    @POST
    fun post(@Url url: String = "",
             @HeaderMap headers: Map<String, String> = mapOf(),
             @QueryMap queryParams: Map<String, String> = mapOf(),
             @Body body: JsonObject = JsonObject()
    ): Call<ReturnType>

    @DELETE
    fun delete(@Url url: String = "",
               @HeaderMap headers: Map<String, String> = mapOf(),
               @QueryMap queryParams: Map<String, String> = mapOf()
    ): Call<ReturnType>

    @PUT
    fun put(@Url url: String = "",
            @HeaderMap headers: Map<String, String> = mapOf(),
            @QueryMap queryParams: Map<String, String> = mapOf(),
            @Body body: JsonObject = JsonObject()
    ): Call<ReturnType>
}