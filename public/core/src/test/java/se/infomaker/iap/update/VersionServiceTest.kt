package se.infomaker.iap.update

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.infomaker.iap.update.version.VersionService

class VersionServiceTest {

    @Test
    fun getVersionObject() {
        val okHttpClient = OkHttpClient.Builder().build()
        val versionService = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.khaleejtimes.com/appversion/get/google/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VersionService::class.java)

        val version = runBlocking {
            versionService.getVersion("se.infomaker.test")
        }
        Assert.assertNotNull(version)
        Assert.assertEquals(107L, version?.recommended)
        Assert.assertEquals(100L, version?.required)
    }
}