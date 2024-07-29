package se.infomaker.livecontentui.livecontentdetailview.pageadapters;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import se.infomaker.datastore.Bookmark;
import se.infomaker.datastore.DatabaseSingleton;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.frtutilities.TextUtils;
import se.infomaker.frtutilities.connectivity.Connectivity;
import se.infomaker.frtutilities.ktx.ContextUtils;
import se.infomaker.frtutilities.view.ScrollingOffsetCounterFrameLayout;
import se.infomaker.iap.articleview.view.ContentFragment;
import se.infomaker.iap.articleview.view.FocusState;
import se.infomaker.iap.provisioning.ProvisioningManagerProvider;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.ui.theme.OverlayThemeProvider;
import se.infomaker.livecontentmanager.parser.PropertyObject;

import com.navigaglobal.mobile.livecontent.R;

import se.infomaker.livecontentui.AccessManager;
import se.infomaker.livecontentui.StatsHelper;
import se.infomaker.livecontentui.bookmark.Bookmarker;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.config.SharingConfig;
import se.infomaker.livecontentui.impressions.ArticleReadMonitor;
import se.infomaker.livecontentui.section.detail.Resetable;
import se.infomaker.livecontentui.sharing.SharingManager;
import timber.log.Timber;

@AndroidEntryPoint
public class UpdatableContentFragment extends ContentFragment implements OnPropertyObjectUpdated, Resetable {

    private static final String UUID_KEY = "uuid";

    @Inject
    SharingManager sharingManager;

    private String uuid;
    private View contentView;
    private View overlayView;
    private ContentLoadingProgressBar progressView;
    private final CompositeDisposable viewDisposables = new CompositeDisposable();
    private final CompositeDisposable shareDisposable = new CompositeDisposable();
    private ArticleReadMonitor articleReadMonitor;
    private String sharingUrl;
    private String shareTitle;
    private Bookmarker bookmarker;
    private boolean gotBookmarkStatus = false;
    private boolean isBookmarked = false;
    private ResourceManager resourceManager;
    private AccessManager accessManager;
    Drawable bookmarkEnabled;
    Drawable bookmarkDisabled;
    private Boolean isFragmentVisible = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resourceManager = new ResourceManager(requireContext(), getModuleId());
        bookmarkEnabled = AppCompatResources.getDrawable(requireContext(), bookmarkActionFilledResource());
        bookmarkDisabled = AppCompatResources.getDrawable(requireContext(), bookmarkActionOutlinedResource());
        if (getArguments() != null) {
            uuid = getArguments().getString(UUID_KEY);
        }
        setHasOptionsMenu(true);
        fetchShareUrl();
    }

    private void fetchShareUrl() {
        LiveContentUIConfig config = ConfigManager.getInstance(requireContext()).getConfig(getModuleId(), LiveContentUIConfig.class);

        SharingConfig sharing = config.getSharing();
        if (sharing == null || TextUtils.isEmpty(sharing.getShareApiUrl())) {
            return;
        }

        /*
         Try to re-fetch share share url every time the connectivity changes until we get a url
         */
        Observable<String> contentIdObservable = getRelay().map(jsonObject -> JSONUtil.getJSONArray(jsonObject, "contentId")).filter(jsonArray -> (jsonArray.length() > 0))
                .map(array -> array.getString(0)).distinctUntilChanged();
        Observable<String> combineLatest = Observable.combineLatest(contentIdObservable, Connectivity.observable(), (contentId, aBoolean) -> contentId);
        shareDisposable.add(combineLatest.subscribeOn(Schedulers.io())
                .flatMap(contentId -> {
                    Timber.d("Fetching share url for %s", contentId);
                    return sharingManager.getSharingUrl(contentId);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(response -> !TextUtils.isEmpty(response.getUrl()))
                .subscribe(response -> {
                            this.sharingUrl = response.getUrl();
                            Timber.d("Share url updated %s", sharingUrl);
                            shareDisposable.clear();
                            safeInvalidateOptionsMenu();
                        }
                        , error -> {
                            Timber.w(error, "Failed to get share url");
                        }));
    }

    private void safeInvalidateOptionsMenu() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        isFragmentVisible = menuVisible;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!isFragmentVisible) return;
        MenuItem bookmarkMenuItem = menu.findItem(R.id.menu_item_bookmark);
        if (bookmarkMenuItem != null) {
            setButtonTint(bookmarkMenuItem, false);
            if (gotBookmarkStatus) {
                bookmarkMenuItem.setIcon(isBookmarked ? bookmarkEnabled : bookmarkDisabled);
                setButtonTint(bookmarkMenuItem, true);
                bookmarkMenuItem.setVisible(true);
            }
        }

        MenuItem shareMenuItem = menu.findItem(R.id.menu_item_share);
        if (shareMenuItem != null) {
            loadCustomIcon(menu, R.id.menu_item_share, "action_share");
            if (sharingUrl == null) {
                shareMenuItem.setEnabled(false);
                setButtonTint(shareMenuItem, false);
            } else {
                shareMenuItem.setEnabled(true);
                setButtonTint(shareMenuItem, true);
            }
            shareMenuItem.setVisible(true);
        }
    }

    private int bookmarkActionFilledResource() {
        return resourceWithFallback("action_bookmark_filled", R.drawable.ic_bookmark);
    }

    private int bookmarkActionOutlinedResource() {
        return resourceWithFallback("action_bookmark_outlined", R.drawable.ic_bookmark_outlined);
    }

    private int resourceWithFallback(String resourceName, int fallback) {
        int identifier = resourceManager.getDrawableIdentifier(resourceName);
        return identifier != 0 ? identifier : fallback;
    }

    private void loadCustomIcon(Menu menu, int id, String resourceName) {
        int drawableIdentifier = resourceManager.getDrawableIdentifier(resourceName);
        if (drawableIdentifier != 0) {
            MenuItem item = menu.findItem(id);
            if (item != null) {
                item.setIcon(AppCompatResources.getDrawable(requireContext(), drawableIdentifier));
            }
        }
    }

    private void setButtonTint(MenuItem menuItem, boolean enabled) {
        Drawable itemIcon = menuItem.getIcon();
        if (itemIcon != null) {
            itemIcon.mutate();
            Theme theme = OverlayThemeProvider.forModule(getActivity(), getModuleId()).getTheme(getThemeOverlays());
            itemIcon.setColorFilter(theme.getColor("toolbarAction", ThemeColor.WHITE).get(), PorterDuff.Mode.SRC_ATOP);
            itemIcon.setAlpha(255);
            if (!enabled) {
                itemIcon.setAlpha(128);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @NotNull
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getAccessManager().isAllContentAccessible()) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            articleReadMonitor = new ArticleReadMonitor(getRecyclerView(), getContentRelay(), (article) -> {
                StatsHelper.logArticleReadStatsEvent(article, getModuleId());
                return null;
            });
            return view;
        }

        ScrollingOffsetCounterFrameLayout wrapper = new ScrollingOffsetCounterFrameLayout(container.getContext());
        wrapper.setLayoutTransition(new LayoutTransition());
        wrapper.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        progressView = new ContentLoadingProgressBar(container.getContext());
        contentView = super.onCreateView(inflater, wrapper, savedInstanceState);
        overlayView = new FrameLayout(container.getContext());
        overlayView.setId(R.id.paywallOverlay);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        overlayView.setLayoutParams(params);

        wrapper.addView(contentView);
        wrapper.addView(progressView);
        wrapper.addViewAsCounterOffset(overlayView);

        viewDisposables.add(getAccessObservable()
                .distinctUntilChanged()
                .subscribe(showContent -> {
                    if (showContent) {
                        articleReadMonitor = new ArticleReadMonitor(getRecyclerView(), getContentRelay(), (article) -> {
                            StatsHelper.logArticleReadStatsEvent(article, getModuleId());
                            return null;
                        });
                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                            articleReadMonitor.resume();
                        }
                        overlayView.setVisibility(View.INVISIBLE);
                    }
                    else {
                        ResourceManager resourceManager = new ResourceManager(overlayView.getContext(), getModuleId());
                        int paywallHeader = resourceManager.getLayoutIdentifier("paywall_header");
                        if (paywallHeader == 0) {
                            paywallHeader = R.layout.gradient_header;
                        }
                        Fragment paywallFragment = ProvisioningManagerProvider.INSTANCE.provide(container.getContext()).createPaywallFragment(ContextUtils.requireActivity(container.getContext()), paywallHeader);
                        getChildFragmentManager().beginTransaction().replace(R.id.paywallOverlay, paywallFragment).commit();
                        overlayView.setVisibility(View.VISIBLE);
                        overlayView.setOnTouchListener((v, event) -> true);
                    }
                    progressView.hide();
                }, throwable -> Timber.e(throwable, "Unable to figure out if content is premium")));

        return wrapper;
    }

    private synchronized AccessManager getAccessManager() {
        if (accessManager == null) {
            accessManager = new AccessManager(requireContext(), getModuleId());
        }
        return accessManager;
    }

    @NotNull
    private Observable<Boolean> getAccessObservable() {
        return getAccessManager().observeAccess(getRelay().map(article -> new PropertyObject(article, JSONUtil.getString(article, "contentId"))));
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookmarker = new Bookmarker(view, getModuleId());
        DatabaseSingleton.getDatabaseInstance().bookmarkDao().get(uuid).observe(getViewLifecycleOwner(), bookmark -> {
            gotBookmarkStatus = true;
            isBookmarked = bookmark != null;
            safeInvalidateOptionsMenu();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (articleReadMonitor != null) {
            articleReadMonitor.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (articleReadMonitor != null) {
            articleReadMonitor.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewDisposables.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shareDisposable.clear();
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, sharingUrl);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_share) {
            StatsHelper.logShareEvent(new PropertyObject(getProperties(), uuid), sharingUrl, getModuleId());
            startActivity(Intent.createChooser(createShareIntent(), getString(R.string.share_with)));
            return true;
        } else if (item.getItemId() == R.id.menu_item_bookmark) {
            if (uuid != null && getProperties() != null) {
                Date publicationDate = Bookmark.getPublicationDate(getProperties());
                long pubDate = publicationDate != null ? publicationDate.getTime() : System.currentTimeMillis();
                Bookmark bookmark = new Bookmark(uuid, getProperties(), getModuleId(), false, pubDate);
                if (isBookmarked) {
                    bookmarker.delete(bookmark);
                } else {
                    bookmarker.insert(bookmark);
                }
                bookmarker.showSnackbar(bookmark, !isBookmarked);
                return true;
            }
            Timber.w("Could not create bookmark for article: %s, with properties: %s", uuid, getProperties());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public @NotNull Observable<FocusState> getFocusState() {
        if (getAccessManager().isAllContentAccessible()) {
            return super.getFocusState();
        }

        Observable<FocusState> accessState = getAccessObservable().map(showContent -> showContent ? FocusState.IN_FOCUS : FocusState.BLOCKED);
        return Observable.combineLatest(super.getFocusState(), accessState, (resumed, accessible) -> resumed.ordinal() > accessible.ordinal() ? resumed : accessible);
    }

    public static UpdatableContentFragment newInstance(String moduleId, String moduleName, JSONObject properties, List<String> themeOverlays) {
        return newInstance(moduleId, moduleName, properties, themeOverlays, null);
    }

    public static UpdatableContentFragment newInstance(String moduleId, String moduleName, JSONObject properties, List<String> themeOverlays, JSONObject presentationContext) {
        return newInstance(moduleId, moduleName, properties, themeOverlays, presentationContext, null);
    }

    public static UpdatableContentFragment newInstance(String moduleId, String moduleName, JSONObject properties, List<String> themeOverlays, JSONObject presentationContext, String uuid) {
        UpdatableContentFragment updatableContentFragment = new UpdatableContentFragment();
        ContentFragment.addAttributes(updatableContentFragment, moduleId, moduleName, properties, themeOverlays, presentationContext);
        if (updatableContentFragment.getArguments() != null) {
            updatableContentFragment.getArguments().putString(UUID_KEY, uuid);
        }
        return updatableContentFragment;
    }

    @Override
    public void onObjectUpdated(PropertyObject object) {
        update(object.getProperties());
    }

    @Override
    public void reset() {
        RecyclerView recyclerView = getRecyclerView();
        if (recyclerView != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(0, 0);
            }
        }
    }
}
