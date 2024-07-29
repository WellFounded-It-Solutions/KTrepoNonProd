package se.infomaker.livecontentui.section.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.articleview.item.author.DividerDecorationConfig;
import se.infomaker.iap.articleview.item.author.DividerDecoratorKt;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.ui.theme.ThemeProvider;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.OnPresentationContextChangedListener;
import se.infomaker.livecontentui.PropertyObjectItemProvider;
import se.infomaker.livecontentui.config.BindingOverride;
import se.infomaker.livecontentui.config.ContentPresentationConfig;
import se.infomaker.livecontentui.config.TemplateConfig;
import se.infomaker.livecontentui.config.ThemeOverlayConfig;
import se.infomaker.livecontentui.extensions.OrgJSONKt;
import se.infomaker.livecontentui.impressions.VisibilityTracker;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.BinderCollection;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMFrameLayoutBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMImageViewBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMRecyclerViewBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.IMTextViewBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.image.ImageUrlBuilderFactory;
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;
import se.infomaker.livecontentui.section.ContentPresentationAware;
import se.infomaker.livecontentui.section.ExpandableListTracker;
import se.infomaker.livecontentui.section.PropertyObjectSectionItem;
import se.infomaker.livecontentui.section.Section;
import se.infomaker.livecontentui.section.SectionAdapterData;
import se.infomaker.livecontentui.section.SectionAdapterUpdater;
import se.infomaker.livecontentui.section.SectionItem;
import se.infomaker.livecontentui.section.SectionState;
import se.infomaker.livecontentui.section.ads.AdSectionItem;
import se.infomaker.livecontentui.section.binding.AdSectionItemBinder;
import se.infomaker.livecontentui.section.binding.PropertyObjectSectionItemBinder;
import se.infomaker.livecontentui.section.binding.SectionItemBinderCollection;
import se.infomaker.livecontentui.section.binding.SectionItemBinderProvider;
import se.infomaker.livecontentui.section.ktx.SectionItemUtils;
import se.infomaker.livecontentui.section.supplementary.ExpandableSectionItem;
import timber.log.Timber;

public class SectionAdapter extends RecyclerView.Adapter<SectionItemViewHolder> implements PropertyObjectItemProvider, OnPresentationContextChangedListener {

    public interface Listener {
        void onUpdate(SectionAdapterData adapterData);
    }

    private static final Object ANIMATION_BLOCKER = new Object();

    private final SectionAdapterUpdater updater;
    private final VisibilityTracker visibilityTracker;
    private final String moduleId;
    private final List<Section> sections;
    private final ResourceManager resourceManager;
    private final ThemeProvider themeProvider;
    private List<SectionItem> currentSectionItems;
    private Listener listener;
    private final SectionItemClickHandler itemClickHandler;
    private final SectionViewBehaviour viewBehaviour;
    private final LifecycleOwner lifecycleOwner;
    private final SectionItemBinderCollection binder;
    private final CompositeDisposable resumedDisposable = new CompositeDisposable();
    private final Set<LiveBinding> currentUpdaters = new HashSet<>();
    private boolean muteAnimations = false;
    final ExpandableListTracker expandableListTracker;

    private SectionAdapter(Builder builder) {
        updater = new SectionAdapterUpdater(builder.sections, builder.resourceManager, this);
        visibilityTracker = builder.visibilityTracker;
        moduleId = builder.moduleId;
        sections = builder.sections;
        resourceManager = builder.resourceManager;
        themeProvider = builder.themeProvider;
        PropertyBinder propertyBinder = new PropertyBinder(BinderCollection.with(new IMImageViewBinder(builder.imageUrlFactory, builder.imageSizes), new IMTextViewBinder(resourceManager), new IMFrameLayoutBinder(), new IMRecyclerViewBinder(resourceManager, builder.imageUrlFactory, builder.imageSizes, themeProvider.getTheme())));
        binder = new SectionItemBinderCollection(new PropertyObjectSectionItemBinder(propertyBinder));
        binder.register(AdSectionItem.class, new AdSectionItemBinder());
        binder.registerAll(SectionItemBinderProvider.all(propertyBinder, moduleId));
        itemClickHandler = new SectionItemClickHandler(moduleId, builder.moduleTitle, builder.extras, builder.contentViewConfig, updater, builder.expandableListTracker);

        lifecycleOwner = builder.lifecycleOwner;
        viewBehaviour = new SectionViewBehaviourFactory(resourceManager, moduleId, builder.lifecycleOwner, this).create(builder.presentationConfig, builder.templates, builder.overlayConfig);
        expandableListTracker = builder.expandableListTracker;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public ExpandableListTracker getExpandableSectionTracker() {
        return expandableListTracker;
    }

    public synchronized void update(SectionAdapterData adapterData) {
        if (listener != null) {
            listener.onUpdate(adapterData);
        }
        viewBehaviour.update(adapterData);
        currentSectionItems = adapterData.items;

        if (adapterData.state != SectionState.READY) {
            muteAnimations = true;
            notifyDataSetChanged();
        }
        else {
            if (muteAnimations) {
                notifyDataSetChanged();
                muteAnimations = false;
            }
            else {
                adapterData.diffResult.dispatchUpdatesTo(this);
            }
        }
    }

    @NonNull
    @Override
    public SectionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = viewBehaviour.layoutResourceForViewType(viewType);
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        List<BindingOverride> bindingOverrides = viewBehaviour.bindingOverridesForViewType(viewType);
        DefaultUtils.applyOverrides(bindingOverrides, itemView);

        return new SectionItemViewHolder(itemView, itemClickHandler, lifecycleOwner);
    }

    @Override
    public void onBindViewHolder(SectionItemViewHolder holder, int position) {
        holder.detach();

        SectionItem item = currentSectionItems.get(position);

        visibilityTracker.resetIfChanged(holder.itemView, item.getContentTracker(moduleId));
        holder.setContentTracker(item.getContentTracker(moduleId));

        if (item instanceof ExpandableSectionItem) {
            ((ExpandableSectionItem) item).setExpanded(expandableListTracker.getExpandedLists().contains(((ExpandableSectionItem) item).getListUuid()));
        }

        Theme overlayedTheme = themeProvider.getTheme(viewBehaviour.themesForKey(item));
        DividerDecorationConfig dividerConfig = item.getDividerConfig();
        dividerConfig.setTheme(overlayedTheme);
        DividerDecoratorKt.putDividers(holder.itemView, dividerConfig);

        if (holder.getUpdater() != null) {
            currentUpdaters.removeAll(holder.getUpdater());
            for (LiveBinding updater : holder.getUpdater()) {
                updater.recycle();
            }
            holder.setUpdater(null);
        }

        Set<LiveBinding> updater = binder.bind(item, holder, resourceManager, overlayedTheme);
        if (updater != null) {
            holder.setUpdater(updater);
            currentUpdaters.addAll(updater);
        }

        holder.bound();
    }

    @Override
    public int getItemViewType(int position) {
        SectionItem item = currentSectionItems.get(position);
        return viewBehaviour.viewTypeForKey(item);
    }

    @Override
    public int getItemCount() {
        return currentSectionItems != null ? currentSectionItems.size() : 0;
    }

    public void resume() {
        ArrayList<Observable<List<SectionItem>>> observables = new ArrayList<>();
        for (Section section : sections) {
            observables.add(section.observeItems());
        }

        resumedDisposable.add(Observable.combineLatest(observables, (Function<Object[], List<SectionItem>>) objects -> {
            ArrayList<SectionItem> items = new ArrayList<>();
            for (Object object : objects) {
                items.addAll((List<SectionItem>) object);
            }
            return items;
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> updater.dispatch(items, getLastUpdated()), throwable -> {
                    Timber.e(throwable, "Failed to load section, pausing and resuming");
                    pause();
                    resume();
                }));

        Observable.fromIterable(sections).doOnNext(Section::resume)
                .subscribe(Functions.emptyConsumer(), throwable -> Timber.e(throwable, "Failed to resume section"));
        Observable.fromIterable(currentUpdaters).doOnNext(LiveBinding::start)
                .subscribe(Functions.emptyConsumer(), throwable -> Timber.e(throwable, "Failed to start live bindings"));
    }

    public void pause() {
        resumedDisposable.clear();
        Observable.fromIterable(sections).doOnNext(Section::pause)
                .subscribe(Functions.emptyConsumer(), throwable -> Timber.e(throwable, "Failed to pause"));
        Observable.fromIterable(currentUpdaters).doOnNext(LiveBinding::stop)
                .subscribe(Functions.emptyConsumer(), throwable -> Timber.e(throwable, "Failed to stop live binding"));
    }

    public void reload() {
        updater.collapseAllExpandableSections();
        Observable.fromIterable(sections).doOnNext(Section::reload)
                .subscribe(Functions.emptyConsumer(), throwable -> Timber.e(throwable, "Failed to reload"));
    }

    public Observable<SectionState[]> observeSectionStates() {
        List<Observable<SectionState>> observableList = Observable.fromIterable(sections)
                .map(Section::observeState)
                .toList().blockingGet();

        return Observable.combineLatest(observableList, objects -> {
                    SectionState[] sectionStates = new SectionState[objects.length];
                    for (int i = 0; i < objects.length; i++) {
                        sectionStates[i] = (SectionState) objects[i];
                    }
                    return sectionStates;
                }
        ).startWith(new SectionState[0]);
    }

    public int positionForId(String itemId) {
        List<SectionItem> items = this.currentSectionItems;
        if (items != null) {
            for (SectionItem item : items) {
                if (item.getId().equals(itemId) && !SectionItemUtils.isRelated(item)) {
                    return items.indexOf(item);
                }
            }
        }
        return -1;
    }

    private Date getLastUpdated() {
        Date out = null;
        for (Section section : sections) {
            Date lastUpdated = section.lastUpdated();
            if (lastUpdated != null && (out == null || out.before(lastUpdated))) {
                out = lastUpdated;
            }
        }
        return out;
    }

    @Nullable
    @Override
    public PropertyObject getPropertyObjectForPosition(int position) {
        SectionItem item = currentSectionItems.get(position);
        if (item instanceof PropertyObjectSectionItem) {
            return ((PropertyObjectSectionItem) item).getPropertyObject();
        }
        return null;
    }

    @Override
    public void onPresentationContextChanged(@NonNull Map<String, JSONObject> changes) {
        if (currentSectionItems != null && changes.size() > 0) {
            for (int i = 0; i < currentSectionItems.size(); i++) {
                SectionItem item = currentSectionItems.get(i);
                if (item instanceof ContentPresentationAware) {
                    JSONObject contextAddition = changes.get(item.getId());
                    if (contextAddition != null) {
                        JSONObject itemContext = OrgJSONKt.patch(((ContentPresentationAware) item).getContext(), contextAddition);
                        ((ContentPresentationAware) item).setContext(itemContext);
                        notifyItemChanged(i, ANIMATION_BLOCKER);
                    }
                }
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VisibilityTracker visibilityTracker;
        private List<Section> sections;
        private ImageUrlBuilderFactory imageUrlFactory;
        private String moduleId;
        private String moduleTitle;
        private List<Double> imageSizes;
        private ResourceManager resourceManager;
        private ThemeProvider themeProvider;
        private ThemeOverlayConfig overlayConfig;
        private Bundle extras;
        private Map<String, TemplateConfig> templates;
        private JsonObject contentViewConfig;
        private ContentPresentationConfig presentationConfig;
        private LifecycleOwner lifecycleOwner;
        private ExpandableListTracker expandableListTracker;


        private Builder() {

        }

        public Builder setVisibilityTracker(VisibilityTracker visibilityTracker) {
            this.visibilityTracker = visibilityTracker;
            return this;
        }

        public Builder setSections(List<Section> sections) {
            this.sections = sections;
            return this;
        }

        public Builder setImageUrlFactory(ImageUrlBuilderFactory imageUrlFactory) {
            this.imageUrlFactory = imageUrlFactory;
            return this;
        }

        public Builder setModuleId(String moduleId) {
            this.moduleId = moduleId;
            return this;
        }

        public Builder setModuleTitle(String moduleTitle) {
            this.moduleTitle = moduleTitle;
            return this;
        }

        public Builder setImageSizes(List<Double> imageSizes) {
            this.imageSizes = imageSizes;
            return this;
        }

        public Builder setResourceManager(ResourceManager resourceManager) {
            this.resourceManager = resourceManager;
            return this;
        }

        public Builder setThemeProvider(ThemeProvider themeProvider) {
            this.themeProvider = themeProvider;
            return this;
        }

        public Builder setOverlayConfig(ThemeOverlayConfig overlayConfig) {
            this.overlayConfig = overlayConfig;
            return this;
        }

        public Builder setExtras(Bundle extras) {
            this.extras = extras;
            return this;
        }

        public Builder setTemplates(Map<String, TemplateConfig> templates) {
            this.templates = templates;
            return this;
        }

        public Builder setContentViewConfig(JsonObject contentViewConfig) {
            this.contentViewConfig = contentViewConfig;
            return this;
        }

        public Builder setPresentationConfig(ContentPresentationConfig presentationConfig) {
            this.presentationConfig = presentationConfig;
            return this;
        }

        public Builder setLifecycleOwner(LifecycleOwner lifecycleOwner) {
            this.lifecycleOwner = lifecycleOwner;
            return this;
        }

        public Builder setExpandableSectionTracker(ExpandableListTracker expandableListTracker) {
            this.expandableListTracker = expandableListTracker;
            return this;
        }

        public SectionAdapter build() {
            return new SectionAdapter(this);
        }
    }
}