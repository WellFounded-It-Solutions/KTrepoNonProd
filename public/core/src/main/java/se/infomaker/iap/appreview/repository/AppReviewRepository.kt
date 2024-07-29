package se.infomaker.iap.appreview.repository

import android.content.Context
import io.reactivex.subjects.BehaviorSubject
import se.infomaker.iap.appreview.AppReviewConfig
import se.infomaker.iap.appreview.PreferencesHelper
import se.infomaker.iap.appreview.data.entity.AppDataProvider
import timber.log.Timber
import java.util.concurrent.TimeUnit


class AppReviewRepository(
    context: Context,
    private val appReviewConfig: AppReviewConfig? = null,
) {

    private val preferencesHelper = PreferencesHelper(context)
    private val appDataProvider = AppDataProvider(context)
    private var snoozeInterval = 0L
    private var minimumUsageTime = 0L
    private var debug = false
    val startReviewBehaviourSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    val startRateOnPlayBehaviourSubject: BehaviorSubject<Boolean> =
        BehaviorSubject.createDefault(false)
    val startEmailBehaviourSubject: BehaviorSubject<Pair<String, String>> = BehaviorSubject.create()

    companion object {
        const val DEFAULT_MIN_USAGE_TIME = 30L // 30 mins
        const val DEFAULT_SNOOZE_TIME = 14L // 14 Days
    }

    init {
        configure(
            appReviewConfig?.appReview?.snoozeInterval?.toLong(),
            appReviewConfig?.appReview?.minimumUsageTime?.toLong(),
            appReviewConfig?.appReview?.debug
        )
    }

    fun appData() = appDataProvider.appData

    fun configure(snoozeInterval: Long?, minimumUsageTime: Long?, debug: Boolean?) {
        this.snoozeInterval = snoozeInterval ?: DEFAULT_SNOOZE_TIME
        this.minimumUsageTime = minimumUsageTime ?: DEFAULT_MIN_USAGE_TIME
        this.debug = debug ?: false
    }

    fun reset() {
        preferencesHelper.neverAsk = false
        preferencesHelper.snoozeStartTime = 0
    }

    fun shouldCheckTriggerPoint(): Boolean =
        isFeatureEnabled() && !shouldNeverAsk() && !isSnoozed() && exceedsMinimumUsageTime()

    /**
     * Checks whether the app review feature is enabled.
     */
    private fun isFeatureEnabled(): Boolean = appReviewConfig?.appReview?.enabled ?: true

    /**
     * Called by the AppReviewStatisticsInterceptor when the
     * correct sequence of events has been achieved
     */
    fun triggerPointDetected() {
        if (!isFeatureEnabled() || shouldNeverAsk()) {
            return
        }
        startReviewBehaviourSubject.onNext(true)
        snooze()
    }

    /**
     * Saves the current time to SharedPrefs so it is possible to calculate
     * the entire duration of a snooze.
     */
    fun snooze() {
        preferencesHelper.snoozeStartTime = generateTimestamp()
    }

    /**
     * Saves the user preference to never be asked for a rating again.
     */
    fun setNeverAsk() {
        preferencesHelper.neverAsk = true
    }

    fun shouldNeverAsk(): Boolean = preferencesHelper.neverAsk

    /**
     * Determines if the review process has been snoozed by the user.
     */
    fun isSnoozed(): Boolean {
        val snoozeEndTime =
            if (debug) snoozeInterval else TimeUnit.DAYS.toSeconds(snoozeInterval)
        if (generateTimestamp() < preferencesHelper.snoozeStartTime + snoozeEndTime) {
            return true
        }
        return false
    }

    /**
     * Determines if the minimum app usage time has been exceeded.
     */
    fun exceedsMinimumUsageTime(): Boolean {
        val minimumUsageTime =
            if (debug) minimumUsageTime else TimeUnit.MINUTES.toSeconds(
                minimumUsageTime
            )
        if (preferencesHelper.totalUsageTime > minimumUsageTime) {
            return true
        }
        return false
    }

    /**
     * Save the session start time to SharedPrefs
     */
    private fun saveSessionStartTime() {
        preferencesHelper.sessionStart = generateTimestamp()
    }

    /**
     * Save the session end time to SharedPrefs
     */
    private fun saveSessionEndTime() {
        preferencesHelper.sessionEnd = generateTimestamp()
    }

    /**
     * Calculates and update the total app usage time
     */
    fun updateTotalUsageTime() {
        if (!isFeatureEnabled() || shouldNeverAsk()) {
            return
        }
        saveSessionEndTime()
        preferencesHelper.totalUsageTime += preferencesHelper.sessionEnd - if (preferencesHelper.sessionStart == 0) preferencesHelper.sessionEnd else preferencesHelper.sessionStart
        Timber.d("totalSessionTime=${preferencesHelper.totalUsageTime}")
        saveSessionStartTime()
    }

    /**
     * Generate in timestamp in seconds
     */
    private fun generateTimestamp() = (System.currentTimeMillis() / 1000).toInt()

    fun openEmailClient(emailSubject: String, emailBody: String) {
        appReviewConfig?.appReview?.feedbackEmail?.let {
            startEmailBehaviourSubject.onNext(Pair(emailSubject, emailBody))
        }
    }
}