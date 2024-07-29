package se.infomaker.livecontentui.livecontentrecyclerview.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.navigaglobal.mobile.livecontent.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.frtutilities.TextUtils;
import se.infomaker.frtutilities.connectivity.Connectivity;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.ui.theme.ThemeProvider;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.OnPresentationContextChangedListener;
import se.infomaker.livecontentui.PropertyObjectItemProvider;
import se.infomaker.livecontentui.ViewBehaviour;
import se.infomaker.livecontentui.config.BindingOverride;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.extensions.PropertyObjectKt;
import se.infomaker.livecontentui.impressions.VisibilityTracker;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.BinderCollection;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMFrameLayoutBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMImageViewBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMTextViewBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilderFactory;
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils;
import se.infomaker.livecontentui.livecontentrecyclerview.view.IMImageView;
import timber.log.Timber;

public class LiveContentRecyclerViewAdapter extends RecyclerView.Adapter<LiveContentRecyclerViewAdapter.ViewHolder> implements LifecycleObserver, PropertyObjectItemProvider {

    public static final int VIEW_TYPE_LOADING_SPINNER = 99;
    public static final int VIEW_TYPE_NO_MORE_ARTICLES = 98;
    public static final PropertyObject EMPTY_PROPERTY_OBJECT = new PropertyObject(new JSONObject(), UUID.randomUUID().toString());

    private final ThemeProvider themeProvider;
    private final LiveContentUIConfig mConfig;
    private final VisibilityTracker visibilityTracker;
    private final PropertyBinder mBinder;
    private final String mModuleIdentifier;
    private final OnClickListener mOnViewHolderClickListener;
    private final Bundle statsExtras;
    private final ViewBehaviour<PropertyObject> viewBehaviour;

    //Use this when no action is wanted on click
    private final OnClickListener NO_ACTION_CLICK_LISTENER = new OnClickListener() {
        @Override
        public void onClick(View view, int position) {
            // NOP
        }

        @Override
        public boolean onLongClick(View view, int position) {
            return false;
        }
    };

    private boolean reachedEnd = false;
    private List<PropertyObject> items = null;
    private List<PropertyObject> frozenItems = null;
    private CompositeDisposable garbage = new CompositeDisposable();
    private boolean hasNetwork = false;

    public LiveContentRecyclerViewAdapter(VisibilityTracker visibilityTracker, Context context, String moduleIdentifier, ImageUrlBuilderFactory imageUrlFactory,
                                          List<Double> imageSizes, OnClickListener onViewHolderClickListener, ThemeProvider themeProvider,
                                          LiveContentUIConfig config, Bundle statsExtras, LifecycleOwner lifecycleOwner, Observable<List<PropertyObject>> data,
                                          OnPresentationContextChangedListener onPresentationContextChangedListener) {
        this.themeProvider = themeProvider;
        this.visibilityTracker = visibilityTracker;
        this.mConfig = config;
        mModuleIdentifier = moduleIdentifier;
        mOnViewHolderClickListener = onViewHolderClickListener;
        ResourceManager mResourceManager = new ResourceManager(context, moduleIdentifier);
        mBinder = new PropertyBinder(BinderCollection.with(new IMImageViewBinder(imageUrlFactory, imageSizes), new IMTextViewBinder(mResourceManager), new IMFrameLayoutBinder()));
        this.statsExtras = statsExtras;
        viewBehaviour = new ViewBehaviourFactory(mResourceManager, mModuleIdentifier, lifecycleOwner, onPresentationContextChangedListener).create(config);
        if (data != null) {
            garbage.add(data.subscribe((articles) -> items = articles));
        }
        garbage.add(Connectivity.observable().subscribe((connected) -> hasNetwork = connected));
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onDestroy() {
        if (garbage != null) {
            garbage.dispose();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_LOADING_SPINNER: {
                if (hasNetwork) {
                    ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_spinner, parent, false);
                    ViewHolder viewHolder = new ViewHolder(viewGroup, "progress_spinner", NO_ACTION_CLICK_LISTENER);
                    viewHolder.itemView.setVisibility(View.GONE);
                    return viewHolder;
                }
            }
            case VIEW_TYPE_NO_MORE_ARTICLES: {
                TextView endOfFeedMessageView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_no_more_articles, parent, false);
                if(!TextUtils.isEmpty(mConfig.getEndOfContentsText())) {
                    endOfFeedMessageView.setText(mConfig.getEndOfContentsText());
                }
                ViewHolder viewHolder = new ViewHolder(endOfFeedMessageView, "progress_no_more_articles", NO_ACTION_CLICK_LISTENER);
                viewHolder.itemView.setVisibility(View.GONE);
                return viewHolder;
            }
            default: {
                int layoutResource = viewBehaviour.layoutResourceForViewType(viewType);
                View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);

                List<BindingOverride> bindingOverrides = viewBehaviour.bindingOverridesForViewType(viewType);
                DefaultUtils.applyOverrides(bindingOverrides, itemView);

                return new ViewHolder(itemView, null, mOnViewHolderClickListener);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getSize()) {
            if (!reachedEnd)
                return VIEW_TYPE_LOADING_SPINNER;
            else
                return VIEW_TYPE_NO_MORE_ARTICLES;
        }
        PropertyObject item = getItem(position);
        PropertyObjectKt.setFirst(item, position == 0);
        return viewBehaviour.viewTypeForKey(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_LOADING_SPINNER ||
                getItemViewType(position) == VIEW_TYPE_NO_MORE_ARTICLES) {
            if (getSize() > 0) {
                themeProvider.getTheme().apply(viewHolder.itemView);
                viewHolder.itemView.setVisibility(View.VISIBLE);
            }
            return;
        }
        final PropertyObject object = getItem(position);

        Theme theme = themeProvider.getTheme(viewBehaviour.themesForKey(object));
        theme.apply(viewHolder.itemView);

        PropertyObjectContentTracker tracker = new PropertyObjectContentTracker(object, mModuleIdentifier, statsExtras);
        visibilityTracker.resetIfChanged(viewHolder.itemView, tracker);

        mBinder.bind(object, viewHolder.viewsArrayList, viewBehaviour.presentationContextForKey(object));
        viewHolder.propertyObject = object;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);
        for (View view : viewHolder.viewsArrayList) {
            if (view instanceof IMImageView) {
                IMImageView imageView = (IMImageView) view;
                Glide.with(imageView.getContext()).clear(imageView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return getSize() + 1;
    }

    public void setReachedEnd(boolean reachedEnd) {
        this.reachedEnd = reachedEnd;
    }

    public boolean isReachedEnd() {
        return reachedEnd;
    }

    public int articlePosition(String articleId) {
        try {
            for (int i = 0; i < getSize(); i++) {
                PropertyObject article = getItem(i);
                if (articleId.equals(article.getId()) && !PropertyObjectKt.isRelated(article)) {
                    return i;
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Could not get article position");
        }
        return -1;
    }

    public void freeze() {
        if (items != null) {
            frozenItems = new ArrayList<>(items);
        }
        else {
            frozenItems = new ArrayList<>();
        }
    }

    public void unfreeze() {
        frozenItems = null;
    }

    public String itemId(int position) {
        return getItem(position).getId();
    }

    public PropertyObject getItem(int position) {
        if (frozenItems != null) {
            return frozenItems.get(position);
        }
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        } else {
            // Handle the case when the index is out of bounds
            // You can return null or a default value, depending on your requirements
            return null; // Or return a default PropertyObject
        }
    }

    private int getSize() {
        if (frozenItems != null) {
            return frozenItems.size();
        }
        return items != null ? items.size() : 0;
    }

    public List<PropertyObject> getItems() {
        List<PropertyObject> out = new ArrayList<>(this.items);
        out.add(EMPTY_PROPERTY_OBJECT);
        return out;
    }

    @Nullable
    @Override
    public PropertyObject getPropertyObjectForPosition(int position) {
        if (getSize() > 0 && position < getSize()) {
            return getItem(position);
        }
        return null;
    }

    public interface OnClickListener {
        void onClick(View view, int position);
        boolean onLongClick(View view, int position);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public OnClickListener mOnClickListener;
        String layoutName;
        List<View> viewsArrayList;
        private PropertyObject propertyObject = null;

        public ViewHolder(View itemView, String layoutName, OnClickListener onCLickListener) {
            super(itemView);
            this.layoutName = layoutName;

            // Get all views from the viewGroup
            viewsArrayList = DefaultUtils.getAllChildren(itemView);

            mOnClickListener = onCLickListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setTag(this);
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return mOnClickListener.onLongClick(v, getAdapterPosition());
        }

        @Nullable
        public PropertyObject getPropertyObject() {
            return propertyObject;
        }
    }
}