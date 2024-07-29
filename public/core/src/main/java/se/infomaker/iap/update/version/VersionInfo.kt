package se.infomaker.iap.update.version

import com.google.gson.annotations.SerializedName

data class VersionInfo(
    val obsolete: Boolean,
    @SerializedName("minimumRequiredVersion") val required: Long,
    @SerializedName("minimumRecommendedVersion") val recommended: Long
) {

    companion object {
        val DEFAULT = VersionInfo(false, 0, 0)
    }
}