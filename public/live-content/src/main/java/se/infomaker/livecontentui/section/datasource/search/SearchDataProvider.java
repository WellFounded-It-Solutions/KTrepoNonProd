package se.infomaker.livecontentui.section.datasource.search;

import com.jakewharton.rxrelay2.BehaviorRelay;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import se.infomaker.livecontentmanager.config.LiveContentConfig;
import se.infomaker.livecontentmanager.config.PropertyConfig;
import se.infomaker.livecontentmanager.extensions.JSONUtil;
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser;
import se.infomaker.livecontentmanager.query.ParameterSearchQuery;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryManager;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import se.infomaker.livecontentmanager.query.lcc.BroadcastObjectChangeManager;
import se.infomaker.livecontentui.extensions.OrgJSONKt;
import se.infomaker.livecontentui.section.ContentPresentationAware;
import se.infomaker.livecontentui.section.LayoutResolver;
import se.infomaker.livecontentui.section.SectionItem;
import se.infomaker.livecontentui.section.SectionUpdateNotifier;
import se.infomaker.livecontentui.section.datasource.DataSource;
import se.infomaker.livecontentui.section.datasource.DataSourceResponse;
import se.infomaker.livecontentui.section.datasource.newspackage.ArticleSectionItem;
import timber.log.Timber;

public class SearchDataProvider implements DataSource, QueryResponseListener {
    private static final String DEFAULT_GROUP_KEY = "article";
    private final SearchSectionConfig config;
    private final LayoutResolver layoutResolver;
    private final LiveContentConfig liveContentConfig;
    private final String properties;
    private final DefaultPropertyObjectParser objectParser;
    private final QueryManager queryManager;
    private final BehaviorRelay<DataSourceResponse> publishRelay = BehaviorRelay.create();
    private final BroadcastObjectChangeManager changeManager;
    private final Set<String> currentObjects = new HashSet<>();
    private final String sectionIdentifier;
    private final JSONObject context;
    private CompositeDisposable disposables = new CompositeDisposable();
    private static final int MAX_RETRY_COUNT = 3;
    private int retryCount = 0;

    public SearchDataProvider(LayoutResolver layoutResolver, LiveContentConfig liveContent, String sectionIdentifier, SearchSectionConfig configuration, QueryManager queryManager, BroadcastObjectChangeManager manager, JSONObject context) {
        this.changeManager = manager;
        this.sectionIdentifier = sectionIdentifier;
        this.config = configuration;
        this.layoutResolver = layoutResolver;
        this.liveContentConfig = liveContent;
        properties = liveContentConfig.getProperties(config.getPropertyMapReference());
        HashMap<String, Map<String, PropertyConfig>> typePropertyMap = liveContentConfig.getTypePropertyMap();
        if (typePropertyMap == null) {
            typePropertyMap = new HashMap<>();
        }
        Map<String, String> typeDescriptionTemplate = liveContentConfig.getTypeDescriptionTemplate();
        if (typeDescriptionTemplate == null) {
            typeDescriptionTemplate = new HashMap<>();
        }
        objectParser = new DefaultPropertyObjectParser(typePropertyMap, typeDescriptionTemplate, liveContentConfig.getTransformSettings());
        this.queryManager = queryManager;
        this.context = context;
    }

    @Override
    public boolean resume() {
        if (disposables.size() == 0) {
            update();
            disposables.add(changeManager.observeWithFilter(currentObjects).subscribe(s -> {
                if (config.isUpdateAllOnChange()) {
                    SectionUpdateNotifier.updateAll();
                }
                else {
                    update();
                }
            }));
            if (currentObjects.size() > 0) {
                changeManager.subscribe(currentObjects);
            }
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        queryManager.addQuery(new ParameterSearchQuery(liveContentConfig.getSearch().getContentProvider(), properties, config.getQueryParams(), null), this);
    }

    @Override
    public void pause() {
        if (disposables.size() > 0 && currentObjects.size() > 0) {
            changeManager.unsubscribe(currentObjects);
        }
        disposables.clear();
    }

    @Override
    public Observable<DataSourceResponse> observeResponse() {
        return publishRelay;
    }

    @Override
    synchronized public void onResponse(Query query, JSONObject response) {
        retryCount = 0;
        try {
            List<SectionItem> items = Observable.fromIterable(objectParser.fromSearch(response, config.getPropertyMapReference())).map(propertyObject -> {
                String template = layoutResolver.getValidTemplate(propertyObject, config.getTemplatePrefix());
                String templateReference = layoutResolver.getValidTemplateReference(propertyObject, config.getTemplatePrefix());
                if(context==null)
                    return (SectionItem) new ArticleSectionItem(propertyObject, sectionIdentifier, getGroupKey(), template, templateReference, config.resolveThemeOverlayAsList(propertyObject), SectionItem.NO_DIVIDER_CONFIG, context);
                else
                    return (SectionItem) new ArticleSectionItem(propertyObject, sectionIdentifier, getGroupKey(), template, templateReference, config.resolveThemeOverlayAsList(propertyObject), SectionItem.NO_DIVIDER_CONFIG, new JSONObject(context.toString()));
            }).toList().blockingGet();

            if (!items.isEmpty()) {
                SectionItem item = items.get(0);
                if (item instanceof ContentPresentationAware) {
                    JSONObject itemContext = ((ContentPresentationAware) item).getContext();
                    if (itemContext == null) {
                        itemContext = new JSONObject();
                    }
                    OrgJSONKt.putAt(itemContext, "position.first", true);
                    ((ContentPresentationAware) item).setContext(itemContext);
                }
            }

            Set<String> newUUIDs = Observable.fromIterable(items).map(SectionItem::getId).distinct().toList().map((Function<List<String>, Set<String>>) HashSet::new).blockingGet();
            Set<String> added;
            if (currentObjects.size() > 0) {
                List<String> removed = Observable.fromIterable(currentObjects).filter(uuid -> !newUUIDs.contains(uuid)).toList().blockingGet();
                added = new HashSet<>(Observable.fromIterable(newUUIDs).filter(uuid -> !currentObjects.contains(uuid)).toList().blockingGet());
                if (removed.size() > 0) {
                    changeManager.unsubscribe(new HashSet<>(removed));
                }
            }
            else {
                added = newUUIDs;
            }

            currentObjects.clear();
            currentObjects.addAll(newUUIDs);

            if (added.size() > 0) {
                changeManager.subscribe(new HashSet<>(added));
            }

            publishRelay.accept(new DataSourceResponse(items, null, JSONUtil.getLastUpdated(response)));
        }
        catch (Throwable throwable) {
            publishRelay.accept(new DataSourceResponse(null, throwable, null));
        }
    }

    @Override
    public void onError(Throwable exception) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            update();
            Timber.e(exception, "Retrying " + retryCount + " (max retries " + MAX_RETRY_COUNT + ")");
        }
        else {
            retryCount = 0;
            Timber.w(exception, "Could not load data");
            publishRelay.accept(new DataSourceResponse(null, exception, null));
        }
    }

    @Override
    public Set<String> groupKeys() {
        HashSet<String> keys = new HashSet<>();
        keys.add(getGroupKey());
        return keys;
    }

    private String getGroupKey() {
        return config.getGroup() != null ? config.getGroup() : DEFAULT_GROUP_KEY;
    }
}
