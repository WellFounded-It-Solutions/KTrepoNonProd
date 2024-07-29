package se.infomaker.frt.statistics.blacklist

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface BlackListBackend {
    @GET("{packagename}/android-{versionCode}.json")
    fun blacklist(@Path("packagename") packageName: String,
                  @Path("versionCode") versionCode: Long): Call<BlackList>
}