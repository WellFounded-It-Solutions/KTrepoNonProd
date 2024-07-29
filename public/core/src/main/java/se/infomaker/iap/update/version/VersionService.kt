package se.infomaker.iap.update.version

import retrofit2.http.GET
import retrofit2.http.Path

interface VersionService {

    @GET("{packageName}")
    suspend fun getVersion(@Path("packageName") packageName: String): VersionInfo?
}