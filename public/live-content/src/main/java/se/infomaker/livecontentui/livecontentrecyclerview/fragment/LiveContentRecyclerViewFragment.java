package se.infomaker.livecontentui.livecontentrecyclerview.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.navigaglobal.mobile.livecontent.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import kotlin.Pair;
import se.infomaker.frt.statistics.StatisticsEvent;
import se.infomaker.frt.statistics.StatisticsManager;
import se.infomaker.frt.ui.fragment.FreeTextFilter;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.frtutilities.NavigationChromeOwner;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.frtutilities.connectivity.Connectivity;
import se.infomaker.frtutilities.runtimeconfiguration.OnModuleConfigChangeListener;
import se.infomaker.iap.action.display.flow.MustacheUtilKt;
import se.infomaker.iap.theme.OnThemeUpdateListener;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.ktx.ThemeUtils;
import se.infomaker.iap.theme.size.ThemeSize;
import se.infomaker.iap.theme.view.ThemeableTextView;
import se.infomaker.iap.ui.theme.OverlayThemeProvider;
import se.infomaker.livecontentmanager.network.AndroidNetworkAvailabilityManager;
import se.infomaker.livecontentmanager.network.NetworkAvailabilityManager;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.query.FilterHelper;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.livecontentmanager.stream.HitsListStream;
import se.infomaker.livecontentmanager.stream.StreamListener;
import se.infomaker.livecontentui.GridRecyclerView;
import se.infomaker.livecontentui.LiveContentStreamProvider;
import se.infomaker.livecontentui.OnPresentationContextChangedListener;
import se.infomaker.livecontentui.ads.StickyAdsCoordinator;
import se.infomaker.livecontentui.bookmark.BookmarkActionBottomSheetFragment;
import se.infomaker.livecontentui.bookmark.BookmarkFeatureFlag;
import se.infomaker.livecontentui.bookmark.BookmarkOverlayItemDecoration;
import se.infomaker.livecontentui.bookmark.Bookmarker;
import se.infomaker.livecontentui.bookmark.BookmarkingResultChannel;
import se.infomaker.livecontentui.config.ErrorConfiguration;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.extensions.PropertyObjectKt;
import se.infomaker.livecontentui.impressions.NoVisibilityTracker;
import se.infomaker.livecontentui.impressions.ViewTreeObserverVisibilityTracker;
import se.infomaker.livecontentui.impressions.VisibilityTracker;
import se.infomaker.livecontentui.livecontentdetailview.activity.ArticlePagerActivity;
import se.infomaker.livecontentui.livecontentrecyclerview.activity.LiveContentRecyclerviewActivity;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.AdWrapperAdapter;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.EndlessRecyclerOnScrollListener;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.HorizontalOffsetItemDecoration;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.LiveContentRecyclerViewAdapter;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.StreamResultMediator;
import se.infomaker.livecontentui.livecontentrecyclerview.decoration.ContentListItemBoundaryDecoration;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilderFactory;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlFactoryProvider;
import se.infomaker.livecontentui.offline.OfflineBannerCoordinator;
import se.infomaker.livecontentui.offline.OfflineBannerLayout;
import se.infomaker.livecontentui.offline.OfflineBannerModel;
import se.infomaker.livecontentui.section.SectionedLiveContentActivity;
import timber.log.Timber;

@AndroidEntryPoint
public class LiveContentRecyclerViewFragment extends Fragment implements StreamListener<PropertyObject>, OnModuleConfigChangeListener, OnThemeUpdateListener, OnPresentationContextChangedListener {

    private static final ThemeSize DEFAULT_SPACING = new ThemeSize(6);
    private static final Object ANIMATION_BLOCKER = new Object();

    static final int ARTICLE_BACK_REQUEST = 1337;
    public static final String TIME_LEFT_KEY = "timeLeftKey";
    public static final String MODULE_NAME = "ContentList";
    public static final String MANUAL_STATISTICS = "manualStatistics";
    public static final String REGISTER_INITAL_STATISTICS = "registerInitialStatistics";

    @Inject LiveContentStreamProvider streamProvider;

    private CompositeDisposable garbage = new CompositeDisposable();
    private Disposable connectivityDisposable;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GridRecyclerView mRecyclerView;
    private ImageView mStartImage;
    private FragmentActivity mFragmentActivity;
    private FrameLayout errorContainer;
    private LinearLayout newMessagesLayout;
    private Bundle statsExtras;
    private FrameLayout emptyContainer;
    private FrameLayout offlineWarningContainer;
    private NetworkAvailabilityManager networkAvailabilityManager;

    private LiveContentUIConfig mConfig;
    private List<QueryFilter> mFilters;
    private ResourceManager mResourceManager;
    private VisibilityTracker visibilityTracker;
    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;
    private AdWrapperAdapter mRecyclerViewAdapter;
    ErrorConfiguration errorConfiguration;
    private HorizontalOffsetItemDecoration mItemDecoration;
    private ImageUrlBuilderFactory imageUrlBuilderFactory;
    private StreamResultMediator streamResultMediator;

    private String mModuleId;
    private String mModuleName;
    private boolean isTotalReloading = false;
    private long timeLeft;
    private boolean manualStatistics;
    private OfflineBannerCoordinator offlineBannerCoordinator;
    private Bookmarker bookmarker;

    private OnItemClickListener mOnArticleClickListener = new OnItemClickListener() {
        @Override
        public void onClick(PropertyObject propertyObject) {
            String contentView = mConfig.getContentView() != null ? mConfig.getContentView() : "nativeArticle";

            switch (contentView) {
                case "SectionedContentList": {
                    Intent intent = new Intent(getActivity(), SectionedLiveContentActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("hitsListListId", mModuleId);
                    bundle.putString("moduleId", mModuleId);
                    bundle.putString("moduleName", mModuleName);
                    bundle.putString("title", getTitleFromArguments());

                    JsonObject contentViewConfiguration = mConfig.getContentViewConfiguration();
                    if (contentViewConfiguration != null) {
                        String overlay = MustacheUtilKt.mustachify(contentViewConfiguration.toString(), new PropertyObjectValueProvider(propertyObject));
                        intent.putExtra("configOverlay", overlay);
                    }

                    intent.putExtras(bundle);
                    requireActivity().startActivity(intent);

                    return;
                }
                case "ContentList": {
                    return;
                }
                default: {
                    if (PropertyObjectKt.isRelated(propertyObject)) {
                        ArticlePagerActivity.openArticle(mFragmentActivity, mModuleId, getTitleFromArguments(), propertyObject.getId());
                    }
                    else {
                        Intent intent = new Intent(mFragmentActivity, ArticlePagerActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("hitsListListId", mModuleId);
                        FilterHelper.put(intent, mFilters);
                        bundle.putString("selectedArticleId", propertyObject.getId());
                        bundle.putString("title", getTitleFromArguments());
                        intent.putExtras(bundle);

                        startActivityForResult(intent, ARTICLE_BACK_REQUEST);
                    }
                }
            }
        }

        @Override
        public boolean onLongClick(PropertyObject item) {
            String contentType = item.optString("contentType", null);
            if (BookmarkFeatureFlag.Companion.isEnabled(requireContext()) && "Article".equals(contentType) && getActivity() != null) {
                BookmarkActionBottomSheetFragment.newInstance(mModuleId, item).show(getParentFragmentManager(), null);
                return true;
            }
            return false;
        }
    };

    private LiveContentRecyclerViewAdapter.OnClickListener mOnRecyclerAdapterViewHolderClickListener = new LiveContentRecyclerViewAdapter.OnClickListener() {
        @Override
        public void onClick(View view, int position) {
            LiveContentRecyclerViewAdapter.ViewHolder viewHolder = (LiveContentRecyclerViewAdapter.ViewHolder) view.getTag();
            if (viewHolder.getItemViewType() >= 99) {
                return;
            }

            if (mOnArticleClickListener != null) {
                mOnArticleClickListener.onClick(mRecyclerViewAdapter.getItem(position));
            }
        }

        @Override
        public boolean onLongClick(View view, int position) {
            LiveContentRecyclerViewAdapter.ViewHolder viewHolder = (LiveContentRecyclerViewAdapter.ViewHolder) view.getTag();
            if (viewHolder.getItemViewType() >= 99) {
                return false;
            }

            if (mOnArticleClickListener != null) {
                return mOnArticleClickListener.onLongClick(mRecyclerViewAdapter.getItem(position));
            }

            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            timeLeft = savedInstanceState.getLong(TIME_LEFT_KEY, 0);
        }
        Bundle arguments = getArguments();
        if (arguments != null) {
            manualStatistics = arguments.getBoolean(MANUAL_STATISTICS, false);

            mModuleId = arguments.getString("moduleId");
            mModuleName = arguments.getString("moduleName", MODULE_NAME);
            statsExtras = arguments.getBundle(LiveContentRecyclerviewActivity.STATS_EXTRAS_KEY);
            mFilters = FilterHelper.getFilters(arguments);

            Timber.e("LiveContentRecyclerViewFragment: OnCreate mModuleId: %s, arguments: %s", mModuleId, arguments);
            
            loadConfig();
            if (savedInstanceState == null && arguments.getBoolean(REGISTER_INITAL_STATISTICS, false)) {
                registerStatsEvent();
            }
        }
        mResourceManager = new ResourceManager(getActivity(), mModuleId);
    }

    private void loadConfig() {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();
        mConfig = ConfigManager.getInstance(requireContext().getApplicationContext()).getConfig(mModuleId, LiveContentUIConfig.class, gson);
        if (mConfig == null) {
            mConfig = ConfigManager.getInstance(requireContext().getApplicationContext()).getConfig(mModuleName, mModuleId, LiveContentUIConfig.class, gson);
        }
        // we dont track article events for grids
        if (mConfig.getGridLayout() == null) {
            visibilityTracker = new ViewTreeObserverVisibilityTracker(requireActivity().getWindow().getDecorView().getViewTreeObserver());
        }
        else {
            visibilityTracker = new NoVisibilityTracker();
        }

        imageUrlBuilderFactory = new ImageUrlFactoryProvider().provide(mConfig.getImageProvider(), mConfig.getImageBaseUrl());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_live_content_recycler_view, container, false);

        mStartImage = fragmentView.findViewById(R.id.start_image);
        mSwipeRefreshLayout = fragmentView.findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = fragmentView.findViewById(R.id.recycler_view);
        newMessagesLayout = fragmentView.findViewById(R.id.newMessages);
        setupEmptyView(fragmentView);
        setupErrorView(fragmentView);
        setupOfflineWarningView(fragmentView);

        OfflineBannerLayout offlineBannerLayout = fragmentView.findViewById(R.id.offline_banner);
        offlineBannerCoordinator = new OfflineBannerCoordinator(offlineBannerLayout, mResourceManager);
        getLifecycle().addObserver(offlineBannerCoordinator);

        if (mConfig.getAds() != null) {
            StickyAdsCoordinator stickyAdsCoordinator = new StickyAdsCoordinator(fragmentView.findViewById(R.id.top_sticky_ad_wrapper), requireActivity().findViewById(R.id.bottom_sticky_ad_wrapper), mConfig.getAds().getProvider(), mConfig.getAds().getSticky());
            getLifecycle().addObserver(stickyAdsCoordinator);
        }

        ThemeManager.getInstance(fragmentView.getContext()).getModuleTheme(mModuleId).apply(fragmentView);

        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookmarker = new Bookmarker(view, mModuleId);
        if (getActivity() != null) {
            BookmarkingResultChannel resultChannel = new ViewModelProvider(getActivity()).get(BookmarkingResultChannel.class);
            garbage.add(resultChannel.getResult().subscribe(bookmarkingResult -> {
                if (bookmarkingResult != null) {
                    bookmarker.showSnackbar(bookmarkingResult.getBookmark(), bookmarkingResult.isBookmarked());
                }
            }));
        }
    }

    private void setupEmptyView(View view) {
        emptyContainer = view.findViewById(R.id.empty_container);
        inflateResourceManagedLayout(emptyContainer, "no_articles", R.layout.no_articles);

        if (mFilters != null) {
            try {
                FreeTextFilter firstFreeTextFilter = null;
                for (QueryFilter filter : mFilters) {
                    if (filter instanceof FreeTextFilter) {
                        firstFreeTextFilter = (FreeTextFilter) filter;
                        break;
                    }
                }
                if (firstFreeTextFilter != null) {
                    View emptyMessageView = emptyContainer.findViewWithTag("empty_message");
                    if (emptyMessageView instanceof TextView) {
                        String emptyMessage = mResourceManager.getString("free_text_search_empty_message", getString(R.string.free_text_search_empty_message), firstFreeTextFilter.getValue());
                        ((TextView) emptyMessageView).setText(emptyMessage);
                    }
                }
            }
            catch (NoSuchElementException e) {
                // ignored
            }
        }
    }

    private void setupErrorView(View view) {
        errorContainer = view.findViewById(R.id.error_container);
        View errorView = inflateResourceManagedLayout(errorContainer, "error_default", R.layout.error_default);

        errorConfiguration = mConfig.getErrorConfiguration();
        if (errorConfiguration != null) {

            ThemeableTextView errorHeader = errorView.findViewById(R.id.error_title);
            errorHeader.setText(errorConfiguration.getErrorHeading());

            ThemeableTextView errorMessage = errorView.findViewById(R.id.error_message);
            errorMessage.setText(errorConfiguration.getErrorMessage());
        }
    }

    private void setupOfflineWarningView(View view) {
        offlineWarningContainer = view.findViewById(R.id.offline_warning_container);
        View offlineView = inflateResourceManagedLayout(offlineWarningContainer, "offline_warning", R.layout.offline_warning_default);

        if (offlineView != null) {
            String offlineWarningTitle = mResourceManager.getString("offline_warning_title", null);
            TextView titleView = offlineView.findViewById(R.id.offline_warning_title);
            if (titleView != null) {
                titleView.setText(offlineWarningTitle);
            }

            String offlineWarningMessage = mResourceManager.getString("offline_warning_message", null);
            TextView messageView = offlineView.findViewById(R.id.offline_warning_message);
            if (messageView != null) {
                messageView.setText(offlineWarningMessage);
            }
        }
    }

    private View inflateResourceManagedLayout(ViewGroup parent, String layoutResourceName, int defaultLayoutIdentifier) {
        Context context = parent.getContext();
        int layoutIdentifier = mResourceManager.getLayoutIdentifier(layoutResourceName);
        if (layoutIdentifier < 1) {
            layoutIdentifier = defaultLayoutIdentifier;
        }
        return LayoutInflater.from(context).inflate(layoutIdentifier, parent, true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(TIME_LEFT_KEY, System.currentTimeMillis());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFragmentActivity = super.getActivity();

        networkAvailabilityManager = new AndroidNetworkAvailabilityManager(requireContext());

        setupStartImage();
        setupSwipeRefreshLayout();
        setupRecyclerView();
        setupRecyclerViewWithConfig();

        if (savedInstanceState == null) {
            mStartImage.setVisibility(View.VISIBLE);
            isTotalReloading = true;
        }

        Theme theme = ThemeManager.getInstance(getActivity()).getModuleTheme(mModuleId);
        GradientDrawable gd = (GradientDrawable) newMessagesLayout.getBackground();
        gd.setColor(ThemeUtils.getBrandColor(theme).get());
        if (!TextUtils.isEmpty(mConfig.getLiveContentNewEventsTitle())) {
            ((TextView) newMessagesLayout.findViewById(R.id.newEventsText)).setText(mConfig.getLiveContentNewEventsTitle());
        }
        newMessagesLayout.findViewById(R.id.newMessagesClickArea).setOnClickListener(v -> {
            scrollToTop();
            // TODO Reset new messages?
            newMessagesLayout.setVisibility(View.INVISIBLE);
        });
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);
        mSwipeRefreshLayout.setEnabled(true);
    }

    private void refresh() {
        newMessagesLayout.setVisibility(View.INVISIBLE);
        mSwipeRefreshLayout.setRefreshing(true);
        isTotalReloading = true;
        mRecyclerViewAdapter.freeze();
        getStream().reset();
    }

    private void setupRecyclerView() {
        mRecyclerView.setGridLayoutConfig(mConfig.getGridLayout());
        mEndlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mRecyclerView.getLayoutManager()) {
            @Override
            public void onLoadMore(int totalItemCount) {
                Timber.d("onLoadMore %s", totalItemCount);
                if (!isTotalReloading) {
                    getStream().searchMore();
                }
            }

            @Override
            public void onTopPosition() {
                //Only animate and hide if not already animating or hidden
                if (newMessagesLayout.getAnimation() == null
                        && newMessagesLayout.getVisibility() != View.INVISIBLE) {
                    Animation anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            newMessagesLayout.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    newMessagesLayout.startAnimation(anim);
                }
            }
        };

        Theme theme = ThemeManager.getInstance(getActivity()).getModuleTheme(mModuleId);

        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
        mRecyclerView.setHasFixedSize(true);

        if (mRecyclerViewAdapter != null) {
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            if (mItemDecoration != null) {
                mRecyclerView.removeItemDecoration(mItemDecoration);
            }
            mItemDecoration = new HorizontalOffsetItemDecoration(mFragmentActivity,
                    HorizontalOffsetItemDecoration.VERTICAL_LIST);
            mRecyclerView.addItemDecoration(mItemDecoration);
        }
        if (mConfig.getGridLayout() != null) {
            setTopAndBottomPadding(theme, "contentGridPadding", DEFAULT_SPACING);
            setStartAndEndPadding(theme, "contentGridPadding", DEFAULT_SPACING);
        }
        else {
            setTopAndBottomPadding(theme, "contentListPadding", new ThemeSize(0));

            mRecyclerView.addItemDecoration(ContentListItemBoundaryDecoration.create(theme));
        }
        if (mConfig.getShowBookmarkTeaserOverlay()) {
            mRecyclerView.addItemDecoration(new BookmarkOverlayItemDecoration(mRecyclerView, theme, getViewLifecycleOwner()));
        }
    }

    private void setTopAndBottomPadding(Theme theme, String themeKeyPrefix, ThemeSize defaultPadding) {
        int recyclerViewPaddingTop = (int) theme.getSize(themeKeyPrefix + "Top", defaultPadding).getSizePx();
        int recyclerViewPaddingBottom = (int) theme.getSize(themeKeyPrefix + "Bottom", defaultPadding).getSizePx();
        mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), recyclerViewPaddingTop, mRecyclerView.getPaddingRight(), recyclerViewPaddingBottom);
    }

    private void setStartAndEndPadding(Theme theme, String themeKeyPrefix, ThemeSize defaultPadding) {
        int recyclerViewPaddingStart = (int) theme.getSize(themeKeyPrefix + "Start", defaultPadding).getSizePx();
        int recyclerViewPaddingEnd = (int) theme.getSize(themeKeyPrefix + "End", defaultPadding).getSizePx();
        mRecyclerView.setPadding(recyclerViewPaddingStart, mRecyclerView.getPaddingTop(), recyclerViewPaddingEnd, mRecyclerView.getPaddingBottom());
    }

    private void setupStartImage() {
        mStartImage.setPadding((int) getResources().getDimension(R.dimen.side_padding), 0, (int) getResources().getDimension(R.dimen.side_padding), 0);
        int startImageId = mResourceManager.getDrawableIdentifier("contentlist_start");
        if (startImageId != 0) {
            Glide.with(requireContext())
                    .load(startImageId)
                    .into(mStartImage);
        }
        mStartImage.setVisibility(View.GONE);
    }

    private void setupRecyclerViewWithConfig() {
        // Setup LiveContentRecyclerAdapter (this needs to be set after we setup the manager)
        List<Double> imageSizes = null;
        try {
            imageSizes = mConfig.getMedia().getImage().getSizes();
        }
        catch (Exception e) {
            Timber.e(e);
        }

        OverlayThemeProvider themeProvider = OverlayThemeProvider.forModule(getActivity(), mModuleId);

        streamResultMediator = new StreamResultMediator(getStream().getItems());

        LiveContentRecyclerViewAdapter liveContentRecyclerViewAdapter = new LiveContentRecyclerViewAdapter(visibilityTracker,
                mFragmentActivity, mModuleId, imageUrlBuilderFactory, imageSizes,
                mOnRecyclerAdapterViewHolderClickListener, themeProvider, mConfig, statsExtras, this, streamResultMediator.observe(), this);

        mRecyclerViewAdapter = new AdWrapperAdapter(mFragmentActivity, mModuleId, getModuleTitle(mModuleId), mConfig.getAds(), liveContentRecyclerViewAdapter, themeProvider, this);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerViewAdapter.articlesUpdated();

        if (mItemDecoration != null) {
            mRecyclerView.removeItemDecoration(mItemDecoration);
        }
        if (mConfig.getGridLayout() == null) {
            mItemDecoration = new HorizontalOffsetItemDecoration(mFragmentActivity, HorizontalOffsetItemDecoration.VERTICAL_LIST);
            mRecyclerView.addItemDecoration(mItemDecoration);
        }
    }

    @Override
    public void onItemsAdded(int i, List<PropertyObject> list) {
        Timber.d("onItemsAdded");
        Pair<Integer, List<PropertyObject>> result = streamResultMediator.add(list, i);
        messageSearchReceived(result.getFirst(), result.getSecond());
    }

    @Override
    public void onItemsRemoved(final List<PropertyObject> items) {
        Timber.d("onItemsRemoved");
        if (mFragmentActivity == null) {
            return;
        }
        streamResultMediator.remove(items);
        mFragmentActivity.runOnUiThread(() -> {
            if (mRecyclerViewAdapter != null) {
                mRecyclerViewAdapter.articlesUpdated();
                updateEmptyView();
                updateOfflineBanner();
            }
        });
    }

    @Override
    public void onItemsChanged(List<PropertyObject> items) {
        Timber.d("onItemsChanged");
        if (mFragmentActivity == null) {
            return;
        }
        streamResultMediator.change(items);
        mFragmentActivity.runOnUiThread(() -> {
            if (mRecyclerViewAdapter != null) {
                mRecyclerViewAdapter.notifyDataSetChanged();
                updateEmptyView();
                updateOfflineBanner();
            }
        });
    }

    @Override
    public void onEndReached() {
        mFragmentActivity.runOnUiThread(() -> new Handler().postDelayed(() -> {
            mRecyclerViewAdapter.setReachedEnd(true);
            mRecyclerViewAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            mStartImage.setVisibility(View.GONE);
            updateEmptyView();
        }, 100));
    }

    private void updateOfflineBanner() {
        if (offlineBannerCoordinator != null) {
            boolean hasContent = getStream().size() > 0;
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

    private void updateEmptyView() {
        if (getStream().size() == 0 && getStream().hasReachedEnd() && !getStream().hasError()) {
            emptyContainer.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mStartImage.setVisibility(View.GONE);
            expandNavigationChrome();
        }
        else {
            emptyContainer.setVisibility(View.GONE);
            hideErrorViews();
        }
    }

    @Override
    public void onReset() {
        mFragmentActivity.runOnUiThread(() -> {
            updateEmptyView();
            newMessagesLayout.setVisibility(View.INVISIBLE);
            streamResultMediator.reset();

            mSwipeRefreshLayout.setRefreshing(true);
            if (!isTotalReloading) {
                if (mRecyclerView != null) {
                    mRecyclerViewAdapter.articlesUpdated();
                }
                if (mEndlessRecyclerOnScrollListener != null) {
                    mEndlessRecyclerOnScrollListener.reset();
                }
            }
        });
    }

    @Override
    public void onError(Exception exception) {
        Timber.d(exception);
        mFragmentActivity.runOnUiThread(() -> {
            if (getStream().size() == 0) {
                showError();
            }
            else {
                hideErrorViews();
            }
            mSwipeRefreshLayout.setRefreshing(false);
            updateOfflineBanner();
        });
    }

    private void showError() {
        mStartImage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.INVISIBLE);

        if (networkAvailabilityManager.hasNetwork()) {
            offlineWarningContainer.setVisibility(View.INVISIBLE);
            errorContainer.setVisibility(View.VISIBLE);
        }
        else {
            errorContainer.setVisibility(View.INVISIBLE);
            offlineWarningContainer.setVisibility(View.VISIBLE);
        }

        expandNavigationChrome();
    }

    private void hideErrorViews() {
        mRecyclerView.setVisibility(View.VISIBLE);
        errorContainer.setVisibility(View.INVISIBLE);
        offlineWarningContainer.setVisibility(View.INVISIBLE);
    }

    private void expandNavigationChrome() {
        if (isResumed() && getActivity() instanceof NavigationChromeOwner) {
            ((NavigationChromeOwner) getActivity()).expandNavigationChrome();
        }
    }

    private HitsListStream getStream() {
        return streamProvider.provide(mConfig.getLiveContent(), mConfig.getProperties(), getFilters());
    }

    private void messageSearchReceived(final int location, final List<PropertyObject> articles) {
        if (mFragmentActivity == null) {
            return;
        }

        mFragmentActivity.runOnUiThread(() -> {
            /*
             * Articles are already present in the stream the + 1 is the loading indicator the adapter provides
             */
            if (!isTotalReloading && location == 0 && mRecyclerViewAdapter.getRealItemCount() > articles.size() + 1) {
                newMessageReceived(articles);
                return;
            }

            if (isTotalReloading) {
                if (mRecyclerView != null && mRecyclerViewAdapter != null) {
                    mRecyclerViewAdapter.unfreeze();
                    mRecyclerViewAdapter.reset();
                    mRecyclerViewAdapter.articlesUpdated();
                }

                isTotalReloading = false;

                if (mEndlessRecyclerOnScrollListener != null) {
                    mEndlessRecyclerOnScrollListener.reset();
                }
            }
            else {
                if (mRecyclerViewAdapter != null) {
                    mRecyclerViewAdapter.articlesInserted(location, articles);
                }
            }

            if (getStream().hasReachedEnd()) {
                mRecyclerViewAdapter.setReachedEnd(true);
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mStartImage.setVisibility(View.GONE);
            updateEmptyView();
            updateOfflineBanner();
        });
    }

    private void newMessageReceived(final List<PropertyObject> articles) {
        if (mFragmentActivity == null) {
            return;
        }
        mFragmentActivity.runOnUiThread(() -> {
            if (mRecyclerViewAdapter != null) {
                mRecyclerViewAdapter.articlesInserted(0, articles);
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (!isTotalReloading && articles.size() > 0) {
                newMessagesLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        ConfigManager.getInstance().registerOnModuleConfigChangeListener(this);
        ThemeManager.getInstance(getContext()).addOnUpdateListener(this);

        if (getStream().size() > 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mStartImage.setVisibility(View.GONE);
            isTotalReloading = false;
        }
        getStream().addListener(this);
        connectivityDisposable = Connectivity.INSTANCE.observable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connected -> {
                    if (connected) {
                        refresh();
                    }
                }, Timber::e);
    }

    @Override
    public void onResume() {
        super.onResume();
        visibilityTracker.resume();
        if (!manualStatistics && System.currentTimeMillis() - timeLeft > 500) {
            registerStatsEvent();
        }
        updateEmptyView();
        updateOfflineBanner();
        if (getStream().hasError() && getStream().size() == 0) {
            showError();
        }
        else {
            hideErrorViews();
        }
    }

    @Override
    public void onPause() {
        visibilityTracker.pause();
        super.onPause();
    }

    @Override
    public void onStop() {
        getStream().removeListener(this);
        ConfigManager.getInstance().removeOnModuleConfigChangeListener(this);
        ThemeManager.getInstance(getContext()).removeOnUpdateListener(this);
        if (connectivityDisposable != null) {
            connectivityDisposable.dispose();
        }
        super.onStop();
    }

    public void registerStatsEvent() {
        Timber.e("LiveContentRecyclerViewFragment, registerStatsEvent mModuleId: %s", mModuleId);
        Timber.d("ModuleParent: %s", getModuleParent(mModuleId));

        StatisticsEvent.Builder builder = new StatisticsEvent.Builder()
                .viewShow()
                .moduleId(String.valueOf(mModuleId))
                .moduleName(getModuleName(mModuleId))
                .moduleTitle(getModuleTitle(mModuleId))
                .parent(getModuleParent(mModuleId))
                .viewName("articleList");
        if (statsExtras != null) {
            for (String key : statsExtras.keySet()) {
                builder.attribute(key, statsExtras.get(key));
            }
        }
        StatisticsManager.getInstance().logEvent(builder.build());
    }

    public static String getModuleName(String moduleId) {
        return ModuleInformationManager.getInstance().getModuleName(moduleId);
    }

    public static String getModuleTitle(String moduleId) {
        return ModuleInformationManager.getInstance().getModuleTitle(moduleId);
    }

    public static String getModuleParent(String moduleId) {
        return ModuleInformationManager.getInstance().getModuleParent(moduleId);
    }

    private String getTitleFromArguments() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            String title = arguments.getString("title");
            if (title != null) {
                return title;
            }
        }
        return "";
    }

    public void scrollToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.articlesUpdated();
        }
        if (requestCode == ARTICLE_BACK_REQUEST && resultCode == Activity.RESULT_OK) {
            String articleId = data.getStringExtra("itemId");
            if (!TextUtils.isEmpty(articleId)) {
                int position = mRecyclerViewAdapter.articlePosition(articleId);
                if (position != -1) {
                    mRecyclerView.scrollToPosition(position);
                }
            }
        }
    }

    public List<QueryFilter> getFilters() {
        return mFilters;
    }

    @Override
    public void onModuleConfigUpdated(@NotNull Set<String> set) {
        if (set.contains(mModuleId)) {
            loadConfig();
            setupRecyclerViewWithConfig();
            refresh();
        }
    }

    @Override
    public void onThemeUpdated() {
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        garbage.clear();
        super.onDestroyView();
    }

    @Override
    public void onPresentationContextChanged(@NonNull Map<String, JSONObject> changes) {
        if (changes.size() > 0) {
            Set<String> changedItems = changes.keySet();
            for (String changedItemId : changedItems) {
                int position = mRecyclerViewAdapter.articlePosition(changedItemId);
                if (position > -1) {
                    mRecyclerViewAdapter.notifyItemChanged(position, ANIMATION_BLOCKER);
                }
            }
        }
    }

    public GridRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public interface OnItemClickListener {
        void onClick(PropertyObject item);

        boolean onLongClick(PropertyObject item);
    }
}
