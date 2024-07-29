package se.infomaker.frt.prefetch

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import se.infomaker.frt.ui.fragment.SettingsFragment
import timber.log.Timber
import java.util.concurrent.TimeUnit


class PrefetchManager(private val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val REPEAT_INTERVAL: Long = 2L // hours
        const val FLEX_INTERVAL: Long = 30L // minutes
    }

    private var isPrefetchEnabled = false
    private var sharedPreferences: SharedPreferences? = null
    private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

    private val prefetchPeriodicWorkRequest = PeriodicWorkRequest.Builder(
            PrefetchWorkRequest::class.java,
            REPEAT_INTERVAL, TimeUnit.HOURS,
            FLEX_INTERVAL, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInitialDelay(30, TimeUnit.SECONDS).build()

    init {
        configureSharedPreferences()
        initialiseWorkManager(context)
        updateWorkManagerState()
    }

    private fun configureSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        isPrefetchEnabled = sharedPreferences?.getBoolean(SettingsFragment.KEY_PREF_PREFETCH, false) ?: false
    }

    private fun initialiseWorkManager(context: Context) {
        val workManagerConfig = Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.WARN)
                .build()
        WorkManager.initialize(context.applicationContext, workManagerConfig)
    }

    private fun updateWorkManagerState() {
        if (isPrefetchEnabled) {
            startPrefetching()
            return
        }
        stopPrefetching()
    }

    private fun startPrefetching() {
        Timber.d("Prefetching enabled.")
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PrefetchWorkRequest.tag, ExistingPeriodicWorkPolicy.REPLACE, prefetchPeriodicWorkRequest)
    }

    private fun stopPrefetching() {
        Timber.d("Prefetching disabled.")
        WorkManager.getInstance(context).cancelAllWorkByTag(PrefetchWorkRequest.tag)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == SettingsFragment.KEY_PREF_PREFETCH) {
            Timber.d("Prefetch setting changed.")
            isPrefetchEnabled = sharedPreferences?.getBoolean(SettingsFragment.KEY_PREF_PREFETCH, false)
                    ?: false
            updateWorkManagerState()
        }
    }
}