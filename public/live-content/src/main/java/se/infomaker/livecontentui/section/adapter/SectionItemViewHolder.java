package se.infomaker.livecontentui.section.adapter;

import android.content.res.Resources;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import se.infomaker.livecontentui.impressions.ContentTracker;
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;
import se.infomaker.livecontentui.livecontentrecyclerview.view.OnClickObservable;
import se.infomaker.livecontentui.livecontentrecyclerview.view.ViewClick;
import se.infomaker.livecontentui.section.SectionItem;
import timber.log.Timber;

public class SectionItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private final List<View> viewsArrayList;
    private ContentTracker contentTracker;
    private final SectionItemClickHandler itemClickHandler;
    private SectionItem item;
    private final HashMap<String, View> namedViews = new HashMap<>();
    private Set<LiveBinding> updater;
    private final CompositeDisposable onClickGarbage = new CompositeDisposable();
    private final LifecycleOwner lifecycleOwner;

    public SectionItemViewHolder(View itemView, SectionItemClickHandler itemClickHandler, LifecycleOwner lifecycleOwner) {
        super(itemView);
        this.itemClickHandler = itemClickHandler;
        viewsArrayList = DefaultUtils.getAllChildren(itemView);
        for (View view : viewsArrayList) {
            if (view.getId() == -1) {
                continue;
            }
            try {
                String name = view.getResources().getResourceEntryName(view.getId());
                if (name != null) {
                    namedViews.put(name, view);
                }
            }
            catch (Resources.NotFoundException e) {
                Timber.w(e, "did not find resource");
            }
        }
        this.lifecycleOwner = lifecycleOwner;
    }

    public List<View> getViewsArrayList() {
        return viewsArrayList;
    }

    @Nullable
    public SectionItem getItem() {
        return item;
    }

    public void setItem(SectionItem item) {
        this.item = item;
        if (item != null && item.isClickable()) {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
    }

    public final <T extends View> T getView(String name) {
        return (T) namedViews.get(name);
    }

    public void putNamedView(String name, View view) {
        this.namedViews.put(name, view);
    }

    public void detach() {
        if (item != null) {
            item.onDetach(this);
        }
        onClickGarbage.clear();
    }

    public void bound() {
        for (View view : viewsArrayList) {
            if (view instanceof OnClickObservable) {
                Observable<ViewClick> clicks = ((OnClickObservable) view).clicks();
                if (clicks != null) {
                    onClickGarbage.add(clicks
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(viewClick ->
                                    itemClickHandler.onSubItemClick(view.getContext(), viewClick, item)
                            )
                    );
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (item != null && item.isClickable()) {
            itemClickHandler.onItemClick(view, item);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        return itemClickHandler.onItemLongClick(view, item);
    }

    public Set<LiveBinding> getUpdater() {
        return updater;
    }

    public void setUpdater(Set<LiveBinding> updater) {
        this.updater = updater;
    }

    public void setContentTracker(ContentTracker onShow) {
        this.contentTracker = onShow;
    }

    public LifecycleOwner getLifecycleOwner() {
        return lifecycleOwner;
    }
}
