package se.infomaker.livecontentui.section.ads;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleObserver;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.articleview.item.author.DividerDecorationConfig;
import se.infomaker.iap.theme.Theme;
import se.infomaker.library.AdViewFactory;
import se.infomaker.library.OnAdFailedListener;
import com.navigaglobal.mobile.livecontent.R;
import se.infomaker.livecontentui.impressions.ContentTracker;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.AdPosition;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;
import se.infomaker.livecontentui.section.FailableSectionItem;
import se.infomaker.livecontentui.section.SectionItem;
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder;
import se.infomaker.livecontentui.section.configuration.AdsConfiguration;
import timber.log.Timber;

public class AdSectionItem implements SectionItem, FailableSectionItem {

    public static final String AD_GROUP_KEY = "ads";
    public static final String AD_VIEW = "publisherAdView";
    public static final String AD_FRAME = "adFrame";
    private final AdPosition adPosition;
    private final AdsConfiguration configuration;
    private final List<JSONObject> adjacentContent;
    private final OnAdFailedListener onAdFailed;

    public AdSectionItem(AdsConfiguration configuration, String moduleTitle, AdPosition adPosition, List<JSONObject> adjacentContent) {
        this(configuration, moduleTitle, adPosition, adjacentContent, null);
    }

    public AdSectionItem(AdsConfiguration configuration, String moduleTitle, AdPosition adPosition, List<JSONObject> adjacentContent, OnAdFailedListener onAdFailed) {
        this.configuration = configuration;
        this.adPosition = adPosition;
        this.adjacentContent = adjacentContent;
        this.onAdFailed = onAdFailed;
    }

    public String getProvider() {
        return configuration.getProvider();
    }

    @Override
    public String getId() {
        return adPosition.getConfiguration().toString();
    }

    @Override
    public String sectionIdentifier() {
        return "ad";
    }

    @Nullable
    @Override
    public Set<LiveBinding> bind(@NonNull PropertyBinder binder, @NonNull SectionItemViewHolder viewHolder, @NonNull ResourceManager resourceManager, @NonNull Theme theme) {
        return bindAd(viewHolder, resourceManager, theme);
    }

    public List<JSONObject> getAdjacentContent() {
        return adjacentContent;
    }

    public Set<LiveBinding> bindAd(SectionItemViewHolder viewHolder, ResourceManager resourceManager, Theme theme) {
        View current = adPosition.current();
        View adView = viewHolder.getView(AD_VIEW);
        if (adView != null && adView.getParent() != null) {
            ((ViewGroup) adView.getParent()).removeView(adView);
        }
        if (current != null) {
            if (current.getParent() != null) {
                ((ViewGroup)current.getParent()).removeView(current);
            }
        }
        if (current != null) {
            adView = current;
        }
        else {
            adView = AdViewFactory.INSTANCE.getView(configuration.getProvider(), viewHolder.itemView.getContext(), adPosition.getConfiguration(), adjacentContent, AdStateManager.get(viewHolder.itemView.getContext()), () -> {
                if (!adPosition.isFailure()) {
                    Timber.d("Ad failed to load at list position: " + viewHolder.getAdapterPosition() + ", marking as failed and notifying listener.");
                    adPosition.markFailed();
                    if (onAdFailed != null) {
                        onAdFailed.onAdFailed();
                    }
                }
            });
            if (adView instanceof LifecycleObserver && viewHolder.getLifecycleOwner() != null) {
                viewHolder.getLifecycleOwner().getLifecycle().addObserver((LifecycleObserver) adView);
            }
            if (adView != null) {
                adPosition.register(adView);
            }
        }
        viewHolder.putNamedView(AD_VIEW, adView);
        if (adView != null) {
            if (adView.getLayoutParams() == null) {
                adView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            FrameLayout adFrame = viewHolder.getView(AD_FRAME);
            if (adFrame != null) {
                adFrame.addView(adView);
            }
            viewHolder.itemView.setVisibility(View.VISIBLE);
            viewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        else {
            viewHolder.itemView.setVisibility(View.GONE);
            viewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
        theme.apply(viewHolder.itemView);
        return null;
    }

    @Override
    public boolean isItemTheSame(SectionItem sectionItem) {
        return sectionItem instanceof AdSectionItem && ((AdSectionItem) sectionItem).adPosition.getConfiguration().toString().equals(adPosition.getConfiguration().toString());
    }

    @Override
    public boolean areContentsTheSame(SectionItem sectionItem) {
        return sectionItem instanceof AdSectionItem && ((AdSectionItem) sectionItem).adPosition.getAdIndex() == adPosition.getAdIndex();
    }

    @Override
    public void onDetach(SectionItemViewHolder viewHolder) {

    }

    @Override
    public String template() {
        return "ad_container";
    }

    @Override
    public String templateReference() {
        return template();
    }

    @Override
    public int defaultTemplate() {
        return R.layout.ad;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public String groupKey() {
        return AD_GROUP_KEY;
    }

    @Nullable
    @Override
    public List<String> overlayThemes() {
        return null;
    }

    @NonNull
    @Override
    public DividerDecorationConfig getDividerConfig() {
        return NO_DIVIDER_CONFIG;
    }

    @Override
    public Fragment createDetailView(String moduleId) {
        throw new RuntimeException("Unsupported!");
    }

    @Override
    public ContentTracker getContentTracker(String moduleId) {
        return null;
    }

    @Override
    public boolean isFailure() {
        return adPosition.isFailure();
    }
}
