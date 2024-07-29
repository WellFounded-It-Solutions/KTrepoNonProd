package se.infomaker.googleanalytics.dispatcher

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface GoogleAnalyticsApi {

    @POST("/batch")
    fun batch(@Body payload: String): Call<ResponseBody>
}