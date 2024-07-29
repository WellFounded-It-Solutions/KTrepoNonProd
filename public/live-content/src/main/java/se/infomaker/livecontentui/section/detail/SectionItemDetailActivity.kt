package se.infomaker.livecontentui.section.detail

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import com.navigaglobal.mobile.livecontent.R
import com.navigaglobal.mobile.livecontent.databinding.ActivitySectionItemDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import se.infomaker.datastore.Article
import se.infomaker.datastore.DatabaseSingleton
import se.infomaker.frtutilities.AppBarOwner
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.ktx.config
import se.infomaker.frtutilities.ktx.moduleInfo
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.livecontentui.AccessManager
import se.infomaker.livecontentui.MenuActivity
import se.infomaker.livecontentui.ads.StickyAdsCoordinator
import se.infomaker.livecontentui.extensions.allItems
import se.infomaker.livecontentui.livecontentdetailview.frequency.FrequencyManagerProvider
import se.infomaker.livecontentui.offline.OfflineBannerCoordinator
import se.infomaker.livecontentui.offline.OfflineBannerModel
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.SectionManager
import se.infomaker.livecontentui.section.configuration.SectionedLiveContentUIConfig
import se.infomaker.livecontentui.section.datasource.newspackage.ArticleSectionItem
import se.infomaker.livecontentui.section.ktx.registerShown
import se.infomaker.livecontentui.sharing.SharingManager
import java.util.Date
import javax.inject.Inject
import kotlin.concurrent.thread

@AndroidEntryPoint
class SectionItemDetailActivity : MenuActivity(), AppBarOwner {

    private val moduleInfo by moduleInfo()
    private val resourceManager by resources()
    private val config: SectionedLiveContentUIConfig by config()
    private val theme by theme()

    @Inject lateinit var sharingManager: SharingManager

    private val itemId by lazy { intent?.getStringExtra(ITEM_ID_KEY) }
    private val frequencyManager by lazy { FrequencyManagerProvider.provide(this) }
    private val accessManager by lazy { AccessManager(this, moduleInfo.identifier) }

    private lateinit var binding: ActivitySectionItemDetailBinding

    override val appBarLayout by lazy { binding.appBar }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySectionItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleToolbar(binding.toolbar, binding.toolbarTitle)
        handleTheme(theme, binding)

        val fragmentTag = "$FRAGMENT_TAG_PREFIX$itemId"
        val currentFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        val sections = SectionManager.getInstance().current(config)
        if (currentFragment == null) {
            sections.flatMap { section ->
                section.items().allItems
            }.firstOrNull { it.id == itemId }?.let {
                registerShown(it)
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        it.createDetailView(moduleInfo.identifier),
                        fragmentTag
                    )
                    .commit()
            }
        }

        val offlineBannerCoordinator = OfflineBannerCoordinator(binding.offlineBanner, resourceManager)
        sections.mapNotNull { it.lastUpdated() }
            .maxByOrNull { it.time }
            ?.let {
                offlineBannerCoordinator.bind(OfflineBannerModel(it, true))
            }
        lifecycle.addObserver(offlineBannerCoordinator)

        val stickyAdsCoordinator = StickyAdsCoordinator(binding.topStickyAdWrapper, binding.bottomStickyAdWrapper, config.ads?.provider, config.ads?.stickyArticle)
        lifecycle.addObserver(stickyAdsCoordinator)
    }

    private fun registerShown(sectionItem: SectionItem) {
        (sectionItem as? ArticleSectionItem)?.let { articleItem ->
            articleItem.registerShown(accessManager, config.sharing, sharingManager, moduleInfo.identifier)
            frequencyManager.registerArticleRead(articleItem.propertyObject)
        }
        thread { DatabaseSingleton.getDatabaseInstance().userLastViewDao().insert(Article(sectionItem.id, "Temp name", Date())) }
    }

    private fun handleTheme(theme: Theme, binding: ActivitySectionItemDetailBinding) {
        theme.apply(this)

        theme.getColor("toolbarAction", ThemeColor.BLACK)?.let {
            binding.toolbar.navigationIcon?.colorFilter = PorterDuffColorFilter(it.get(), PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun handleToolbar(toolbar: Toolbar, toolbarTitle: TextView) {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false)

            val upIcon = AppCompatResources.getDrawable(this, resourceWithFallback(resourceManager, "action_up", R.drawable.up_arrow))
            it.setHomeAsUpIndicator(upIcon)
        }
        toolbarTitle.text = moduleInfo.title
    }

    private fun resourceWithFallback(resourceManager: ResourceManager, resourceName: String, fallbackIdentifier: Int): Int {
        val identifier = resourceManager.getDrawableIdentifier(resourceName)
        return if (identifier != 0) identifier else fallbackIdentifier
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private const val ITEM_ID_KEY = "itemId"
        private const val MODULE_ID_KEY = "moduleId"
        private const val MODULE_TITLE_KEY = "moduleTitle"
        private const val FRAGMENT_TAG_PREFIX = "currentDetailFragment-"

        fun open(context: Context, itemId: String, moduleId: String, moduleTitle: String?, extras: Bundle?) {
            Intent(context, SectionItemDetailActivity::class.java).apply {
                Bundle().apply {
                    extras?.let {
                        putAll(it)
                    }
                    putString(ITEM_ID_KEY, itemId)
                    putString(MODULE_ID_KEY, moduleId)
                    putString(MODULE_TITLE_KEY, moduleTitle)
                }.also {
                    putExtras(it)
                }
            }.also {
                context.startActivity(it)
            }
        }
    }
}