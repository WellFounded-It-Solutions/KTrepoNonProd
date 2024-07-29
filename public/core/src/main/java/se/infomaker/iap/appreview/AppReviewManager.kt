package se.infomaker.iap.appreview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import com.navigaglobal.mobile.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.connectivity.hasInternetConnection
import se.infomaker.frtutilities.ktx.openStoreListing
import se.infomaker.iap.appreview.fragments.AlertDialogFragment
import se.infomaker.iap.appreview.fragments.DialogArgs
import se.infomaker.iap.appreview.repository.AppReviewRepository
import se.infomaker.iap.appreview.utils.DialogStateFactory
import se.infomaker.iap.appreview.utils.DialogType
import timber.log.Timber


class AppReviewManager(
    private val config: AppReviewConfig? = null,
    private val repository: AppReviewRepository
) : LifecycleObserver {

    private var interceptor: AppReviewStatisticsInterceptor? = null
    private var startReviewDisposable: Disposable? = null
    private var startEmailDisposable: Disposable? = null
    private var startPlayRatingDisposable: Disposable? = null
    private var foregroundActivity: FragmentActivity? = null


    init {
        if (repository.shouldCheckTriggerPoint()) {
            interceptor = AppReviewStatisticsInterceptor(repository)
        }
    }

    fun onCreate(activity: FragmentActivity) {
        foregroundActivity = activity
        repository.updateTotalUsageTime()
        interceptor?.let {
            StatisticsManager.getInstance().registerInterceptor(interceptor)

            startReviewDisposable = repository.startReviewBehaviourSubject
                .doOnError { Timber.e(it) }
                .filter { it }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ startReviewProcess() }, { e -> Timber.d(e) })

            startPlayRatingDisposable = repository.startRateOnPlayBehaviourSubject
                .filter { it }
                .doOnError { Timber.e(it) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ startRating() }, { e -> Timber.d(e) })

            startEmailDisposable = repository.startEmailBehaviourSubject
                .doOnError { Timber.e(it) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ startEmailFeedbackProcess(it.first, it.second) }, { e -> Timber.d(e) })
        }
    }

    fun onDestroy() {
        repository.updateTotalUsageTime()
        interceptor?.let {
            StatisticsManager.getInstance().removeInterceptor(interceptor)
        }
        startReviewDisposable?.dispose()
        startPlayRatingDisposable?.dispose()
        startEmailDisposable?.dispose()
        foregroundActivity = null
    }

    /**
     * Begins the feedback process.  If there's an email address configured
     * then we present a dialog in advance of the actual "rating" dialog in order
     * to filter out the negative feedback via email.
     */
    @SuppressLint("CheckResult")
    fun startReviewProcess() {
        foregroundActivity?.let {
            if (!it.hasInternetConnection()) {
                return
            }
            it.runOnUiThread {
                val dialogType = if (config?.appReview?.feedbackEmail.isNullOrEmpty()) {
                    DialogType.RATE
                } else {
                    DialogType.INITIAL
                }
                val dialogStateFactory = DialogStateFactory(
                    it,
                    AppReviewRepositoryProvider.provide().appData(),
                    dialogType
                )
                AppReviewRepositoryProvider.provide().startReviewBehaviourSubject.onComplete()
                val dialog =
                    AlertDialogFragment.newInstance(DialogArgs(dialogStateFactory.dialogStates))
                dialog.show(it.supportFragmentManager, "REVIEW_DIALOG")
            }
        }
    }

    private fun startEmailFeedbackProcess(
        emailSubject: String,
        emailBody: String
    ) {
        AppReviewRepositoryProvider.provide().startEmailBehaviourSubject.onComplete()
        config?.appReview?.feedbackEmail?.let { email ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, Array(1) { email })
                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
            }

            foregroundActivity?.let {
                try {
                    it.startActivity(
                        Intent.createChooser(
                            intent,
                            it.resources.getString(R.string.intent_chooser)
                        )
                    )
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(it, R.string.no_email_client_available, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    /**
     * Begins the actual rating workflow. If the device has the Play Store app installed
     * then the app details are open there.  Otherwise open the app using a browser.
     */
    private fun startRating() {
        AppReviewRepositoryProvider.provide().startRateOnPlayBehaviourSubject.onComplete()
        foregroundActivity?.openStoreListing()
    }
}