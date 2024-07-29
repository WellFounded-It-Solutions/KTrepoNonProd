package se.infomaker.frt.statistics.blacklist

import com.navigaglobal.mobile.di.PackageName
import com.navigaglobal.mobile.di.VersionCode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlackListManager @Inject constructor(
    private val backend: BlackListBackend,
    private val store: Store<BlackList>,
    @PackageName private val packageName: String,
    @VersionCode private val versionCode: Long
) : FeatureToggle {

    companion object {
        private const val interval: Long = 12*60*1000*1000
    }

    override fun isEnabled(identifier: String): Boolean {
        conditionalUpdate()
        blackList?.disable?.let {
            return !it.contains(identifier)
        }
        return true
    }

    private var blackList: BlackList? = null

    init {
        blackList = store.get()
        conditionalUpdate()
    }

    /**
     * Avoid having multiple requests flying at once
     */
    private var isUpdating: Boolean = false

    fun conditionalUpdate(onDone: ((Boolean) -> Unit)? = null) {
        if (isUpdating) {
            onDone?.invoke(false)
            return
        }
        val timeSinceLastSet = System.currentTimeMillis() - (store.lastSet()?.time ?: 0)
        if (timeSinceLastSet > interval) {
            forceUpdate {
                onDone?.invoke(true)
            }
        }
        else {
            onDone?.invoke(false)
        }
    }

    fun forceUpdate(onDone: (() -> Unit)? = null) {
        isUpdating = true
        backend.blacklist(packageName, versionCode).enqueue(object : Callback<BlackList> {
            override fun onResponse(call: Call<BlackList>, response: Response<BlackList>) {
                if (response.isSuccessful) {
                    blackList = response.body()
                } else if (response.code() == 404) {
                    blackList = null
                }
                store.set(blackList)
                isUpdating = false
                onDone?.invoke()
            }

            override fun onFailure(call: Call<BlackList>, t: Throwable) {
                Timber.i(t, "Failed to forceUpdate blacklist")
                isUpdating = false
                onDone?.invoke()
            }
        })
    }
}

