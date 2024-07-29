package se.infomaker.streamviewer.topicpicker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.config
import se.infomaker.frtutilities.ktx.moduleInfo
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.ktx.chromeColor
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.theme.ktx.toolbarActionColor
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.streamviewer.StatsHelper
import se.infomaker.streamviewer.config.FollowConfig
import se.infomaker.streamviewer.config.TopicPickerConfig
import com.navigaglobal.mobile.livecontent.databinding.ActivityTopicPickerBinding
import se.infomaker.iap.theme.ktx.brandColor
import se.infomaker.iap.theme.ktx.isDarkColor
import se.infomaker.streamviewer.di.StreamNotificationSettingsHandlerFactory
import se.infomaker.streamviewer.di.TopicLiveDataFactory
import se.infomaker.streamviewer.tabs.TopicPickerHelper
import javax.inject.Inject

@AndroidEntryPoint
class TopicPickerActivity : AppCompatActivity() {

    private val moduleInfo by moduleInfo()
    private val resources by resources()
    private val config by config<FollowConfig>()
    private val theme by theme()

    private val binding by lazy { ActivityTopicPickerBinding.inflate(layoutInflater) }
    private val passedTopic by lazy { intent?.getSerializableExtra("topic") as Topic? }
    private val isSubTopic by lazy { passedTopic != null }

    private var topicAdapter: TopicAdapter? = null
    private var exitTime = 0L

    @Inject lateinit var settingsHandlerFactory: StreamNotificationSettingsHandlerFactory
    @Inject lateinit var topicLiveDataFactory: TopicLiveDataFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            exitTime = it.getLong("exitTime", 0L)
        }

        setContentView(binding.root)

        setupAppBar()
        setupTopicRecyclerView()
        setupSearch()

        theme.apply(this)
    }

    private fun setupAppBar() {
        binding.appbar.setBackgroundColor(theme.getColor("toolbarColor", theme.chromeColor).get())
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            if (isSubTopic) {
                it.setDisplayHomeAsUpEnabled(true)
            }
            it.setDisplayShowTitleEnabled(false)
        }

        val upOverrideIdentifier = resources.getDrawableIdentifier("action_up")
        if (upOverrideIdentifier > 0) {
            AppCompatResources.getDrawable(this, upOverrideIdentifier)?.let { supportActionBar?.setHomeAsUpIndicator(it) }
        }
        binding.toolbar.navigationIcon?.colorFilter = PorterDuffColorFilter(theme.toolbarActionColor.get(), PorterDuff.Mode.SRC_ATOP)

        if (!isSubTopic && config.prominentTopicPickerButtons == true) {
            binding.actionButtonContainer.visibility = View.VISIBLE
            binding.cancelButton.setOnClickListener { cancel() }
            binding.saveButton.setOnClickListener { save() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!isSubTopic && config.prominentTopicPickerButtons != true) {
            menuInflater.inflate(R.menu.topic_menu, menu)
            menu.findItem(R.id.topic_menu_item_save)?.let { saveItem ->
                val actionColor = theme.getColor("topicSave", theme.brandColor).get()
                saveItem.title.toString().let {
                    val spannable = SpannableString(it)
                    spannable.setSpan(ForegroundColorSpan(actionColor), 0, it.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                    saveItem.title = spannable
                }
            }
            menu.findItem(R.id.topic_menu_item_cancel)?.let { cancelItem ->
                val toolbarBackgroundColor = theme.getColor("toolbarColor", ThemeColor.DEFAULT_CHROME_COLOR).get()
                val actionColor = if (toolbarBackgroundColor.isDarkColor()) Color.WHITE else Color.BLACK
                cancelItem.title.toString().let {
                    val spannable = SpannableString(it)
                    spannable.setSpan(ForegroundColorSpan(actionColor), 0, it.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                    cancelItem.title = spannable
                }
            }
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.topic_menu_item_cancel -> {
                cancel()
                return true
            }
            R.id.topic_menu_item_save -> {
                save()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupTopicRecyclerView() {

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.addItemDecoration(TopicDividerDecoration(theme))

        if (passedTopic != null) {
            setTopic(passedTopic)
        }
        else {
            val topicPickerConfig = intent.getSerializableExtra("topic_config") as? TopicPickerConfig
            topicPickerConfig?.file?.let { assetPath ->
                val topicsUrl = topicPickerConfig.url
                val handler = Handler().apply { postDelayed({ binding.progressBar.visibility = View.VISIBLE }, 200) }
                topicLiveDataFactory.create(resources, topicsUrl, assetPath).observe(this, { topic ->
                    handler.removeCallbacksAndMessages(null)
                    binding.progressBar.visibility = View.GONE
                    setTopic(topic)
                })
            }
        }
    }

    private fun setTopic(topic: Topic?) {
        topic?.let {
            topicAdapter = TopicAdapter(
                moduleInfo.identifier ?: "",
                theme,
                topic,
                isSubTopic,
                if (isSubTopic) R.layout.topic_title else R.layout.topic_headline,
                R.layout.topic_item
            )
            binding.recyclerView.adapter = topicAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        if (System.currentTimeMillis() - 1000 > exitTime) {
            StatisticsManager.getInstance().logEvent(StatisticsEvent.Builder()
                .event(StatsHelper.VIEW_SHOW_EVENT)
                .moduleId(moduleInfo.identifier)
                .moduleName(moduleInfo.name)
                .moduleTitle(moduleInfo.title)
                .viewName(StatsHelper.TOPIC_PICKER_VIEW).build())
        }
    }

    override fun onPause() {
        super.onPause()
        exitTime = System.currentTimeMillis()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("exitTime", System.currentTimeMillis())
    }

    private fun setupSearch() {
        if (isSubTopic || config.topicPickerAlwaysShowSearchbar == true) {
            binding.search.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    topicAdapter?.setSearch(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
        else {
            binding.searchIcon.visibility = View.GONE
            binding.search.visibility = View.GONE
        }
    }

    private fun cancel() {
        TopicManager.cancel()
        finish()
    }

    private fun save() {
        val enablePushOnSubscription = config.enablePushOnSubscription
        TopicManager.save(enablePushOnSubscription, moduleInfo.identifier ?: "") { subscription ->
            settingsHandlerFactory.create(this, moduleInfo.identifier ?: "", subscription).initialActivate(applicationContext)
        }
        binding.recyclerView.adapter?.notifyDataSetChanged()
        finish()
    }

    override fun onBackPressed() {
        TopicManager.cancel()
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        fun createIntent(context: Context, moduleId: String, topicConfig: TopicPickerConfig?): Intent {
            val intent = Intent(context, TopicPickerActivity::class.java)
            intent.putExtra("moduleId", moduleId)
            intent.putExtra("topic_config", topicConfig
                ?: TopicPickerConfig(
                    file = TopicPickerHelper.DEFAULT_TOPICS_FILE,
                    url = ConfigManager.getInstance(context).getConfig(moduleId, FollowConfig::class.java).topicsUrl))

            return intent
        }
    }
}