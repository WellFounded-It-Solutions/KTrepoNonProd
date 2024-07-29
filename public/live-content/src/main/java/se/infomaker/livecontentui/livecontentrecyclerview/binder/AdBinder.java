package se.infomaker.livecontentui.livecontentrecyclerview.binder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import org.json.JSONObject;

import java.util.List;

import se.infomaker.library.AdViewFactory;
import se.infomaker.library.OnAdFailedListener;
import se.infomaker.livecontentui.config.AdsConfig;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.AdPosition;
import se.infomaker.livecontentui.livecontentrecyclerview.adapter.AdWrapperAdapter;
import se.infomaker.livecontentui.section.ads.AdStateManager;

public class AdBinder {

    public static final String USER_LOCATIONS = "userLocations";
    public static final String PAGE_TITLE = "pageTitle";
    private Context mContext;
    private AdsConfig mAdsConfig;
    private final String mModuleTitle;

    private final LifecycleOwner lifecycleOwner;

    @Deprecated
    public AdBinder(Context context, AdsConfig adsConfig, String moduleTitle) {
        this(context, adsConfig, moduleTitle, null);
    }

    public AdBinder(Context mContext, AdsConfig mAdsConfig, String moduleTitle, LifecycleOwner lifecycleOwner) {
        this.mModuleTitle = moduleTitle;
        this.mContext = mContext;
        this.mAdsConfig = mAdsConfig;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void bindAd(AdWrapperAdapter.ViewHolder viewHolder, AdPosition adPosition, List<JSONObject> adjacentContent) {
        bindAd(viewHolder, adPosition, adjacentContent, null);
    }

    public void bindAd(AdWrapperAdapter.ViewHolder viewHolder, AdPosition adPosition, List<JSONObject> adjacentContent, OnAdFailedListener adListener) {
        View current = adPosition.current();
        if (viewHolder.publisherAdView != null && viewHolder.publisherAdView.getParent() != null) {
            ((ViewGroup) viewHolder.publisherAdView.getParent()).removeView(viewHolder.publisherAdView);
        }
        if (current != null) {
            if (current.getParent() != null) {
                ((ViewGroup)current.getParent()).removeView(current);
            }
        }
        if (current != null) {
            viewHolder.publisherAdView = current;
        }
        else if (mAdsConfig != null && mAdsConfig.getProvider() != null) {
            View adView = AdViewFactory.INSTANCE.getView(mAdsConfig.getProvider(), viewHolder.adFrame.getContext(), adPosition.getConfiguration(), adjacentContent, AdStateManager.get(viewHolder.itemView.getContext()), adListener);
            if (adView instanceof LifecycleObserver && lifecycleOwner != null) {
                lifecycleOwner.getLifecycle().addObserver((LifecycleObserver) adView);
            }
            if (adView != null) {
                adPosition.register(adView);
            }
            viewHolder.publisherAdView = adView;
        }
        else {
            viewHolder.publisherAdView = new View(viewHolder.adFrame.getContext());
        }

        if (viewHolder.publisherAdView != null) {
            ViewGroup.LayoutParams params = viewHolder.publisherAdView.getLayoutParams();
            if (params == null) {
                viewHolder.publisherAdView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            viewHolder.adFrame.addView(viewHolder.publisherAdView);
        }
    }
}
