package se.infomaker.livecontentmanager.query.lcc

import com.google.gson.JsonObject
import junit.framework.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Response
import com.navigaglobal.mobile.auth.BasicAuthAuthorizationProvider
import se.infomaker.livecontentmanager.network.NetworkAvailabilityManager
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentBuilder
import timber.log.Timber
import java.util.HashMap
import java.util.concurrent.CountDownLatch

class OpenContentCacheTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()


    @Test
    fun testCacheHit() {
        var hasNetwork = true
        val openContent = OpenContentBuilder().setBaseUrl(TestURLS.OPEN_CONTENT_URL)
                .setCacheDir(temporaryFolder.root)
                .setAuthorizationProvider(BasicAuthAuthorizationProvider("bt", "daic6Nid"))
                .setNetworkAvailabilityManager(object : NetworkAvailabilityManager {
                    override fun hasNetwork(): Boolean {
                        return hasNetwork;
                    }

                }).build()
        val onlineLatch = CountDownLatch(1)
        val result = arrayOfNulls<JsonObject>(2)
        openContent.search(0, 1, "uuid", "*:*", "uuid", HashMap()).subscribe({ response: Response<JsonObject>? ->
            result[0] = response?.body()
            onlineLatch.countDown()
        }, { throwable: Throwable? ->
            Timber.e(throwable)
            onlineLatch.countDown()
        })
        onlineLatch.await()

        hasNetwork = false

        val offlineLatch = CountDownLatch(1)
        openContent.search(0, 1, "uuid", "*:*", "uuid", HashMap()).subscribe({ response: Response<JsonObject>? ->
            result[1] = response?.body()
            offlineLatch.countDown()
        }, { throwable: Throwable? ->
            Timber.e(throwable)
            offlineLatch.countDown()
        })
        offlineLatch.await()
        Assert.assertEquals(result[0], result[1])
    }
}