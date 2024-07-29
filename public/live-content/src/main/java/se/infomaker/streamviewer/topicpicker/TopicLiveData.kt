package se.infomaker.streamviewer.topicpicker

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.TextUtils
import timber.log.Timber
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date

class TopicLiveData @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    @Assisted private val resourceManager: ResourceManager,
    @Assisted("url") private val url: String?,
    @Assisted("asset") private val assetPath: String
) : LiveData<Topic?>() {

    override fun onActive() {
        //Ooops, we do not have URL, load from file
        if(url == null) {
            value = load(context, resourceManager, url, assetPath)
            return
        }

        //Check if we need to start download again
        //NO -> Return from preferences
        val preferences = context.topicPreferences()
        val lastCheck = preferences.getLong("lastCheck:$url", 0)
        if (System.currentTimeMillis() - lastCheck < UPDATE_INTERVAL) {
            Timber.d("Ignoring update, version still current")
            value = load(context, resourceManager, url, assetPath)
            return
        }
        preferences.edit().putLong("lastCheck:$url", System.currentTimeMillis()).apply()

        //YES -> Download, update preferences and return
        val etag = preferences.getString("etag:$url", null)
        remoteDownload(okHttpClient,updateRemoteURL(url) , etag) { topic ->
            topic?.save(context, url)
            Handler(Looper.getMainLooper()).post {
                value = topic ?: load(context, resourceManager, url, assetPath)
            }
        }
    }

    private fun updateRemoteURL(url: String) = "$url?${System.currentTimeMillis()}"


    companion object {
        private const val UPDATE_INTERVAL = 1000

        private fun remoteDownload(okHttpClient: OkHttpClient, url: String, etag: String?, callback: (Topic?) -> Unit) {

            val requestBuilder = Request.Builder().url(url)
            etag?.let {
                if (!TextUtils.isEmpty(etag)) {
                    requestBuilder.addHeader("If-None-Match", etag)
                }
            }

            okHttpClient.newCall(requestBuilder.build()).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Timber.e(e, "Failed to download topics")
                    callback(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    when {
                        response.isSuccessful -> response.body?.string()?.let { topicsJson ->
                            try {
                                // Make sure the content is valid topic json
                                callback(Gson().fromJson(topicsJson, Topic::class.java))
                            } catch (e: JsonSyntaxException) {
                                Timber.e(e, "Response was invalid json")
                                callback(null)
                            }
                        }
                        response.code == 304 -> {
                            Timber.d("Topics already up to date")
                            callback(null)
                        }
                        else -> {
                            Timber.w("Unexpected response ${response.code} - ${response.message}")
                            callback(null)
                        }
                    }
                }
            })
        }

        private fun Context.topicPreferences(): SharedPreferences = getSharedPreferences("topics", Context.MODE_PRIVATE)

        /**
         * Loads topic from SharedPreferences and if found, returns them
         */
        private fun load(context: Context, resourceManager: ResourceManager, url: String?, assetPath: String): Topic? {
            val preferences = context.topicPreferences()
            if (preferences.contains(url)) {
                try {
                    return Gson().fromJson(preferences.getString(url, null), Topic::class.java)
                } catch (e: JsonSyntaxException) {
                    Timber.e(e, "Failed to parse topic")
                }
            }
            try {
                return resourceManager.getAsset(assetPath, Topic::class.java)
            } catch (ex: IOException) {
                Timber.e(ex, "Could not load topics")
            }
            return null
        }

        private fun Topic.save(context: Context, url: String) {
            context.topicPreferences().edit().also { editor ->
                editor.putString(url, Gson().toJson(this))
            }.apply()

        }

        private  fun addCurrentDateAndTimeToUrl(baseUrl: String): String {
            val currentDateAndTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            val encodedDateAndTime = URLEncoder.encode(currentDateAndTime, "UTF-8")

            return "$baseUrl?datetime=$encodedDateAndTime"
        }

    }
}