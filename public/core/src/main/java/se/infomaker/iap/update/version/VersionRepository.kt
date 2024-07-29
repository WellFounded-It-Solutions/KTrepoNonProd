package se.infomaker.iap.update.version

import com.navigaglobal.mobile.di.PackageName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class VersionRepository @Inject constructor(
    @PackageName private val packageName: String,
    private val versionService: VersionService,
    private val versionStore: VersionStore
) {

    fun refresh() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                versionService.getVersion(packageName)?.let {
                    versionStore.set(it)
                }
            }
            catch (e: Exception) {
                if (e is HttpException && e.code() == 404) {
                    Timber.d("No version information set up in backend.")
                    versionStore.set(VersionInfo.DEFAULT)
                }
                else {
                    Timber.e(e, "Could not retrieve version information.")
                }
            }
        }
    }

    fun observe() = versionStore.observe()

    fun get() = versionStore.get()
}