package se.infomaker.iap.update.version

import android.content.SharedPreferences
import androidx.core.content.edit
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import se.infomaker.iap.update.di.VersionStorePreferences
import javax.inject.Inject

class VersionStore @Inject constructor(
    @VersionStorePreferences private val prefs: SharedPreferences
) {

    private val versionInfoRelay: BehaviorRelay<VersionInfo>

    init {
        val didMigrate = prefs.getBoolean(MIGRATION_KEY, false)
        val (obsolete, required, recommended) = if (didMigrate) {
            Triple(
                prefs.getBoolean(OBSOLETE_KEY, false),
                prefs.getLong(REQUIRED_KEY, 0),
                prefs.getLong(RECOMMENDED_KEY, 0)
            )
        }
        else {
            val required = prefs.getInt(REQUIRED_KEY, 0).toLong()
            val recommended = prefs.getInt(RECOMMENDED_KEY, 0).toLong()
            prefs.edit {
                putLong(REQUIRED_KEY, required)
                putLong(RECOMMENDED_KEY, recommended)
                putBoolean(MIGRATION_KEY, true)
            }
            Triple(
                prefs.getBoolean(OBSOLETE_KEY, false),
                required,
                recommended
            )
        }

        versionInfoRelay = BehaviorRelay.createDefault(VersionInfo(obsolete, required, recommended))
    }

    fun observe(): Observable<VersionInfo> = versionInfoRelay

    fun set(versionInfo: VersionInfo) {
        prefs.edit {
            putBoolean(OBSOLETE_KEY, versionInfo.obsolete)
            putLong(REQUIRED_KEY, versionInfo.required)
            putLong(RECOMMENDED_KEY, versionInfo.recommended)
        }
        versionInfoRelay.accept(versionInfo)
    }

    fun get(): VersionInfo = versionInfoRelay.value ?: VersionInfo.DEFAULT
    
    companion object Keys {
        private const val OBSOLETE_KEY = "obsolete"
        private const val REQUIRED_KEY = "required"
        private const val RECOMMENDED_KEY = "recommended"
        private const val MIGRATION_KEY = "migrated_to_long"
    }
}