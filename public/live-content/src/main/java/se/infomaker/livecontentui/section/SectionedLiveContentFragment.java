package se.infomaker.livecontentui.section;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.navigaglobal.mobile.livecontent.R;
import com.navigaglobal.mobile.livecontent.databinding.FragmentSectionedLiveContentBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import se.infomaker.frt.statistics.StatisticsEvent;
import se.infomaker.frt.statistics.StatisticsManager;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.NavigationChromeOwner;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.frtutilities.connectivity.ConnectivityUtils;
import se.infomaker.frtutilities.runtimeconfiguration.OnModuleConfigChangeListener;
import se.infomaker.iap.articleview.item.author.RecyclerViewDividerDecorator;
import se.infomaker.iap.theme.OnThemeUpdateListener;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.size.ThemeSize;
import se.infomaker.iap.theme.view.ThemeableTextView;
import se.infomaker.iap.ui.theme.OverlayThemeProvider;
import se.infomaker.livecontentui.ads.StickyAdsCoordinator;
import se.infomaker.livecontentui.bookmark.BookmarkOverlayItemDecoration;
import se.infomaker.livecontentui.bookmark.Bookmarker;
import se.infomaker.livecontentui.bookmark.BookmarkingResultChannel;
import se.infomaker.livecontentui.config.ErrorConfiguration;
import se.infomaker.livecontentui.di.DataSourceProviderFactory;
import se.infomaker.livecontentui.impressions.NoVisibilityTracker;
import se.infomaker.livecontentui.impressions.ViewTreeObserverVisibilityTracker;
import se.infomaker.livecontentui.impressions.VisibilityTracker;
import se.infomaker.livecontentui.livecontentrecyclerview.activity.LiveContentRecyclerviewActivity;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.HorizontalOffsetItemDecoration;
import se.infomaker.livecontentui.livecontentrecyclerview.decoration.ContentListDividerFactory;
import se.infomaker.livecontentui.livecontentrecyclerview.decoration.ContentListItemBoundaryDecoration;
import se.infomaker.livecontentui.livecontentrecyclerview.fragment.LiveContentRecyclerViewFragment;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilderFactory;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlFactoryProvider;
import se.infomaker.livecontentui.offline.OfflineBannerCoordinator;
import se.infomaker.livecontentui.offline.OfflineBannerModel;
import se.infomaker.livecontentui.offline.OfflineBannerOwner;
import se.infomaker.livecontentui.section.adapter.SectionAdapter;
import se.infomaker.livecontentui.section.configuration.SectionedLiveContentUIConfig;
import timber.log.Timber;

@AndroidEntryPoint
public class SectionedLiveContentFragment extends Fragment implements SectionUpdateNotifier.OnPackageUpdated, OnModuleConfigChangeListener, OnThemeUpdateListener {

    private static final String TIME_LEFT_KEY = "timeLeftKey";
    private static final String EXPANDABLE_SECTION_TRACKER = "expandableSectionTracker";
    private static final ThemeSize DEFAULT_SPACING = new ThemeSize(6);

    public static final int ITEM_BACK_REQUEST = 545;

    @Inject DataSourceProviderFactory dataSourceProviderFactory;
    private SectionedLiveContentUIConfig config;
    private String moduleId;
    private String moduleName;
    private ResourceManager resourceManager;
    private SectionAdapter adapter;
    private CompositeDisposable resumedDisposables = new CompositeDisposable();
    private CompositeDisposable garbage = new CompositeDisposable();
    private String moduleTitle = "";
    private long timeLeft;
    private Bundle statsExtras;
    private FragmentSectionedLiveContentBinding binding;
    private View errorView;
    private SectionState currentState;
    private SectionAdapterData currentAdapterData;
    private OverlayThemeProvider themeProvider;
    private VisibilityTracker visibilityTracker;
    private ExpandableListTracker expandableListTracker;
    private boolean reloadOnResume;
    private OfflineBannerCoordinator offlineBannerCoordinator;
    private Bookmarker bookmarker;
    private int originalCollapsingToolbarScrollFlags = -1;

    private final SectionAdapter.Listener adapterListener = new SectionAdapter.Listener() {
        @Override
        public void onUpdate(SectionAdapterData adapterData) {
            currentAdapterData = adapterData;
            if (offlineBannerCoordinator != null) {
                boolean hasContent = adapterData.items.size() > 0;
                Date last;
                if (hasContent) {
                    last = adapterData.lastUpdated;
                }
                else {
                    // TODO adapterData.lastUpdateAttempt;
                    last = new Date();
                }
                if (last != null) {
                    offlineBannerCoordinator.bind(new OfflineBannerModel(last, hasContent));
                }
            }
            updateEmptyContainer();
        }
    };

    private void updateEmptyContainer() {
        if (currentState == SectionState.READY && currentAdapterData != null && currentAdapterData.items.size() == 0 &&
                currentAdapterData.state != SectionState.LOADING && currentAdapterData.state != SectionState.ERROR) {
            binding.emptyContainer.setVisibility(View.VISIBLE);
        } else {
            binding.emptyContainer.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            timeLeft = savedInstanceState.getLong(TIME_LEFT_KEY, 0);
            expandableListTracker = savedInstanceState.getParcelable(EXPANDABLE_SECTION_TRACKER);
        }
        if (expandableListTracker == null) {
            expandableListTracker = new ExpandableListTracker();
        }

        Bundle arguments = getArguments();
        moduleId = arguments.getString("moduleId");
        moduleTitle = arguments.getString("moduleTitle");
        moduleName = arguments.getString("moduleName");
        statsExtras = arguments.getBundle(LiveContentRecyclerviewActivity.STATS_EXTRAS_KEY);
        resourceManager = new ResourceManager(getActivity(), moduleId);
        String packageUuid = getArguments().getString("packageUuid");
        loadConfig();

        Timber.e("SectionedLiveContent, OnCreate ModuleId: %s, moduleTitle: %s, moduleName: %s", moduleId, moduleTitle, moduleName);

        // we dont track article events for grids
        if (config.getGridLayout() == null) {
            visibilityTracker = new ViewTreeObserverVisibilityTracker(getActivity().getWindow().getDecorView().getViewTreeObserver());
        }
        else {
            visibilityTracker = new NoVisibilityTracker();
        }
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
        else if (getArguments().getString("configOverlay") != null) {
            String overlay = getArguments().getString("configOverlay");
            config = ConfigManager.getInstance().getConfig(moduleName, moduleId, SectionedLiveContentUIConfig.class, overlay);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ConfigManager.getInstance().registerOnModuleConfigChangeListener(this);
        ThemeManager.getInstance(getContext()).addOnUpdateListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ConfigManager.getInstance().removeOnModuleConfigChangeListener(this);
        ThemeManager.getInstance(getContext()).removeOnUpdateListener(this);
    }

    @Override
    public void onModuleConfigUpdated(@NotNull Set<String> set) {
        Timber.d("ping");
        if (set.contains(moduleId)) {
            Timber.d("Reloading");
            loadConfig();
            if (isVisible()) {
                pause();
                setupSections();
                resume();
            }
            else {
                reloadOnResume = true;
            }
        }
    }

    private void loadConfig() {
        config = ConfigManager.getInstance(requireActivity().getApplicationContext()).getConfig(moduleName, moduleId, SectionedLiveContentUIConfig.class);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(TIME_LEFT_KEY, System.currentTimeMillis());
        outState.putParcelable(EXPANDABLE_SECTION_TRACKER, expandableListTracker);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSectionedLiveContentBinding.inflate(inflater, container, false);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            adapter.reload();
        });
        binding.recyclerView.setGridLayoutConfig(config.getGridLayout());

        Theme theme = ThemeManager.getInstance(getActivity()).getModuleTheme(moduleId);
        setupLoadingLayout(inflater);
        setupSections();
        setupEmptyView();
        setupErrorView();
        setupOfflineWarningView();
        setupItemDecorations(theme, binding.recyclerView);
        setupPadding(theme);

        if (getActivity() instanceof OfflineBannerOwner && ((OfflineBannerOwner) getActivity()).getOfflineBannerCoordinator() != null) {
            offlineBannerCoordinator = ((OfflineBannerOwner) getActivity()).getOfflineBannerCoordinator();
        }
        else {
            offlineBannerCoordinator = new OfflineBannerCoordinator(binding.offlineBanner, resourceManager);
            getLifecycle().addObserver(offlineBannerCoordinator);
        }

        if (config.getAds() != null) {
            StickyAdsCoordinator stickyAdsCoordinator = new StickyAdsCoordinator(binding.topStickyAdWrapper, requireActivity().findViewById(R.id.bottom_sticky_ad_wrapper), config.getAds().getProvider(), config.getAds().getSticky());
            getLifecycle().addObserver(stickyAdsCoordinator);
        }

        ThemeManager.getInstance(getContext()).getModuleTheme(moduleId).apply(binding.getRoot());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookmarker = new Bookmarker(view, moduleId);
        if (getActivity() != null) {
            BookmarkingResultChannel resultChannel = new ViewModelProvider(getActivity()).get(BookmarkingResultChannel.class);
            garbage.add(resultChannel.getResult().subscribe(bookmarkingResult -> {
                if (bookmarkingResult != null) {
                    bookmarker.showSnackbar(bookmarkingResult.getBookmark(), bookmarkingResult.isBookmarked());
                }
            }));
        }
    }

    private void setupPadding(Theme theme) {
        if (config.getGridLayout() != null) {
            setTopAndBottomPadding(theme, "contentGridPadding", binding.recyclerView, DEFAULT_SPACING);
            setStartAndEndPadding(theme, "contentGridPadding", binding.recyclerView, DEFAULT_SPACING);
        } else {
            setTopAndBottomPadding(theme, "contentListPadding", binding.recyclerView, DEFAULT_SPACING);
        }
    }

    private void setTopAndBottomPadding(Theme theme, String themeKeyPrefix, RecyclerView recyclerView, ThemeSize defaultPadding) {
        int recyclerViewPaddingTop = (int) theme.getSize(themeKeyPrefix + "Top", defaultPadding).getSizePx();
        int recyclerViewPaddingBottom = (int) theme.getSize(themeKeyPrefix + "Bottom", defaultPadding).getSizePx();
        recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerViewPaddingTop, recyclerView.getPaddingRight(), recyclerViewPaddingBottom);
    }

    private void setStartAndEndPadding(Theme theme, String themeKeyPrefix, RecyclerView recyclerView, ThemeSize defaultPadding) {
        int recyclerViewPaddingStart = (int) theme.getSize(themeKeyPrefix + "Start", defaultPadding).getSizePx();
        int recyclerViewPaddingEnd = (int) theme.getSize(themeKeyPrefix + "End", defaultPadding).getSizePx();
        recyclerView.setPadding(recyclerViewPaddingStart, recyclerView.getPaddingTop(), recyclerViewPaddingEnd, recyclerView.getPaddingBottom());
    }

    private void setupItemDecorations(Theme theme, RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new RecyclerViewDividerDecorator(ContentListDividerFactory.create(theme), theme));
        if (config.getGridLayout() == null) {
            recyclerView.addItemDecoration(ContentListItemBoundaryDecoration.create(theme));
            recyclerView.addItemDecoration(new HorizontalOffsetItemDecoration(requireContext(), HorizontalOffsetItemDecoration.VERTICAL_LIST));
        }
        if (config.getShowBookmarkTeaserOverlay()) {
            recyclerView.addItemDecoration(new BookmarkOverlayItemDecoration(recyclerView, theme, getViewLifecycleOwner()));
        }
    }

    private void setupErrorView() {
        errorView = inflateResourceManagedView(binding.errorContainer, "error_default", R.layout.error_default);

        ErrorConfiguration errorConfiguration = config.getErrorConfiguration();
        if (errorConfiguration != null) {

            ThemeableTextView errorHeader = errorView.findViewById(R.id.error_title);
            errorHeader.setText(errorConfiguration.getErrorHeading());

            ThemeableTextView errorMessage = errorView.findViewById(R.id.error_message);
            errorMessage.setText(errorConfiguration.getErrorMessage());
        }
    }

    private void setupEmptyView() {
        inflateResourceManagedView(binding.emptyContainer, "no_articles", R.layout.no_articles);
    }

    private void setupOfflineWarningView() {
        View offlineView = inflateResourceManagedView(binding.offlineWarningContainer, "missing_network", R.layout.offline_warning_default);

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

    private View inflateResourceManagedView(ViewGroup parent, String layoutResourceName, int defaultLayoutIdentifier) {
        Context context = parent.getContext();
        ResourceManager resourceManager = new ResourceManager(context, moduleId);
        int layoutIdentifier = resourceManager.getLayoutIdentifier(layoutResourceName);
        if (layoutIdentifier < 1) {
            layoutIdentifier = defaultLayoutIdentifier;
        }
        return LayoutInflater.from(context).inflate(layoutIdentifier, parent, true);
    }

    private void setupSections() {
        List<Section> sections = SectionManager.getInstance().create(dataSourceProviderFactory.create(config), config, moduleTitle);
        List<Double> imageSizes = null;
        try {
            imageSizes = config.getMedia().getImage().getSizes();
        } catch (Exception e) {
            Timber.e(e, "Failed to get image sizes");
        }

        ImageUrlBuilderFactory imageUrlBuilderFactory = new ImageUrlFactoryProvider().provide(config.getImageProvider(), config.getImageBaseUrl());

        themeProvider = OverlayThemeProvider.forModule(getActivity(), moduleId);

        adapter = SectionAdapter.builder()
                .setVisibilityTracker(visibilityTracker)
                .setSections(sections)
                .setImageUrlFactory(imageUrlBuilderFactory)
                .setModuleId(moduleId)
                .setModuleTitle(moduleTitle)
                .setImageSizes(imageSizes)
                .setResourceManager(resourceManager)
                .setThemeProvider(themeProvider)
                .setOverlayConfig(config.getThemeOverlayMapping())
                .setExtras(getArguments())
                .setTemplates(config.getTemplates())
                .setContentViewConfig(config.getContentViewConfiguration())
                .setPresentationConfig(config.getContentPresentation())
                .setLifecycleOwner(this)
                .setExpandableSectionTracker(expandableListTracker)
                .build();

        binding.recyclerView.setAdapter(adapter);
    }

    private void setupLoadingLayout(LayoutInflater inflater) {
        int layoutIdentifier = R.layout.default_loading_view;
        if (config.getLoadingLayout() != null) {
            layoutIdentifier = resourceManager.getLayoutIdentifier(config.getLoadingLayout());
        }
        if (layoutIdentifier == 0) {
            layoutIdentifier = R.layout.default_loading_view;
        }
        inflater.inflate(layoutIdentifier, binding.loadingView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (reloadOnResume) {
            reloadOnResume = false;
            setupSections();
        }
        resume();
    }

    private void resume() {
        visibilityTracker.resume();
        SectionUpdateNotifier.addListener(this);
        updateEmptyContainer();
        adapter.setListener(adapterListener);
        if (System.currentTimeMillis() - timeLeft > 500) {
            registerStatsEvent();
        }
        resumedDisposables.add(adapter.observeSectionStates().map(sectionStates -> {
            if (sectionStates.length == 0) {
                return SectionState.IDLE;
            }
            boolean allError = true;
            for (SectionState sectionState : sectionStates) {
                allError &= sectionState == SectionState.ERROR;
            }
            if (allError) {
                return SectionState.TOTAL_FAILURE;
            }
            int minProgress = SectionState.ERROR.ordinal();
            for (SectionState sectionState : sectionStates) {
                minProgress = Math.min(minProgress, sectionState.ordinal());
            }
            return SectionState.values()[minProgress];
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(sectionState -> {
            this.currentState = sectionState;
            updateEmptyContainer();
            switch (sectionState) {
                case IDLE: {
                    binding.emptyContainer.setVisibility(View.INVISIBLE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    binding.swipeRefreshLayout.setEnabled(true);
                    binding.loadingView.setVisibility(View.INVISIBLE);
                    binding.recyclerView.setVisibility(View.INVISIBLE);
                    errorView.setVisibility(View.INVISIBLE);
                    break;
                }
                case LOADING: {
                    binding.emptyContainer.setVisibility(View.INVISIBLE);
                    binding.swipeRefreshLayout.setRefreshing(true);
                    binding.swipeRefreshLayout.setEnabled(false);
                    binding.loadingView.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.INVISIBLE);
                    errorView.setVisibility(View.INVISIBLE);
                    binding.offlineWarningContainer.setVisibility(View.INVISIBLE);
                    break;
                }
                case RELOADING: {
                    binding.emptyContainer.setVisibility(View.INVISIBLE);
                    binding.swipeRefreshLayout.setRefreshing(true);
                    binding.swipeRefreshLayout.setEnabled(false);
                    binding.loadingView.setVisibility(View.INVISIBLE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    errorView.setVisibility(View.INVISIBLE);
                    binding.offlineWarningContainer.setVisibility(View.INVISIBLE);
                    break;
                }
                case READY:
                case ERROR: {
                    binding.swipeRefreshLayout.setRefreshing(false);
                    binding.swipeRefreshLayout.setEnabled(true);
                    binding.loadingView.setVisibility(View.INVISIBLE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    errorView.setVisibility(View.INVISIBLE);
                    binding.offlineWarningContainer.setVisibility(View.INVISIBLE);
                    break;
                }
                case TOTAL_FAILURE: {
                    binding.swipeRefreshLayout.setRefreshing(false);
                    binding.swipeRefreshLayout.setEnabled(true);
                    binding.loadingView.setVisibility(View.INVISIBLE);
                    binding.recyclerView.setVisibility(View.INVISIBLE);
                    handleErrorViewVisibility();
                    break;
                }
            }
            handleAppBarScrolling();
        }));
        adapter.resume();
    }

    private void handleErrorViewVisibility() {
        if (ConnectivityUtils.hasInternetConnection(requireContext())) {
            errorView.setVisibility(View.VISIBLE);
        }
        else {
            binding.offlineWarningContainer.setVisibility(View.VISIBLE);
        }
    }

    private void handleAppBarScrolling() {
        if (getActivity() instanceof NavigationChromeOwner) {
            ViewGroup.LayoutParams layoutParams = ((NavigationChromeOwner) getActivity()).getCollapsingToolbarLayout().getLayoutParams();
            if (layoutParams instanceof AppBarLayout.LayoutParams) {
                if (originalCollapsingToolbarScrollFlags == -1) {
                    originalCollapsingToolbarScrollFlags = ((AppBarLayout.LayoutParams) layoutParams).getScrollFlags();
                }
                if (binding.recyclerView.getVisibility() == View.INVISIBLE || binding.recyclerView.getVisibility() == View.GONE) {
                    ((AppBarLayout.LayoutParams) layoutParams).setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
                }
                else {
                    ((AppBarLayout.LayoutParams) layoutParams).setScrollFlags(originalCollapsingToolbarScrollFlags);
                }
                ((NavigationChromeOwner) getActivity()).getCollapsingToolbarLayout().setLayoutParams(layoutParams);
            }
        }
    }

    protected void registerStatsEvent() {
        Timber.d("SectionedLiveContent, registerStatsEvent mModuleId: %s", moduleId);
        Timber.d("ModuleParent: %s", LiveContentRecyclerViewFragment.getModuleParent(moduleId));

        StatisticsEvent.Builder builder = new StatisticsEvent.Builder()
                .viewShow()
                .moduleId(String.valueOf(moduleId))
                .moduleName(LiveContentRecyclerViewFragment.getModuleName(moduleId))
                .moduleTitle(LiveContentRecyclerViewFragment.getModuleTitle(moduleId))
                .parent(LiveContentRecyclerViewFragment.getModuleParent(moduleId))
                .viewName("articleList");
        if (statsExtras != null) {
            for (String key : statsExtras.keySet()) {
                builder.attribute(key, statsExtras.get(key));
            }
        }
        StatisticsManager.getInstance().logEvent(builder.build());


    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    private void pause() {
        visibilityTracker.pause();
        SectionUpdateNotifier.removeListener(this);
        adapter.setListener(null);
        adapter.pause();
        resumedDisposables.clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ITEM_BACK_REQUEST && resultCode == Activity.RESULT_OK) {
            String itemId = data.getStringExtra("itemId");
            if (!TextUtils.isEmpty(itemId)) {
                int position = adapter.positionForId(itemId);
                if (position != -1) {
                    binding.recyclerView.scrollToPosition(position);
                }
            }
        }
    }

    @Override
    public void packageUpdated() {
        adapter.reload();
    }

    @Override
    public void onThemeUpdated() {
        themeProvider.reset();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        garbage.clear();
        super.onDestroyView();
    }

    public void scrollToTop() {
        binding.recyclerView.smoothScrollToPosition(0);
    }
}
