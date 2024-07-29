package se.infomaker.livecontentui.section.detail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.navigaglobal.mobile.livecontent.R;
import com.navigaglobal.mobile.livecontent.databinding.TranslucentAppBarBinding;
import com.rd.animation.type.AnimationType;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observable;
import se.infomaker.datastore.Article;
import se.infomaker.datastore.DatabaseSingleton;
import se.infomaker.frtutilities.AppBarOwner;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.articleview.item.image.ParallaxImagePageTransformer;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.ktx.ThemeUtils;
import se.infomaker.iap.theme.style.text.ThemeTextStyle;
import se.infomaker.iap.theme.util.UI;
import se.infomaker.iap.ui.promotion.view.ThemeablePageIndicatorView;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.AccessManager;
import se.infomaker.livecontentui.MenuActivity;
import se.infomaker.livecontentui.OnPresentationContextChangedListener;
import se.infomaker.livecontentui.StatsHelper;
import se.infomaker.livecontentui.ads.StickyAdsCoordinator;
import se.infomaker.livecontentui.di.DataSourceProviderFactory;
import se.infomaker.livecontentui.livecontentdetailview.frequency.FrequencyManager;
import se.infomaker.livecontentui.livecontentdetailview.frequency.FrequencyManagerProvider;
import se.infomaker.livecontentui.livecontentdetailview.swipe.DepthPageTransformer;
import se.infomaker.livecontentui.livecontentdetailview.view.ToggleSwipableViewPager;
import se.infomaker.livecontentui.offline.OfflineBannerCoordinator;
import se.infomaker.livecontentui.offline.OfflineBannerLayout;
import se.infomaker.livecontentui.offline.OfflineBannerModel;
import se.infomaker.livecontentui.section.Section;
import se.infomaker.livecontentui.section.SectionItem;
import se.infomaker.livecontentui.section.SectionManager;
import se.infomaker.livecontentui.section.SectionedLiveContentFragment;
import se.infomaker.livecontentui.section.configuration.Orientation;
import se.infomaker.livecontentui.section.configuration.SectionedLiveContentUIConfig;
import se.infomaker.livecontentui.section.datasource.newspackage.ArticleSectionItem;
import se.infomaker.livecontentui.section.datasource.newspackage.PackageCoverSectionItem;
import se.infomaker.livecontentui.section.ktx.SectionItemUtils;
import se.infomaker.livecontentui.sharing.SharingManager;
import se.infomaker.livecontentui.view.appbar.TranslucentAppBarCoordinator;
import timber.log.Timber;

@AndroidEntryPoint
public class SectionDetailPagerActivity extends MenuActivity implements AppBarOwner, OnPresentationContextChangedListener {

    public static final String MODULE_ID = "moduleId";
    public static final String MODULE_NAME = "moduleName";
    public static final String ITEM_ID = "itemId";
    public static final String SECTION_IDENTIFIER = "sectionIdentifier";
    public static final String GROUP_KEY = "groupKey";
    public static final String IGNORE_SECTION_IDENTIFIER = "ignoreSectionIdentifier";
    private static final String MODULE_TITLE = "moduleTitle";
    private static final int INDEX_NOT_FOUND = -1;

    @Inject DataSourceProviderFactory dataSourceProviderFactory;
    @Inject SharingManager sharingManager;

    private ToggleSwipableViewPager pager;
    private String moduleId;
    private String moduleName;
    private String itemId;
    private String itemSectionIdentifier;
    private String groupKey;
    private SectionedLiveContentUIConfig config;
    private SectionDetailPagerAdapter adapter;
    private FrameLayout loadingContainer;
    private ResourceManager resourceManager;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private boolean goToInitialPage = true;
    private boolean ignoreSectionIdentifier = false;
    private String moduleTitle = "";
    private TextView toolbarTitle;
    private String source;
    private FrequencyManager frequencyManager;
    private AccessManager accessManager;
    private OfflineBannerCoordinator offlineBannerCoordinator;
    private SectionDetailViewModel viewModel;
    private ConstraintLayout contentContainer;

    public static void open(Activity context, String moduleId, String itemId, String title, String itemSectionIdentifier, String groupKey, Bundle extras) {
        Bundle arguments = new Bundle();
        if (extras != null) {
            arguments.putAll(extras);
        }
        arguments.putString(MODULE_ID, moduleId);
        arguments.putString(MODULE_TITLE, title);
        arguments.putString(ITEM_ID, itemId);
        arguments.putString(SECTION_IDENTIFIER, itemSectionIdentifier);
        arguments.putString(GROUP_KEY, groupKey);

        Intent intent = new Intent(context, SectionDetailPagerActivity.class);
        intent.putExtras(arguments);
        context.startActivityForResult(intent, SectionedLiveContentFragment.ITEM_BACK_REQUEST);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String overlay = null;
        Intent intent = getIntent();
        if (intent != null) {
            overlay = intent.getStringExtra("configOverlay");
            moduleId = intent.getStringExtra(MODULE_ID);
            moduleName = intent.getStringExtra(MODULE_NAME);
            moduleTitle = intent.getStringExtra(MODULE_TITLE);
            itemId = intent.getStringExtra(ITEM_ID);
            itemSectionIdentifier = intent.getStringExtra(SECTION_IDENTIFIER);
            groupKey = intent.getStringExtra(GROUP_KEY);
            ignoreSectionIdentifier = intent.getBooleanExtra(IGNORE_SECTION_IDENTIFIER, false);
            if (savedInstanceState == null) {
                source = intent.getStringExtra("source");
            }
        }
        accessManager = new AccessManager(this, moduleId);

        config = ConfigManager.getInstance(getApplicationContext()).getConfig(moduleName, moduleId, SectionedLiveContentUIConfig.class, overlay);

        frequencyManager = FrequencyManagerProvider.INSTANCE.provide(getApplicationContext());
        String packageUuid = getIntent().getExtras().getString("packageUuid");
        String listUuid = getIntent().getExtras().getString("listUuid");
        if (!TextUtils.isEmpty(packageUuid)) {
            SectionedLiveContentUIConfig packageNotificationConfiguration = config.forPackageNotification(packageUuid);
            if (packageNotificationConfiguration != null) {
                // Backwards compatibility
                config = packageNotificationConfiguration;
            }
            else {
                config = config.packageOverlay(packageUuid, moduleName, moduleId);
            }
        }


        if (config.getTranslucentToolbar()) {
            setContentView(R.layout.activity_section_pager_translucent);
        } else {
            setContentView(R.layout.activity_section_pager);
        }

        contentContainer = findViewById(R.id.content_container);
        appBarLayout = findViewById(R.id.app_bar);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);

        loadingContainer = findViewById(R.id.loadingView);
        pager = findViewById(R.id.pager);

        resourceManager = new ResourceManager(this, moduleId);

        setupLoadingLayout();
        initToolbar();

        Theme moduleTheme = ThemeManager.getInstance(this).getModuleTheme(moduleId);
        setupColors(moduleTheme);
        moduleTheme.apply(findViewById(android.R.id.content));

        Orientation forceOrientation = null;
        if (!TextUtils.isEmpty(listUuid)) {
            forceOrientation = Orientation.VERTICAL;
        }
        List<Section> allSections = SectionManager.getInstance().create(dataSourceProviderFactory.create(config), config, moduleTitle, forceOrientation);
        List<Section> sections = Observable.fromIterable(allSections).filter(section -> section.groupKeys().contains(groupKey)).toList().blockingGet();
        for (Section section : sections) {
            getLifecycle().addObserver(section);
        }

        viewModel = new ViewModelProvider(this, new SectionDetailViewModelFactory(resourceManager, moduleId, sections, groupKey, config, this, this))
                .get(SectionDetailViewModel.class);

        viewModel.getViewState().observe(this, state -> {
            List<SectionItem> items = state.getItems();
            if (goToInitialPage) {
                switch (state.getMinSectionState()) {
                    case IDLE: {
                        loadingContainer.setVisibility(View.GONE);
                        pager.setVisibility(View.GONE);
                        break;
                    }
                    case RELOADING:
                    case LOADING: {
                        applyLoadingConstraints();
                        break;
                    }
                    case READY: {
                        submitList(items);
                        int pageIndex = findPageIndex(itemId, items);
                        if (pageIndex == INDEX_NOT_FOUND) {
                            pageIndex = 0;
                        }
                        goToPage(pageIndex);
                        applyContentConstraints();
                        updateOfflineBanner();
                        break;
                    }
                    default: {
                        applyContentConstraints();
                        updateOfflineBanner();
                        break;
                    }
                }
            }
            else {
                submitList(items);
                applyContentConstraints();
                updateOfflineBanner();
            }
        });

        OfflineBannerLayout offlineBannerLayout = findViewById(R.id.offline_banner);
        offlineBannerCoordinator = new OfflineBannerCoordinator(offlineBannerLayout, resourceManager);
        getLifecycle().addObserver(offlineBannerCoordinator);
        updateOfflineBanner();

        if (config.getAds() != null) {
            StickyAdsCoordinator stickyAdsCoordinator = new StickyAdsCoordinator(findViewById(R.id.top_sticky_ad_wrapper), findViewById(R.id.bottom_sticky_ad_wrapper), config.getAds().getProvider(), config.getAds().getStickyArticle());
            getLifecycle().addObserver(stickyAdsCoordinator);
        }

        if (config.getTranslucentToolbar()) {
            ViewGroup root = findViewById(R.id.content_wrapper);
            TranslucentAppBarBinding appBarBinding = TranslucentAppBarBinding.bind(root);
            TranslucentAppBarCoordinator translucentAppBarCoordinator = new TranslucentAppBarCoordinator(root, appBarBinding);
            getLifecycle().addObserver(translucentAppBarCoordinator);
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }*/

    private int findPageIndex(String itemId, List<SectionItem> items) {
        if (ignoreSectionIdentifier) {
            return itemIndex(items, itemId);
        }
        else {
            return itemIndex(items, itemId, itemSectionIdentifier);
        }
    }

    private void applyLoadingConstraints() {
        TransitionManager.beginDelayedTransition(contentContainer);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(contentContainer);
        constraintSet.setVisibility(loadingContainer.getId(), View.VISIBLE);
        constraintSet.setVisibility(pager.getId(), View.GONE);
        constraintSet.applyTo(contentContainer);
    }

    private void applyContentConstraints() {
        TransitionManager.beginDelayedTransition(contentContainer);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(contentContainer);
        constraintSet.setVisibility(loadingContainer.getId(), View.GONE);
        constraintSet.setVisibility(pager.getId(), View.VISIBLE);
        constraintSet.applyTo(contentContainer);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        itemId = savedInstanceState.getString(ITEM_ID);
        itemSectionIdentifier = savedInstanceState.getString(SECTION_IDENTIFIER);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SectionItem currentItem = null;
        if (adapter != null && adapter.getCount() > pager.getCurrentItem()) {
            currentItem = adapter.getCurrentItem(pager.getCurrentItem());
        }
        else if (adapter != null && adapter.getCount() == 0) {
            // This check is here to avoid cluttering Crashlytics non-fatals.
            Timber.d("No items in adapter, nothing to save.");
        }
        else {
            int count = adapter != null ? adapter.getCount() : 0;
            Timber.w("Current item(" + pager.getCurrentItem() + ") out of bounds(" + count + ")");
        }

        if (currentItem != null) {
            outState.putString(ITEM_ID, currentItem.getId());
            outState.putString(SECTION_IDENTIFIER, currentItem.sectionIdentifier());
        }
        super.onSaveInstanceState(outState);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);

        Theme theme = ThemeManager.getInstance(this).getModuleTheme(moduleId);
        final Drawable up = getResources().getDrawable(resourceWithFallback(resourceManager, "action_up", R.drawable.up_arrow));
        if (config.getTranslucentToolbar()) {
            up.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        } else {
            up.setColorFilter(theme.getColor("toolbarAction", ThemeColor.WHITE).get(), PorterDuff.Mode.SRC_ATOP);
        }

        if (config.showBarPagerIndicator()) {
            toolbar.removeView(toolbarTitle);
            ThemeablePageIndicatorView indicatorView = new ThemeablePageIndicatorView(this);
            Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            indicatorView.setLayoutParams(layoutParams);
            indicatorView.setDynamicCount(true);
            indicatorView.setAnimationType(AnimationType.SCALE);
            indicatorView.setInteractiveAnimation(true);
            toolbar.addView(indicatorView);
            toolbar.setContentInsetStartWithNavigation(0);
            indicatorView.setViewPager(pager);
            ThemeManager.getInstance(this).getModuleTheme(moduleId).apply(indicatorView);
        } else if (!config.getTranslucentToolbar()) {
            String title = getIntent().getStringExtra("title");
            if (title == null) {
                title = getIntent().getStringExtra("moduleTitle");
            }
            toolbarTitle.setText(TextUtils.isEmpty(title) ? ModuleInformationManager.getInstance().getModuleTitle(moduleId) : title);
        }
        getSupportActionBar().setHomeAsUpIndicator(up);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    private void setupColors(Theme theme) {
        ThemeColor appBackground = theme.getColor("appBackground", ThemeColor.WHITE);

        loadingContainer.setBackgroundColor(appBackground.get());
        pager.setBackgroundColor(appBackground.get());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        final Drawable up = getResources().getDrawable(resourceWithFallback(resourceManager, "action_up", R.drawable.up_arrow));
        if (config.getTranslucentToolbar()) {
            up.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            DrawableCompat.setTint(up, Color.WHITE);
        }
        else {
            up.setColorFilter(theme.getColor("toolbarAction", ThemeColor.WHITE).get(), PorterDuff.Mode.SRC_ATOP);
            theme.getText("toolbarTitle", ThemeTextStyle.DEFAULT).apply(theme, toolbarTitle);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(theme.getColor("toolbarColor", ThemeColor.GRAY).get()));
        }
        getSupportActionBar().setHomeAsUpIndicator(up);
        ThemeUtils.apply(theme, getWindow());
    }

    private void setupLoadingLayout() {
        int layoutIdentifier = R.layout.default_loading_view;

        if (config.getLoadingLayout() != null) {
            layoutIdentifier = resourceManager.getLayoutIdentifier(config.getLoadingLayout());
        }
        if (layoutIdentifier == 0) {
            layoutIdentifier = R.layout.default_loading_view;
        }
        LayoutInflater.from(this).inflate(layoutIdentifier, loadingContainer);
    }

    private void submitList(List<SectionItem> items) {
        if (pager.getAdapter() == null) {
            setupPager();
            PagerAdapter adapter = createAdapter(items);
            pager.setAdapter(adapter);
        }
        else {
            ((SectionDetailPagerAdapter) pager.getAdapter()).submitList(items);
        }
    }

    private void setupPager() {
        if (!config.keepPositionOfScreen()) {
            pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE && adapter != null) {
                        for (Fragment fragment : adapter.nearbyFragments(pager.getCurrentItem())) {
                            if (fragment instanceof Resetable) {
                                ((Resetable) fragment).reset();
                            }
                        }
                    }
                    appBarLayout.setExpanded(true);
                }
            });
        }
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (!goToInitialPage && adapter != null) {
                    registerReadArticle(adapter.getCurrentItem(position));
                }
            }
        });
        setupPageTransformer();
    }

    private void setupPageTransformer() {
        String pagerEffect = config.getPagerEffect();
        if (DepthPageTransformer.DEPTH_EFFECT.equals(pagerEffect)) {
            pager.setPageMargin(0);
            pager.setPageTransformer(true, new DepthPageTransformer());
        } else {
            pager.setPageMargin((int) UI.dp2px(4));
            pager.setPageTransformer(true, new ParallaxImagePageTransformer());
        }
    }

    private PagerAdapter createAdapter(List<SectionItem> items) {
        adapter = new SectionDetailPagerAdapter(getSupportFragmentManager(), moduleId, items);
        return adapter;
    }

    private void registerReadArticle(SectionItem currentItem) {
        if (currentItem instanceof ArticleSectionItem) {
            ArticleSectionItem articleItem = (ArticleSectionItem) currentItem;
            SectionItemUtils.registerShown(articleItem, accessManager, config.getSharing(), sharingManager, moduleId);
            frequencyManager.registerArticleRead(articleItem.getPropertyObject());
            source = null;
        } else if (currentItem instanceof PackageCoverSectionItem) {
            PropertyObject propertyObject = ((PackageCoverSectionItem) currentItem).getPropertyObject();
            StatsHelper.logPackagePreviewShowStatsEvent(propertyObject, moduleId, source);
            frequencyManager.registerArticleRead(propertyObject);
            source = null;
        }

        new Thread(() -> DatabaseSingleton.getDatabaseInstance().userLastViewDao().insert(new Article(currentItem.getId(), "Temp name", new Date()))).start();
    }

    private void goToPage(int pageIndex) {
        SectionItem item = adapter.getCurrentItem(pageIndex);
        if (item != null) {
            registerReadArticle(item);
        }
        pager.setCurrentItem(pageIndex);
        goToInitialPage = false;
    }

    /**
     * Same as {@link #itemIndex(List, String, String)}, but ignoring section identifier.
     *
     * @param itemId
     * @return index ignoring section identifier
     */
    public int itemIndex(List<SectionItem> sectionItems, String itemId) {
        if (sectionItems != null && itemId != null) {
            for (SectionItem currentSectionItem : sectionItems) {
                if (itemId.equals(currentSectionItem.getId())) {
                    return sectionItems.indexOf(currentSectionItem);
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    public int itemIndex(List<SectionItem> sectionItems, String itemId, String itemSectionIdentifier) {
        if (sectionItems != null && itemId != null && itemSectionIdentifier != null) {
            for (SectionItem currentSectionItem : sectionItems) {
                if (itemId.equals(currentSectionItem.getId()) && itemSectionIdentifier.equals(currentSectionItem.sectionIdentifier())) {
                    return sectionItems.indexOf(currentSectionItem);
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    private void updateOfflineBanner() {
        Date lastUpdated = viewModel.getLastUpdated();
        if (lastUpdated != null && offlineBannerCoordinator != null) {
            offlineBannerCoordinator.bind(new OfflineBannerModel(lastUpdated, true));
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnData = new Intent();
        int pagerCurrentItem = pager.getCurrentItem();
        if (adapter != null && adapter.getCount() > pagerCurrentItem) {
            SectionItem currentItem = adapter.getCurrentItem(pagerCurrentItem);
            returnData.putExtra("itemId", currentItem.getId());
        } else {
            returnData.putExtra("itemId", itemId);
        }

        setResult(RESULT_OK, returnData);
        supportFinishAfterTransition();
    }

    @NonNull
    @Override
    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    @Override
    public void onPresentationContextChanged(@NonNull Map<String, JSONObject> changes) {
        // NOP
    }
}
