package se.infomaker.iap.action.http

import android.content.Context
import com.samskivert.mustache.DefaultCollector
import com.samskivert.mustache.Mustache
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.Operation
import timber.log.Timber
import java.util.concurrent.CountDownLatch

class HttpActionTest {

    private lateinit var httpAction: HttpAction

    @Before
    fun runBefore() {
        println()
        httpAction = HttpAction(OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor { message ->
                println(message)
            }.also { it.level = HttpLoggingInterceptor.Level.BODY })
            .build())
    }

    @Test(timeout = 10_000)
    fun testGet() {
        Timber.d("Starting")
        val latch = CountDownLatch(1)
        httpAction.perform(MOCKED_CONTEXT, Operation(action = "http-action", moduleID = "myModule", parameters = GET_VERSION_INFO, values = EmptyValueProvider())) {
            Assert.assertEquals("404", it.value?.getString("test-id.response.statusCode"))
            latch.countDown()
        }
        latch.await()
    }

    companion object {
        init {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    println(message)
                }
            })
        }

        val JSONOBJECT_FETCHER: Mustache.VariableFetcher = object : Mustache.VariableFetcher {
            @Throws(Exception::class)
            override fun get(ctx: Any, name: String): Any? {
                return (ctx as ValueProvider).getString(name)
            }

            override fun toString(): String = "GSON_JSONOBJECT_FETCHER"
        }

        val JSONOBJECT_COLLECTOR = object : DefaultCollector() {
            override fun createFetcher(ctx: Any?, name: String?): Mustache.VariableFetcher {
                if (ctx is ValueProvider) {
                    return JSONOBJECT_FETCHER
                }
                return super.createFetcher(ctx, name)
            }
        }

        val MUSTACHE_COMPILER = Mustache.compiler().withCollector(JSONOBJECT_COLLECTOR)
        val MOCKED_CONTEXT = Mockito.mock(Context::class.java)

        val GET_VERSION_INFO = JSONObject(
            """
                {
                    "id": "test-id",
                    "method": "GET",
                    "url": "https://app-update-versions.s3-eu-west-1.amazonaws.com/android/se.infomaker.test"
                }
            """.trimIndent()
        )
    }
}

private class EmptyValueProvider : ValueProvider {
    override fun getStrings(keyPath: String) = null
    override fun getString(keyPath: String) = null
    override fun observeString(keyPath: String) = Observable.just("")
}