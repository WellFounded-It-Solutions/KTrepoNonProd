package se.infomaker.iap.update

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.navigaglobal.mobile.di.VersionCode
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import se.infomaker.frtutilities.ForegroundDetector
import se.infomaker.iap.SpringBoardManager
import se.infomaker.iap.extensions.asUpdateType
import se.infomaker.iap.extensions.now
import se.infomaker.iap.update.ui.UpdateActivity
import se.infomaker.iap.update.version.VersionRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @VersionCode current: Long,
    private val versionRepo: VersionRepository,
    private val prefs: SharedPreferences
) {

    private val garbage = CompositeDisposable()
    private val versionCode = if (current == 0L) Long.MAX_VALUE else current

    private var lastCheck = prefs.getLong(LAST_VERSION_CHECK_KEY, 0L)
        set(value) {
            prefs.edit {
                putLong(LAST_VERSION_CHECK_KEY, value)
            }
            field = value
        }

    var presenter: UpdatePresenter? = null

    init {

        garbage.add(ForegroundDetector.observable()
                .subscribeOn(Schedulers.io())
                .subscribe { foreground ->
                    if (foreground && allowCheck()) {
                        versionRepo.refresh()
                        checked(now())
                    }
                })

        garbage.add(ForegroundDetector.observable()
                .filter { it }
                .flatMap { versionRepo.observe() }
                .map { Update(it.asUpdateType(versionCode)) }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {

                    presenter?.let { presenter ->
                        presenter.present(it)
                        return@subscribe
                    }

                    when (it.type) {
                        UpdateType.REQUIRED, UpdateType.OBSOLETE -> SpringBoardManager.restart(appContext)
                        UpdateType.RECOMMENDED -> {
                            // If the app is in route, do nothing SpringBoardActivity will handle it
                            if (SpringBoardManager.router?.currentRoute() == null) {
                                UpdateActivity.openIfAllowed(appContext, it)
                            }
                        }
                        else -> Timber.d("No update available.")
                    }
                })
    }

    fun get(): Update {
        val updateType = versionRepo.get().asUpdateType(versionCode)
        return Update(updateType)
    }

    private fun allowCheck(): Boolean {
        return now() - lastCheck > VERSION_CHECK_PERIOD
    }

    private fun checked(timestamp: Long) {
        lastCheck = timestamp
    }

    companion object {
        private const val LAST_VERSION_CHECK_KEY = "last.version.check"
        private val VERSION_CHECK_PERIOD = TimeUnit.MINUTES.toMillis(5)
    }
}