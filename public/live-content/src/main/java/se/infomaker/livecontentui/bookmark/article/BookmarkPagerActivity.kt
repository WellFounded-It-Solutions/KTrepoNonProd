package se.infomaker.livecontentui.bookmark.article

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.navigaglobal.mobile.livecontent.R
import com.navigaglobal.mobile.livecontent.databinding.TranslucentAppBarBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import se.infomaker.datastore.Article
import se.infomaker.datastore.Bookmark
import se.infomaker.datastore.DatabaseSingleton
import se.infomaker.frtutilities.AppBarOwner
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.ktx.config
import se.infomaker.frtutilities.ktx.moduleInfo
import se.infomaker.iap.articleview.item.image.ParallaxImagePageTransformer
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.color.ThemeColor
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.iap.theme.style.text.ThemeTextStyle
import se.infomaker.iap.theme.util.UI
import se.infomaker.livecontentui.AccessManager
import se.infomaker.livecontentui.MenuActivity
import se.infomaker.livecontentui.StatsHelper
import se.infomaker.livecontentui.ads.StickyAdsCoordinator
import se.infomaker.livecontentui.config.LiveContentUIConfig
import se.infomaker.livecontentui.livecontentdetailview.adapter.ArticleFragmentStatePagerAdapter
import se.infomaker.livecontentui.livecontentdetailview.pageadapters.ArticlePageAdapterFactory
import se.infomaker.livecontentui.livecontentdetailview.swipe.DepthPageTransformer
import se.infomaker.livecontentui.livecontentdetailview.view.ToggleSwipableViewPager
import se.infomaker.livecontentui.offline.OfflineBannerCoordinator
import se.infomaker.livecontentui.offline.OfflineBannerLayout
import se.infomaker.livecontentui.sharing.SharingManager
import se.infomaker.livecontentui.sharing.SharingResponse
import se.infomaker.livecontentui.view.appbar.TranslucentAppBarCoordinator
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class BookmarkPagerActivity : MenuActivity(), AppBarOwner {

    private val moduleInfo by moduleInfo({ intent?.getStringExtra(ARG_MODULE_ID) }, { intent?.getStringExtra(ARG_TITLE) })
    private val config: LiveContentUIConfig by config { moduleInfo }
    private val moduleTheme by theme { moduleInfo.identifier }

    @Inject lateinit var sharingManager: SharingManager

    private var mPager: ToggleSwipableViewPager? = null
    private var mPagerAdapter: ArticleFragmentStatePagerAdapter? = null

    private var mStartPosition = 0
    private var mToolbar: Toolbar? = null

    private var delayedRestore = false

    private var timeLeft: Long = 0
    private lateinit var mAppBarLayout: AppBarLayout
    override val appBarLayout: AppBarLayout
        get() = mAppBarLayout

    private var toolbarTitle: TextView? = null
    private var emptyContainer: FrameLayout? = null
    private var offlineWarningContainer: FrameLayout? = null
    private var mShouldDisplayEmptyView = false
    private var accessManager: AccessManager? = null
    private lateinit var resourceManager: ResourceManager
    private var bookmarks: List<Bookmark>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        accessManager = AccessManager(this, moduleInfo.identifier)
        resourceManager = ResourceManager(this, moduleInfo.identifier)
        setContentView(if (config.translucentToolbar) R.layout.activity_article_pager_translucent else R.layout.activity_article_pager)

        toolbarTitle = findViewById(R.id.toolbar_title)
        mPager = findViewById(R.id.pager)
        mAppBarLayout  = findViewById(R.id.app_bar)

        mToolbar = findViewById(R.id.toolbar)
        findViewById<OfflineBannerLayout>(R.id.offline_banner)?.let { offlineBannerLayout ->
            OfflineBannerCoordinator(offlineBannerLayout, resourceManager).also {
                lifecycle.addObserver(it)
            }
        }

        if (config.translucentToolbar) {
            val root = findViewById<ViewGroup>(R.id.content_wrapper)
            val appBarBinding = TranslucentAppBarBinding.bind(root)
            TranslucentAppBarCoordinator(root, appBarBinding).also {
                lifecycle.addObserver(it)
            }
        }
        initToolbar()
        setupColors(moduleTheme)
        moduleTheme.apply(findViewById(android.R.id.content))

        config.ads?.let { adsConfig ->
            val stickyAdsCoordinator = StickyAdsCoordinator(findViewById(R.id.top_sticky_ad_wrapper), findViewById(R.id.bottom_sticky_ad_wrapper), adsConfig.provider, adsConfig.stickyArticle)
            lifecycle.addObserver(stickyAdsCoordinator)
        }

        /*
        We need to wait for the bookmarks to be available, we then ignore any changes as the user
        does not expect the bookmarks to change while browsing.
         */
        DatabaseSingleton.getDatabaseInstance().bookmarkDao().all().observe(this, Observer { updated ->
            if (bookmarks == null) {
                bookmarks = updated
                intent.extras?.let {
                    restoreCurrentArticlePosition(updated, it)
                }
                setupEmptyView(updated.isEmpty())
                setupOfflineWarningView()

                val pageAdapterFactory = ArticlePageAdapterFactory.getFactory(
                        this,
                        updated.map { it.propertyObject },
                        config,
                        moduleInfo.identifier)
                mPagerAdapter = pageAdapterFactory.getPageAdapter(supportFragmentManager, config.themeOverlayMapping)
                mPager?.apply {
                    offscreenPageLimit = 1
                    adapter = mPagerAdapter
                    setupPageTransformer()
                    currentItem = mStartPosition
                }
            }
        })

        mPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                appBarLayout.setExpanded(true)
            }
        })
    }

    private fun setupEmptyView(isEmpty: Boolean) {
        emptyContainer = findViewById(R.id.empty_container)
        if (isEmpty) {
            mShouldDisplayEmptyView = true
        }
        emptyContainer?.visibility = if (mShouldDisplayEmptyView) View.VISIBLE else View.INVISIBLE
        inflateAndThemeLayout(emptyContainer, "no_articles", R.layout.no_articles)
    }

    private fun setupOfflineWarningView() {
        offlineWarningContainer = findViewById(R.id.offline_warning_container)
        val offlineView = inflateAndThemeLayout(offlineWarningContainer, "offline_warning", R.layout.offline_warning_default)
        if (offlineView != null) {
            val offlineWarningTitle = resourceManager.getString("offline_warning_title", null)
            val titleView = offlineView.findViewById<TextView>(R.id.offline_warning_title)
            if (titleView != null) {
                titleView.text = offlineWarningTitle
            }
            val offlineWarningMessage = resourceManager.getString("offline_warning_message", null)
            val messageView = offlineView.findViewById<TextView>(R.id.offline_warning_message)
            if (messageView != null) {
                messageView.text = offlineWarningMessage
            }
        }
    }

    private fun inflateAndThemeLayout(parent: ViewGroup?, layoutResourceName: String, defaultLayoutIdentifier: Int): View? {
        var layoutIdentifier = resourceManager.getLayoutIdentifier(layoutResourceName)
        if (layoutIdentifier < 1) {
            layoutIdentifier = defaultLayoutIdentifier
        }
        val inflated = LayoutInflater.from(this).inflate(layoutIdentifier, parent, true)
        moduleTheme.apply(parent)
        return inflated
    }

    private fun setupPageTransformer() {
        if (DepthPageTransformer.DEPTH_EFFECT == config.pagerEffect) {
            mPager?.pageMargin = 0
            mPager?.setPageTransformer(true, DepthPageTransformer())
        } else {
            mPager?.pageMargin = UI.dp2px(4f).toInt()
            mPager?.setPageTransformer(true, ParallaxImagePageTransformer())
        }
    }

    private fun restoreCurrentArticlePosition(bookmarks: List<Bookmark>, bundle: Bundle) {
        val articleId = bundle.getString(ARG_CURRENT_ARTICLE_ID)
        if (articleId != null) {
            bookmarks.filter { it.uuid == articleId }.let {
                mStartPosition = bookmarks.indexOf(it.firstOrNull() ?: 0)
            }
            delayedRestore = true
        }
    }

    private fun initToolbar() {
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (!config.translucentToolbar) {
            toolbarTitle?.text = moduleInfo.title
        }
    }

    private fun setupColors(theme: Theme) {
        val appBackground = theme.getColor("appBackground", ThemeColor.WHITE)
        mPager?.setBackgroundColor(appBackground.get())
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val up = resources.getDrawable(resourceWithFallback(resourceManager, "action_up", R.drawable.up_arrow))
        if (config.translucentToolbar == true) {
            up.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        } else {
            up.setColorFilter(theme.getColor("toolbarAction", ThemeColor.WHITE).get(), PorterDuff.Mode.SRC_ATOP)
            theme.getText("toolbarTitle", ThemeTextStyle.DEFAULT).apply(theme, toolbarTitle)
            supportActionBar?.setBackgroundDrawable(ColorDrawable(theme.getColor("toolbarColor", ThemeColor.GRAY).get()))
        }
        supportActionBar?.setHomeAsUpIndicator(up)
        theme.apply(window)
    }

    private fun resourceWithFallback(resourceManager: ResourceManager, resourceName: String, fallback: Int): Int {
        val identifier = resourceManager.getDrawableIdentifier(resourceName)
        return if (identifier != 0) identifier else fallback
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onPause() {
        super.onPause()
        timeLeft = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        //More than 500ms since leaving viewTreeObserver (or rotating, this is to prevent statistics on rotate)
        if (System.currentTimeMillis() - timeLeft > 500) {
            Handler().postDelayed({
                mPager?.currentItem?.let {
                    pushViewShowToStatisticsManager(it)
                }
            },
                    500)
        }
    }

    override fun onBackPressed() {
        val returnData = Intent()
        mPagerAdapter?.getHitslistAtPosition(mPager?.currentItem ?: 0)?.let {
            returnData.putExtra("itemId", it.id)
        }
        setResult(Activity.RESULT_OK, returnData)
        supportFinishAfterTransition()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val intent = intent
        val bundle = intent.extras
        if (bundle != null) {
            mPager?.currentItem?.let { position ->
                val propertyObject = mPagerAdapter?.getHitslistAtPosition(position)
                if (propertyObject != null) {
                    bundle.putString(ARG_CURRENT_ARTICLE_ID, propertyObject.id)
                }
            }
            intent.putExtras(bundle)
        }
        setIntent(intent)
        super.onSaveInstanceState(outState)
    }

    private fun pushViewShowToStatisticsManager(position: Int) {
        val propertyObject = mPagerAdapter?.getHitslistAtPosition(position) ?: return
        var sharing: Observable<SharingResponse>? = null
        config.sharing?.shareApiUrl?.let {
            sharing = sharingManager.getSharingUrl(propertyObject.id)
        }
        StatsHelper.logArticleShowStatsEvent(propertyObject, moduleInfo.identifier, null,
                accessManager?.observeAccessAttributes(Observable.just(propertyObject))?.firstOrError(),
                sharing?.firstOrError())
        Thread(Runnable { DatabaseSingleton.getDatabaseInstance().userLastViewDao().insert(Article(propertyObject.id, "Temp name", Date())) }).start()
    }

    companion object {

        private const val ARG_CURRENT_ARTICLE_ID = "selectedArticleId"
        private const val ARG_MODULE_ID = "hitsListListId"
        private const val ARG_TITLE = "title"

        fun openArticle(context: Context, moduleId: String?, title: String?, articleUuid: String) {
            val intent = Intent(context, BookmarkPagerActivity::class.java)
            val arguments = Bundle()
            arguments.putSerializable(ARG_MODULE_ID, moduleId)
            arguments.putString(ARG_TITLE, title)
            arguments.putString(ARG_CURRENT_ARTICLE_ID, articleUuid)
            intent.putExtras(arguments)
            context.startActivity(intent)
        }
    }
}