package se.infomaker.iap.appreview.data.entity

import android.content.Context
import android.os.Build
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import se.infomaker.frtutilities.GlobalValueManager


class AppDataProvider(context: Context) {

    var appData: AppData = AppData(
        GlobalValueManager.getGlobalValueManager(context).getString("APP.name")
            ?: EMPTY_STRING,
        GlobalValueManager.getGlobalValueManager(context).getString("APP.versionName")
            ?: EMPTY_STRING,
        GlobalValueManager.getGlobalValueManager(context).getString("APP.versionCode")
            ?: EMPTY_STRING,
        Build.VERSION.RELEASE,
        Build.MANUFACTURER,
        Build.MODEL
    )

    @Parcelize
    data class AppData(
        val name: String, var versionName: String? = null,
        var versionCode: String? = null, var sdkRelease: String? = null,
        var manufacturer: String? = null, var model: String? = null
    ) : Parcelable

    companion object {
        const val EMPTY_STRING = ""
    }
}