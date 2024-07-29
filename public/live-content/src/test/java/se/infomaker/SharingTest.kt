package se.infomaker

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import se.infomaker.frtutilities.TextUtils
import se.infomaker.livecontentui.config.SharingConfig
import se.infomaker.livecontentui.sharing.SharingManager
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.util.concurrent.CountDownLatch


class SharingTest {

    @Test
    fun sharingConfig() {
        try {
            var config = SharingConfig(titleKey = "SHARE ME")
        } catch (e: IllegalArgumentException){
            Timber.e(e)
        }
        try {
            var config = SharingConfig(shareApiUrl = "https://www.norran.se/api/article/", titleKey = "SHARE ME")
        } catch (e: IllegalArgumentException){
            Timber.e(e)
        }
        try {
            var config1 = SharingConfig(shareKey = "siteUrl", titleKey = "SHARE ME")
        } catch (e: IllegalArgumentException){
            Timber.e(e)
        }
    }

    @Test
    fun shareAndShareAlike() {
        val properties = JSONObject()
        properties.put("isPremium", JSONArray().put("false"))
        val shareUrls = JSONArray()
        shareUrls.put("http://www.bt.se/link")
        shareUrls.put("http://www.barometern.se/link")
        shareUrls.put("http://www.ostran.se/link")
        properties.put("siteUrl", shareUrls)

        val shareConfig = SharingConfig(shareKey = "siteUrl", preferredShareSource = "ostran.se", titleKey = "SHARE ME")
        val shareValue = properties.getJSONArray(shareConfig.shareKey)
        var found:String? = null
        shareValue.let { urls ->
            for (i in 0 until urls.length()) {
                val url = urls.getString(i)

                if (url.contains(shareConfig.preferredShareSource ?: "", ignoreCase = true)) {
                    found = url
                }
            }
        }
        if (found == null) {
            found = shareValue.getString(0)
        }
        val showShare = found != null && shareValue.length() > 0 && !TextUtils.isEmpty(shareValue.optString(0))
    }
}