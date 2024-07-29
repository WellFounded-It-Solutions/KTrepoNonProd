package se.infomaker.streamviewer.config

import com.google.gson.JsonObject
import timber.log.Timber
import java.io.Serializable

data class PickerConfig(val type: String?, val title: String?, val icon: String?, var config: JsonObject?) : Serializable {
    /**
     * Returns whether the type is null or not, and warns timber if it is.
     * @return if the config contains a type, if not returns false
     */
    fun isTypeNull(): Boolean {
        if (type == null) {
            Timber.w("Type was null")
            return true
        }
        return false
    }
}
