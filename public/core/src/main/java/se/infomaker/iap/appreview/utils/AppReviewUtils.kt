package se.infomaker.iap.appreview.utils

import android.os.Build
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.MustacheException
import se.infomaker.iap.appreview.data.entity.AppDataProvider
import timber.log.Timber

class AppReviewUtils {

    fun getEmailBody(appData: AppDataProvider.AppData, emailBody: String): String {
        try {
            return Mustache.compiler().compile(emailBody).execute(object: Any() {
                val APP = AppDataProvider.AppData(
                    appData.name,
                    appData.versionName,
                    appData.versionCode,
                    Build.VERSION.RELEASE,
                    Build.MANUFACTURER,
                    Build.MODEL
                )
            }).toString()
        } catch (e: MustacheException){
            Timber.e(e,"Error generating mustachioed string")
        }
        return emailBody
    }

    fun formatString(appData:AppDataProvider.AppData, template: String, fallback: String? = null): String {
        return try {
            Mustache.compiler().compile(template).execute(object: Any() {
                val APP = AppDataProvider.AppData(
                    appData.name
                )
            }).toString()
        } catch (e: MustacheException){
            Timber.e(e,"Error generating mustachioed app name")
            fallback ?: ""
        }
    }
}