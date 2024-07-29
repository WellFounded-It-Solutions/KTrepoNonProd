package se.infomaker.iap.update.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.navigaglobal.mobile.R
import dagger.hilt.android.AndroidEntryPoint
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ktx.findActivity
import se.infomaker.frtutilities.ktx.openStoreListing
import se.infomaker.frtutilities.ktx.privatePreferences
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.SpringBoardManager
import se.infomaker.iap.extensions.now
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.theme.view.ThemeableTextView
import se.infomaker.iap.update.Update
import se.infomaker.iap.update.UpdateManager
import se.infomaker.iap.update.UpdatePresenter
import se.infomaker.iap.update.UpdateType
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class UpdateActivity : AppCompatActivity(), UpdatePresenter {

    @Inject lateinit var updateManager: UpdateManager
    @Inject lateinit var sharedPreferences: SharedPreferences

    private val update by lazy { intent.getParcelableExtra(UPDATE_KEY) as? Update ?: throw IllegalStateException("UpdateActivity started without an appropriate Update.") }
    private val resourceManager by resources()
    private val theme by theme()

    private var shouldReportViewShow = true

    private val title: String
        get() = when (update.type) {
            UpdateType.OBSOLETE -> resourceManager.getString("appUpdateObsoleteTitle", getString(R.string.default_app_update_obsolete_title))
            UpdateType.REQUIRED -> resourceManager.getString("appUpdateRequiredTitle", getString(R.string.default_app_update_required_title))
            else -> resourceManager.getString("appUpdateRecommendedTitle", getString(R.string.default_app_update_recommended_title))
        }

    private val titleThemeKey: String
        get() = when (update.type) {
            UpdateType.OBSOLETE -> "appUpdateObsoleteTitle"
            UpdateType.REQUIRED -> "appUpdateRequiredTitle"
            else -> "appUpdateRecommendedTitle"
        }

    private val description: String
        get() = when (update.type) {
            UpdateType.OBSOLETE -> resourceManager.getString("appUpdateObsoleteDescription", getString(R.string.default_app_update_obsolete_description))
            UpdateType.REQUIRED -> resourceManager.getString("appUpdateRequiredDescription", getString(R.string.default_app_update_required_description))
            else -> resourceManager.getString("appUpdateRecommendedDescription", getString(R.string.default_app_update_recommended_description))
        }

    private val descriptionThemeKey: String
        get() = when (update.type) {
            UpdateType.OBSOLETE -> "appUpdateObsoleteDescription"
            UpdateType.REQUIRED -> "appUpdateRequiredDescription"
            else -> "appUpdateRecommendedDescription"
        }

    private val updateButtonVisibility: Int
        get() = if (update.type == UpdateType.OBSOLETE) View.GONE else View.VISIBLE

    private val updateButtonText: String
        get() = resourceManager.getString("appUpdatePrompt", getString(R.string.default_app_update_button_text))

    private val laterButtonVisibility: Int
        get() = if (update.type == UpdateType.RECOMMENDED) View.VISIBLE else View.GONE

    private val laterButtonText: String
        get() = resourceManager.getString("appUpdateRecommendedPromptLater", getString(R.string.default_app_update_later_button_text))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        setupViews()
        theme.apply(this)

        if (savedInstanceState != null) {
            val destroyTime = savedInstanceState.getLong(DESTROY_TIME_KEY, 0)
            shouldReportViewShow = System.currentTimeMillis() - destroyTime > RECREATION_TIMEOUT
        }
    }

    private fun setupViews() {

        findViewById<ThemeableTextView>(R.id.title)?.apply {
            text = title
            themeKeys = listOf(titleThemeKey)
        }

        findViewById<ThemeableTextView>(R.id.description)?.apply {
            text = description
            themeKeys = listOf(descriptionThemeKey)
        }

        findViewById<Button>(R.id.update_button)?.apply {
            setOnClickListener {
                openStoreListing()
            }
            visibility = updateButtonVisibility
            text = updateButtonText
        }

        findViewById<Button>(R.id.later_button)?.apply {
            setOnClickListener {
                postponeUpdate()
                finish()
            }
            visibility = laterButtonVisibility
            text = laterButtonText
        }
    }

    override fun present(update: Update) {
        when {
            update.type == UpdateType.NONE && !isTaskRoot -> finish()
            else -> SpringBoardManager.restart(this)
        }
    }

    override fun onResume() {
        super.onResume()
        updateManager.presenter = this

        if (shouldReportViewShow) {
            StatisticsEvent.Builder()
                .viewShow()
                .viewName("update")
                .attribute("type", update.type.name.lowercase())
                .build()
                .also { event ->
                    StatisticsManager.getInstance().logEvent(event)
                }
        }
    }

    override fun onPause() {
        updateManager.presenter = null
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(DESTROY_TIME_KEY, System.currentTimeMillis())
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (update.type == UpdateType.RECOMMENDED) postponeUpdate()
        super.onBackPressed()
    }

    private fun postponeUpdate() {
        sharedPreferences.edit {
            putLong(LAST_UPDATE_OFFER, now())
        }
    }

    companion object {
        private const val UPDATE_KEY = "initial.update"
        private const val LAST_UPDATE_OFFER = "last.recommended.update.offer"
        private val ONE_DAY = TimeUnit.DAYS.toMillis(1)
        private const val DESTROY_TIME_KEY = "destroyTime"
        private const val RECREATION_TIMEOUT = 700

        fun openIfAllowed(context: Context, update: Update, intent: Intent = Intent(), onComplete: (() -> Unit)? = null) {

            if (update.type != UpdateType.RECOMMENDED || allowRecommendedUpdatePresentation(context)) {
                val updateIntent = Intent(context, UpdateActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)

                    putExtras(intent)
                    putExtra(UPDATE_KEY, update)

                    val activity = context.findActivity()
                    if (activity == null) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                context.startActivity(updateIntent)
            }

            onComplete?.invoke()
        }

        private fun allowRecommendedUpdatePresentation(context: Context): Boolean {
            val lastOffer = context.privatePreferences().getLong(LAST_UPDATE_OFFER, 0L)
            return now() - lastOffer > ONE_DAY
        }
    }
}