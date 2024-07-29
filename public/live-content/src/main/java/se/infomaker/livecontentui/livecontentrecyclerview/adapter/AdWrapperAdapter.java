package se.infomaker.livecontentui.livecontentrecyclerview.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.size.ThemeSize;
import se.infomaker.iap.ui.theme.ThemeProvider;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import com.navigaglobal.mobile.livecontent.R;
import se.infomaker.livecontentui.PropertyObjectItemProvider;
import se.infomaker.livecontentui.config.AdsConfig;
import se.infomaker.livecontentui.extensions.PropertyObjectKt;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.AdBinder;
import timber.log.Timber;

public class AdWrapperAdapter extends RecyclerView.Adapter implements PropertyObjectItemProvider {

    private static final int VIEW_TYPE_AD = 100;
    private static final String ARTICLE_TYPE_ID = "article";
    private static final String RELATED_TYPE_ID = "related";
    public static final String AD_LAYOUT_ID = "ad";
    public static final int MAX_NOTIFY_RETRY_COUNT = 5;
    public static final ThemeSize DEFAULT_AD_PADDING = new ThemeSize(8);
    private final AdBinder mBinder;
    private final ThemeProvider themeProvider;
    private final LiveContentRecyclerViewAdapter mLiveContentRecyclerViewAdapter;
    private final List<Object> backingItems = new ArrayList<>();
    private final List<Object> items = new ArrayList<>();
    // Cache adOffset and index for buttery smooth scrolling
    private final List<Integer> adOffsetCache = new ArrayList<>();
    private final List<Integer> indexCache = new ArrayList<>();
    @Nullable
    private AdInsertHelper adInsertHelper;
    private Handler uiThreadHandler = null;
    private List<String> adBlockers = null;
    private final ResourceManager resourceManager;

    public AdWrapperAdapter(Context context, String moduleId, String moduleTitle, @Nullable AdsConfig adsConfig, LiveContentRecyclerViewAdapter liveContentRecyclerViewAdapter, ThemeProvider themeProvider, LifecycleOwner lifecycleOwner) {
        this.themeProvider = themeProvider;
        this.resourceManager = new ResourceManager(context, moduleId);
        mLiveContentRecyclerViewAdapter = liveContentRecyclerViewAdapter;
        mBinder = new AdBinder(context, adsConfig, moduleTitle, lifecycleOwner);
        if (adsConfig != null) {
            adBlockers = new ArrayList<>();
            adBlockers.add(RELATED_TYPE_ID);
            adInsertHelper = new AdInsertHelper(adsConfig.getStartIndex(), adsConfig.getDistanceMin(), adsConfig.getDistanceMax(), adsConfig.getProviderConfiguration(), ARTICLE_TYPE_ID, adBlockers);
        }
    }

    private synchronized void updateAdOffsetCacheAndArticleIndex() {
        updateItems();
        adOffsetCache.clear();
        indexCache.clear();
        int offset = 0;
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (isKnownType(item)) {
                indexCache.add(i);
            }
            else {
                offset++;
            }
            adOffsetCache.add(offset);
        }
    }

    private void updateItems() {
        items.clear();
        for (Object item : backingItems) {
            if (!(item instanceof AdPosition) || !((AdPosition) item).isFailure()) {
                items.add(item);
            }
        }
    }

    private boolean isKnownType(Object item) {
        if (ARTICLE_TYPE_ID.equals(item)) {
            return true;
        }
        if (adBlockers != null) {
            for (String adBlocker : adBlockers) {
                if (adBlocker != null && adBlocker.equals(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_AD) {
            int layoutIdentifier = resourceManager.getLayoutIdentifier(AD_LAYOUT_ID);
            ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(layoutIdentifier, parent, false);
            AdWrapperAdapter.ViewHolder viewHolder = new ViewHolder(viewGroup);
            viewHolder.adFrame = viewGroup.findViewById(R.id.adFrame);
            int padding = (int) themeProvider.getTheme().getSize("adPadding", DEFAULT_AD_PADDING).getSizePx();
            viewGroup.findViewById(R.id.adText).setPadding(padding, padding, padding, padding);
            return viewHolder;
        }

        return mLiveContentRecyclerViewAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_AD) {
            if (items.size() > position) {
                Object item = items.get(position);
                if (!isKnownType(item)) {
                    if (holder instanceof ViewHolder && item instanceof AdPosition) {
                        ViewHolder viewHolder = (ViewHolder) holder;
                        AdPosition adPosition = (AdPosition) item;
                        mBinder.bindAd(viewHolder, adPosition, getAdjacentContent(position), () -> {
                            Timber.d("Ad failed to load at list position: " + position + ", marking as failed and notifying.");
                            adPosition.markFailed();
                            updateAdOffsetCacheAndArticleIndex();
                            notifyItemRemoved(position);
                        });
                        if (viewHolder.publisherAdView == null) {
                            viewHolder.itemView.setVisibility(View.GONE);
                            viewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                        }
                        else {
                            viewHolder.itemView.setVisibility(View.VISIBLE);
                            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        }
                    }
                    Theme overlayedTheme = themeProvider.getTheme();
                    overlayedTheme.apply(holder.itemView);
                }
            }
        } else {
            if (holder instanceof LiveContentRecyclerViewAdapter.ViewHolder) {
                mLiveContentRecyclerViewAdapter.onBindViewHolder((LiveContentRecyclerViewAdapter.ViewHolder) holder, getPositionWithoutAdOffset(position));
            }
        }
    }

    private List<JSONObject> getAdjacentContent(int position) {
        List<JSONObject> adjacentContent = new ArrayList<>();
        if (position > 0 && getItemViewType(position - 1) != VIEW_TYPE_AD) {
            int positionWithoutAdOffset = getPositionWithoutAdOffset(position - 1);
            adjacentContent.add(mLiveContentRecyclerViewAdapter.getItem(positionWithoutAdOffset).getProperties());
        }
        if (getItemCount() > position  + 1 && getItemViewType(position + 1) != VIEW_TYPE_AD) {
            int positionWithoutAdOffset = getPositionWithoutAdOffset(position + 1);
            // -1 as the item count includes a view saying that there is no more items.
            if (mLiveContentRecyclerViewAdapter.getItemCount() - 1 > positionWithoutAdOffset) {
                adjacentContent.add(mLiveContentRecyclerViewAdapter.getItem(positionWithoutAdOffset).getProperties());
            }
        }
        return adjacentContent;
    }

    public String itemId(int position) {
        return mLiveContentRecyclerViewAdapter.itemId(getPositionWithoutAdOffset(position));
    }

    public PropertyObject getItem(int position) {
        return mLiveContentRecyclerViewAdapter.getItem(getPositionWithoutAdOffset(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.size() > position) {
            Object item = items.get(position);
            if (!isKnownType(item)) {
                return VIEW_TYPE_AD;
            }
        }
        return mLiveContentRecyclerViewAdapter.getItemViewType(getPositionWithoutAdOffset(position));
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof LiveContentRecyclerViewAdapter.ViewHolder) {
            mLiveContentRecyclerViewAdapter.onViewRecycled((LiveContentRecyclerViewAdapter.ViewHolder) holder);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof LiveContentRecyclerViewAdapter.ViewHolder) {
            mLiveContentRecyclerViewAdapter.onViewAttachedToWindow((LiveContentRecyclerViewAdapter.ViewHolder) holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof LiveContentRecyclerViewAdapter.ViewHolder) {
            mLiveContentRecyclerViewAdapter.onViewDetachedFromWindow((LiveContentRecyclerViewAdapter.ViewHolder) holder);
        }
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == VIEW_TYPE_AD) {
            return super.getItemId(position);
        }

        return mLiveContentRecyclerViewAdapter.getItemId(getPositionWithoutAdOffset(position));
    }

    public int getRealItemCount() {
        return mLiveContentRecyclerViewAdapter.getItemCount();
    }

    public void articlesInserted(int start, List<PropertyObject> articles) {
        if (indexCache.size() > start && articles.size() > 0) {
            int startIndex = indexCache.get(start);
            addArticles(startIndex, articles);
            notifyItemRangeInserted(startIndex, articles.size());
            if (adInsertHelper != null) {
                List<Integer> insertedAdIndexes = adInsertHelper.fillAds(backingItems);
                updateAdOffsetCacheAndArticleIndex();
                for (int index : insertedAdIndexes) {
                    notifyItemInserted(index);
                }
            }
        }
        if (mLiveContentRecyclerViewAdapter.isReachedEnd()) {
            runOnUiThread(() -> safeNotifyItemChanged(items.size()));
        }
    }

    private void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            getUiThreadHandler().post(action);
        }
        else {
            action.run();
        }
    }

    private synchronized Handler getUiThreadHandler() {
        if (uiThreadHandler == null) {
            uiThreadHandler = new Handler(Looper.getMainLooper());
        }
        return uiThreadHandler;
    }

    public void articleRemoved(int position) {
        resetLists();
        notifyDataSetChanged();
    }

    public void articleChanged(int position) {
        safeNotifyItemChanged(indexCache.get(position));
    }

    public void articlesUpdated() {
        resetLists();
        addArticles(0, mLiveContentRecyclerViewAdapter.getItems());
        if (adInsertHelper != null) {
            adInsertHelper.fillAds(backingItems);
        }
        updateAdOffsetCacheAndArticleIndex();
        notifyDataSetChanged();
    }

    private void addArticles(int start, List<PropertyObject> items) {
        for (int i = 0; i < items.size(); i++) {
            PropertyObject item = items.get(i);
            int insertIndex = i + start;
            if (PropertyObjectKt.isRelated(item)) {
                backingItems.add(insertIndex, RELATED_TYPE_ID);
            }
            else {
                backingItems.add(insertIndex, ARTICLE_TYPE_ID);
            }
        }
        updateAdOffsetCacheAndArticleIndex();
    }

    public void setReachedEnd(boolean reachedEnd) {
        mLiveContentRecyclerViewAdapter.setReachedEnd(reachedEnd);
        if (!items.isEmpty()) {
            runOnUiThread(() -> safeNotifyItemChanged(items.size()));
        }
    }

    /**
     * Tries to notify item changed and swallow any IllegalStateException.
     * Will retry MAX_NOTIFY_RETRY_COUNT times
     *
     * @param position that changed
     */
    private void safeNotifyItemChanged(int position) {
        safeNotifyItemChanged(position, 0);
    }

    private void safeNotifyItemChanged(final int i, final int count) {
        try {
            notifyItemChanged(i);
        } catch (IllegalStateException e) {
            if (count < MAX_NOTIFY_RETRY_COUNT) {
                // Add to message queue regardless of current thread.
                getUiThreadHandler().post(() -> safeNotifyItemChanged(i, count + 1));
            } else {
                Timber.w(e, "Failed to notify item changed");
            }
        }
    }

    public int articlePosition(String articleId) {
        int articlePosition = mLiveContentRecyclerViewAdapter.articlePosition(articleId);
        if (articlePosition == -1) {
            return -1;
        }
        if (articlePosition >= indexCache.size()) {
            return indexCache.get(indexCache.size() - 1);
        }
        return indexCache.get(articlePosition);
    }

    public int getAdOffset(int position) {
        return position < adOffsetCache.size() ? adOffsetCache.get(position) : adOffsetCache.get(adOffsetCache.size() - 1);
    }

    public int getPositionWithoutAdOffset(int position) {
        return position - getAdOffset(position);
    }

    private void resetLists() {
        if (adInsertHelper != null) {
            adInsertHelper.reset();
        }
        backingItems.clear();
        items.clear();
    }

    public void reset() {
        mLiveContentRecyclerViewAdapter.setReachedEnd(false);
        resetLists();
        notifyDataSetChanged();
    }

    public void freeze() {
        mLiveContentRecyclerViewAdapter.freeze();
    }

    public void unfreeze() {
        mLiveContentRecyclerViewAdapter.unfreeze();
    }

    @Nullable
    @Override
    public PropertyObject getPropertyObjectForPosition(int position) {
        if (items.size() > position) {
            Object item = items.get(position);
            if (!isKnownType(item)) {
                return null;
            }
        }
        return mLiveContentRecyclerViewAdapter.getPropertyObjectForPosition(getPositionWithoutAdOffset(position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //Ad view
        public View publisherAdView;
        public FrameLayout adFrame;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
