package se.infomaker.streamviewer.editpage

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.navigaglobal.mobile.livecontent.databinding.ActivityEditPageBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ktx.moduleInfo
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.ktx.listBackgroundColor
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.theme.ktx.toolbarActionColor
import se.infomaker.streamviewer.StatsHelper
import se.infomaker.streamviewer.di.StreamNotificationSettingsHandlerFactory
import javax.inject.Inject

@AndroidEntryPoint
class EditPageActivity : AppCompatActivity() {

    private val moduleInfo by moduleInfo()
    private val resources by resources()
    private val theme by theme()

    private val binding by lazy { ActivityEditPageBinding.inflate(layoutInflater) }

    private var lastPause: Long = 0L
    private var compositeDisposable = CompositeDisposable()

    @Inject lateinit var settingsHandlerFactory: StreamNotificationSettingsHandlerFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            lastPause = savedInstanceState.getLong("lastPause", 0)
        }

        setContentView(binding.root)

        handleTopAppBar(theme)
        handleRecyclerView()

        theme.apply(this)
    }

    private fun handleRecyclerView() {
        binding.recyclerView.setBackgroundColor(theme.listBackgroundColor.get())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = EditPageAdapter(this, binding.recyclerView, moduleInfo.identifier
            ?: "", theme, settingsHandlerFactory)
        binding.recyclerView.itemAnimator = null

        binding.recyclerView.addItemDecoration(EditPageDividerDecoration(theme))
    }

    private fun handleTopAppBar(theme: Theme) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val upOverrideIdentifier = resources.getDrawableIdentifier("action_up")
        if (upOverrideIdentifier > 0) {
            AppCompatResources.getDrawable(this, upOverrideIdentifier)?.let { supportActionBar?.setHomeAsUpIndicator(it) }
        }
        binding.toolbar.navigationIcon?.colorFilter = PorterDuffColorFilter(theme.toolbarActionColor.get(), PorterDuff.Mode.SRC_ATOP)
    }

    override fun onPause() {
        compositeDisposable.clear()
        lastPause = System.currentTimeMillis()
        super.onPause()
    }

    override fun onResume() {
        if (System.currentTimeMillis() - lastPause > TIMEOUT) {
            registerStatistics()
        }
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("lastPause", System.currentTimeMillis())
        super.onSaveInstanceState(outState)
    }

    private fun registerStatistics() {
        StatisticsManager.getInstance().logEvent(StatisticsEvent.Builder()
            .event(StatsHelper.VIEW_SHOW_EVENT)
            .moduleId(moduleInfo.identifier)
            .moduleName(moduleInfo.name)
            .moduleTitle(moduleInfo.title)
            .viewName(STATISTICS_VIEW_NAME).build())
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val MODULE_ID_KEY = "moduleId"

        const val STATISTICS_VIEW_NAME = "editSubscriptionList"
        const val REQUEST_SETTINGS = 152
        const val TIMEOUT = 5000

        @JvmStatic
        fun createIntent(context: Context, moduleId: String): Intent {
            val intent = Intent(context, EditPageActivity::class.java)
            intent.putExtra(MODULE_ID_KEY, moduleId)
            return intent
        }
    }
}