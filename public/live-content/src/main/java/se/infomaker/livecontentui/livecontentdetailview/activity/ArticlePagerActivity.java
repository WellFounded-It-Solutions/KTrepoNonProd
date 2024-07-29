package se.infomaker.livecontentui.livecontentdetailview.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import se.infomaker.datastore.Article;
import se.infomaker.datastore.DatabaseSingleton;
import se.infomaker.frtutilities.AppBarOwner;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.frtutilities.connectivity.Connectivity;
import se.infomaker.frtutilities.connectivity.ConnectivityUtils;
import se.infomaker.iap.articleview.item.image.ParallaxImagePageTransformer;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.ktx.ThemeUtils;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;
import se.infomaker.iap.theme.util.UI;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.query.FilterHelper;
import se.infomaker.livecontentmanager.query.MatchFilter;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.livecontentmanager.stream.HitsListStream;
import se.infomaker.livecontentmanager.stream.StreamListener;

import com.navigaglobal.mobile.livecontent.R;
import com.navigaglobal.mobile.livecontent.databinding.TranslucentAppBarBinding;
import se.infomaker.livecontentui.AccessManager;
import se.infomaker.livecontentui.LiveContentStreamProvider;
import se.infomaker.livecontentui.MenuActivity;
import se.infomaker.livecontentui.StatsHelper;
import se.infomaker.livecontentui.ads.StickyAdsCoordinator;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.livecontentdetailview.adapter.ArticleFragmentStatePagerAdapter;
import se.infomaker.livecontentui.livecontentdetailview.pageadapters.ArticlePageAdapterFactory;
import se.infomaker.livecontentui.livecontentdetailview.swipe.DepthPageTransformer;
import se.infomaker.livecontentui.livecontentdetailview.view.ToggleSwipableViewPager;
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils;
import se.infomaker.livecontentui.offline.OfflineBannerCoordinator;
import se.infomaker.livecontentui.offline.OfflineBannerLayout;
import se.infomaker.livecontentui.offline.OfflineBannerModel;
import se.infomaker.livecontentui.sharing.SharingManager;
import se.infomaker.livecontentui.sharing.SharingResponse;
import se.infomaker.livecontentui.view.appbar.TranslucentAppBarCoordinator;
import timber.log.Timber;

@AndroidEntryPoint
public class ArticlePagerActivity extends MenuActivity implements StreamListener<PropertyObject>, AppBarOwner {
    private static final String ARG_CURRENT_ARTICLE_ID = "selectedArticleId";
    private static final String ARG_MODULE_ID = "hitsListListId";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SOURCE = "source";
    public static final int SEARCH_MORE_THRESHOLD = 3;

    @Inject LiveContentStreamProvider streamProvider;
    @Inject SharingManager sharingManager;

    private Disposable connectivityDisposable;
    private ToggleSwipableViewPager mPager;
    private ArticleFragmentStatePagerAdapter mPagerAdapter;
    private String mModuleId;

    private int mStartPosition;
    private Toolbar mToolbar;

    private boolean isWaitingForLiveContent = false;
    private View mNewMessagesLayout;

    private LiveContentUIConfig mConfig;
    private List<QueryFilter> mFilters;
    private String source;
    private boolean delayedRestore;

    private long timeLeft;
    private AppBarLayout appBarLayout;
    private TextView toolbarTitle;
    private FrameLayout emptyContainer;
    private FrameLayout offlineWarningContainer;
    private boolean mShouldDisplayEmptyView;
    private AccessManager accessManager;
    private ResourceManager resourceManager;
    private OfflineBannerCoordinator offlineBannerCoordinator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            mFilters = FilterHelper.getFilters(intent);
            if (bundle != null) {
                mModuleId = bundle.getString(ARG_MODULE_ID);
                mConfig = ConfigManager.getInstance(getApplicationContext()).getConfig(mModuleId, LiveContentUIConfig.class);
                if (savedInstanceState == null) {
                    source = bundle.getString(ARG_SOURCE, null);
                }
                restoreCurrentArticlePosition(bundle);
            }
        }
        accessManager = new AccessManager(this, mModuleId);
        resourceManager = new ResourceManager(this, mModuleId);

        if (mConfig.getTranslucentToolbar()) {
            setContentView(R.layout.activity_article_pager_translucent);
        }
        else {
            setContentView(R.layout.activity_article_pager);
        }
        setupEmptyView();
        setupOfflineWarningView();

        toolbarTitle = findViewById(R.id.toolbar_title);
        mPager = findViewById(R.id.pager);
        appBarLayout = findViewById(R.id.app_bar);

        ArrayList<PropertyObject> objects = new ArrayList<>();
        for (int i = 0; i < getStream().size(); i++) {
            objects.add(getStream().get(i));
        }

        ArticlePageAdapterFactory pageAdapterFactory = ArticlePageAdapterFactory.getFactory(
                this,
                objects,
                mConfig,
                mModuleId);
        mPagerAdapter = pageAdapterFactory.getPageAdapter(getSupportFragmentManager(), mConfig.getThemeOverlayMapping());
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(mPagerAdapter);
        setupPageTransformer();
        mPager.setCurrentItem(mStartPosition);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position >= mPagerAdapter.getCount() - SEARCH_MORE_THRESHOLD && !isWaitingForLiveContent) {
                    getStream().searchMore();
                    isWaitingForLiveContent = true;
                }
                pushViewShowToStatisticsManager(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    mNewMessagesLayout.setVisibility(View.INVISIBLE);
                }
                appBarLayout.setExpanded(true);
            }
        });

        mNewMessagesLayout = findViewById(R.id.newMessages);
        mNewMessagesLayout.findViewById(R.id.newMessagesClickArea).setOnClickListener(v -> mPager.setCurrentItem(0));
        mToolbar = findViewById(R.id.toolbar);

        OfflineBannerLayout offlineBannerLayout = findViewById(R.id.offline_banner);
        offlineBannerCoordinator = new OfflineBannerCoordinator(offlineBannerLayout, resourceManager);
        getLifecycle().addObserver(offlineBannerCoordinator);

        if (mConfig.getAds() != null) {
            StickyAdsCoordinator stickyAdsCoordinator = new StickyAdsCoordinator(findViewById(R.id.top_sticky_ad_wrapper), findViewById(R.id.bottom_sticky_ad_wrapper), mConfig.getAds().getProvider(), mConfig.getAds().getStickyArticle());
            getLifecycle().addObserver(stickyAdsCoordinator);
        }

        if (mConfig.getTranslucentToolbar()) {
            ViewGroup root = findViewById(R.id.content_wrapper);
            TranslucentAppBarBinding appBarBinding = TranslucentAppBarBinding.bind(root);
            TranslucentAppBarCoordinator translucentAppBarCoordinator = new TranslucentAppBarCoordinator(root, appBarBinding);
            getLifecycle().addObserver(translucentAppBarCoordinator);
        }

        initToolbar();
        Theme moduleTheme = ThemeManager.getInstance(this).getModuleTheme(mModuleId);
        setupColors(moduleTheme);
        moduleTheme.apply(findViewById(android.R.id.content));
    }

    private void setupEmptyView() {
        emptyContainer = findViewById(R.id.empty_container);
        if (getStream().hasReachedEnd() && getStream().size() == 0) {
            mShouldDisplayEmptyView = true;
        }
        emptyContainer.setVisibility(mShouldDisplayEmptyView ? View.VISIBLE : View.INVISIBLE);
        inflateAndThemeLayout(emptyContainer, "no_articles", R.layout.no_articles);
    }

    private void setupOfflineWarningView() {
        offlineWarningContainer = findViewById(R.id.offline_warning_container);
        View offlineView = inflateAndThemeLayout(offlineWarningContainer, "offline_warning", R.layout.offline_warning_default);

        if (offlineView != null) {
            String offlineWarningTitle = resourceManager.getString("offline_warning_title", null);
            TextView titleView = offlineView.findViewById(R.id.offline_warning_title);
            if (titleView != null) {
                titleView.setText(offlineWarningTitle);
            }

            String offlineWarningMessage = resourceManager.getString("offline_warning_message", null);
            TextView messageView = offlineView.findViewById(R.id.offline_warning_message);
            if (messageView != null) {
                messageView.setText(offlineWarningMessage);
            }
        }
    }

    private View inflateAndThemeLayout(ViewGroup parent, String layoutResourceName, int defaultLayoutIdentifier) {
        int layoutIdentifier = resourceManager.getLayoutIdentifier(layoutResourceName);
        if (layoutIdentifier < 1) {
            layoutIdentifier = defaultLayoutIdentifier;
        }
        View inflated = LayoutInflater.from(this).inflate(layoutIdentifier, parent, true);
        ThemeManager.getInstance(this).getModuleTheme(mModuleId).apply(parent);
        return inflated;
    }

    private void setupPageTransformer() {
        String pagerEffect = mConfig.getPagerEffect();
        if (DepthPageTransformer.DEPTH_EFFECT.equals(pagerEffect)) {
            mPager.setPageMargin(0);
            mPager.setPageTransformer(true, new DepthPageTransformer());
        }
        else {
            mPager.setPageMargin((int) UI.dp2px(4));
            mPager.setPageTransformer(true, new ParallaxImagePageTransformer());
        }
    }

    private void restoreCurrentArticlePosition(Bundle bundle) {
        String articleId = bundle.getString(ARG_CURRENT_ARTICLE_ID);
        if (articleId != null) {
            for (int i = 0; i < getStream().size(); i++) {
                if (articleId.equals(getStream().get(i).getId())) {
                    mStartPosition = i;
                    return;
                }
            }
            delayedRestore = true;
        }
    }

    private HitsListStream getStream() {
        return streamProvider.provide(mConfig.getLiveContent(), mConfig.getProperties(), mFilters);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        Theme theme = ThemeManager.getInstance(this).getModuleTheme(mModuleId);
        final Drawable up = getResources().getDrawable(resourceWithFallback(resourceManager, "action_up", R.drawable.up_arrow));
        if (mConfig.getTranslucentToolbar()) {
            up.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        } else {
            up.setColorFilter(theme.getColor("toolbarAction", ThemeColor.WHITE).get(), PorterDuff.Mode.SRC_ATOP);
        }
        getSupportActionBar().setHomeAsUpIndicator(up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (!mConfig.getTranslucentToolbar()) {
            String title = getIntent().getStringExtra(ARG_TITLE);
            toolbarTitle.setText(TextUtils.isEmpty(title) ? ModuleInformationManager.getInstance().getModuleTitle(mModuleId) : title);
        }
    }


    private void setupColors(Theme theme) {
        ThemeColor appBackground = theme.getColor("appBackground", ThemeColor.WHITE);

        mPager.setBackgroundColor(appBackground.get());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        final Drawable up = getResources().getDrawable(resourceWithFallback(resourceManager, "action_up", R.drawable.up_arrow));
        if (mConfig.getTranslucentToolbar()) {
            up.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        else {
            up.setColorFilter(theme.getColor("toolbarAction", ThemeColor.WHITE).get(), PorterDuff.Mode.SRC_ATOP);
            theme.getText("toolbarTitle", ThemeTextStyle.DEFAULT).apply(theme, toolbarTitle);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(theme.getColor("toolbarColor", ThemeColor.GRAY).get()));
        }
        getSupportActionBar().setHomeAsUpIndicator(up);
        ThemeUtils.apply(theme, getWindow());
    }

    private int resourceWithFallback(ResourceManager resourceManager, String resourceName, int fallback){
        int identifier = resourceManager.getDrawableIdentifier(resourceName);
        return identifier != 0 ? identifier : fallback;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        getStream().removeListener(this);
        timeLeft = System.currentTimeMillis();
        if (connectivityDisposable != null) {
            connectivityDisposable.dispose();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getStream().addListener(this);
        connectivityDisposable = Connectivity.INSTANCE.observable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connected -> {
                    if (connected) {
                        getStream().reset();
                    }
                }, Timber::e);

        if (getStream().hasError() && getStream().size() == 0) {
            showError();
        }

        //More than 500ms since leaving viewTreeObserver (or rotating, this is to prevent statistics on rotate)
        if (System.currentTimeMillis() - timeLeft > 500) {
            pushViewShowToStatisticsManager(mPager.getCurrentItem());
        }

        updateOfflineBanner();
    }

    private void updateOfflineBanner() {
        if (!DefaultUtils.isOnMainThread()) {
            runOnUiThread(this::updateOfflineBanner);
            return;
        }
        if (offlineBannerCoordinator != null) {
            boolean hasContent = getStream().getItems().size() > 0;
            Date last;
            if (hasContent) {
                last = getStream().getLastUpdated();
            }
            else {
                last = getStream().getLastUpdateAttempt();
            }
            if (last != null) {
                offlineBannerCoordinator.bind(new OfflineBannerModel(last, hasContent));
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnData = new Intent();
        Timber.d("%d : %d", mPager.getCurrentItem(), mPager.getCurrentItem());
        PropertyObject hitsList = mPagerAdapter.getHitslistAtPosition(mPager.getCurrentItem());
        if (hitsList != null) {
            returnData.putExtra("itemId", mPagerAdapter.getHitslistAtPosition(mPager.getCurrentItem()).getId());
        }
        setResult(RESULT_OK, returnData);
        supportFinishAfterTransition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            PropertyObject hitsList = mPagerAdapter.getHitslistAtPosition(mPager.getCurrentItem());
            if (hitsList != null) {
                bundle.putString(ARG_CURRENT_ARTICLE_ID, hitsList.getId());
            }
            intent.putExtras(bundle);
        }
        setIntent(intent);
        super.onSaveInstanceState(outState);
    }

    private void pushViewShowToStatisticsManager(int position) {
        PropertyObject propertyObject = mPagerAdapter.getCount() > 0 ? mPagerAdapter.getHitslistAtPosition(position) : null;
        if (propertyObject == null) {
            return;
        }

        Observable<SharingResponse> sharing = null;
        if (mConfig.getSharing() != null && mConfig.getSharing().getShareApiUrl() != null) {
            sharing = sharingManager.getSharingUrl(propertyObject.getId());
        }

        StatsHelper.logArticleShowStatsEvent(propertyObject, mModuleId, source,
                accessManager.observeAccessAttributes(Observable.just(propertyObject)).firstOrError(),
                sharing != null ? sharing.firstOrError() : null);

        source = null;

        new Thread(() -> DatabaseSingleton.getDatabaseInstance().userLastViewDao().insert(new Article(propertyObject.getId(), "Temp name", new Date()))).start();
    }

    @Override
    public void onItemsAdded(final int index, final List<PropertyObject> items) {
        if (!DefaultUtils.isOnMainThread()) {
            if(index < items.size()){
                runOnUiThread(() -> onItemsAdded(index, items));
                StatsHelper.logArticleViewEvent(items.get(index),mModuleId);
            }
            return;
        }

        isWaitingForLiveContent = false;
        if (index == 0) {
            if (mPagerAdapter.getCount() != 0 && items.size() > 0) {
                mNewMessagesLayout.setVisibility(View.VISIBLE);
            }
            mPagerAdapter.addHitsListListsToStart(items);
        }
        else {
            mPagerAdapter.addHitsListListsToEnd(items);
        }
        if (delayedRestore) {
            restoreCurrentArticlePosition(getIntent().getExtras());
            mPager.setCurrentItem(mStartPosition);
            delayedRestore = false;
        }

        updateEmptyView();
        updateOfflineBanner();
    }

    private void updateEmptyView() {
        if (!DefaultUtils.isOnMainThread()) {
            runOnUiThread(this::updateEmptyView);
            return;
        }
        if (mPagerAdapter == null) {
            return;
        }
        mShouldDisplayEmptyView = mPagerAdapter.getCount() == 0;
        if (emptyContainer == null) {
            return;
        }
        emptyContainer.setVisibility(mShouldDisplayEmptyView ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onItemsRemoved(List<PropertyObject> items) {
        mPagerAdapter.updateDataSet(getStream().getItems(), null);
        updateEmptyView();
        updateOfflineBanner();
    }

    @Override
    public void onItemsChanged(List<PropertyObject> items) {
        mPagerAdapter.updateDataSet(getStream().getItems(), items);
        updateEmptyView();
        updateOfflineBanner();
    }

    @Override
    public void onEndReached() {
        updateEmptyView();
    }

    @Override
    public void onReset() {
        updateOfflineBanner();
    }

    @Override
    public void onError(Exception exception) {
        runOnUiThread(() -> {
            if (getStream().size() == 0) {
                showError();
            }
            updateOfflineBanner();
        });
    }

    private void showError() {
        if (!ConnectivityUtils.hasInternetConnection(this)) {
            offlineWarningContainer.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    public static void openArticle(Context context, String moduleId, String title, String articleUuid) {
        openArticle(context, moduleId, title, articleUuid, null);
    }

    public static void openArticle(Context context, String moduleId, String title, String articleUuid, String source) {
        Intent intent = new Intent(context, ArticlePagerActivity.class);
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_MODULE_ID, moduleId);
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_SOURCE, source);
        ArrayList<QueryFilter> filters = new ArrayList<>();
        filters.add(new MatchFilter("uuid", articleUuid));
        FilterHelper.put(intent, filters);
        intent.putExtras(arguments);
        context.startActivity(intent);
    }
}