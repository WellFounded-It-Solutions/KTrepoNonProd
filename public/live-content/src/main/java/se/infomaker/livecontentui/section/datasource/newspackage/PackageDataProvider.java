package se.infomaker.livecontentui.section.datasource.newspackage;

import android.text.TextUtils;

import com.jakewharton.rxrelay2.BehaviorRelay;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import se.infomaker.datastore.Article;
import se.infomaker.datastore.ArticleLastViewDao;
import se.infomaker.datastore.DatabaseSingleton;
import se.infomaker.iap.articleview.item.author.DividerDecorationConfig;
import se.infomaker.livecontentmanager.config.LiveContentConfig;
import se.infomaker.livecontentmanager.config.PropertyConfig;
import se.infomaker.livecontentmanager.extensions.JSONUtil;
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.query.ParameterSearchQuery;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryManager;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import se.infomaker.livecontentmanager.query.lcc.BroadcastObjectChangeManager;
import se.infomaker.livecontentui.section.LayoutResolver;
import se.infomaker.livecontentui.section.PropertyObjectSectionItem;
import se.infomaker.livecontentui.section.SectionItem;
import se.infomaker.livecontentui.section.SectionUpdateNotifier;
import se.infomaker.livecontentui.section.configuration.DividerConfig;
import se.infomaker.livecontentui.section.datasource.DataSource;
import se.infomaker.livecontentui.section.datasource.DataSourceResponse;
import timber.log.Timber;

public class PackageDataProvider implements QueryResponseListener, DataSource {

    public static final String DEFAULT_GROUP_KEY = "article";
    private final LayoutResolver layoutResolver;
    private final BroadcastObjectChangeManager changeManager;
    private final String sectionIdentifier;
    private final JSONObject context;
    private final LiveContentConfig liveContentConfig;
    private final PackageSectionConfig config;
    private final DefaultPropertyObjectParser objectParser;
    private final String properties;
    private final QueryManager queryManager;
    private final Set<String> currentObjects = new HashSet<>();
    private final BehaviorRelay<DataSourceResponse> publishRelay = BehaviorRelay.create();
    private final CompositeDisposable disposables = new CompositeDisposable();
    private static final int MAX_RETRY_COUNT = 3;
    private int retryCount = 0;

    public PackageDataProvider(LayoutResolver layoutResolver, LiveContentConfig liveContentConfig, String sectionIdentifier, PackageSectionConfig config, QueryManager queryManager, BroadcastObjectChangeManager manager, JSONObject context) {
        this.changeManager = manager;
        this.config = config;
        this.sectionIdentifier = sectionIdentifier;
        this.layoutResolver = layoutResolver;
        this.liveContentConfig = liveContentConfig;
        this.context = context;
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
    }

    @Override
    public void update() {
        queryManager.addQuery(new ParameterSearchQuery(liveContentConfig.getSearch().getContentProvider(), properties, config.getQueryParams(), null), this);
    }

    @Override
    synchronized public void onResponse(Query query, JSONObject response) {
        retryCount = 0;
        ArrayList<SectionItem> sectionItems = new ArrayList<>();

        List<PropertyObject> packages = objectParser.fromSearch(response, config.getPropertyMapReference());
        for (PropertyObject aPackage : packages) {
            String overlayThemeFile = config.resolveThemeOverlay(aPackage);

            SectionItem coverItem = createCoverItem(aPackage, overlayThemeFile);
            sectionItems.add(coverItem);

            List<PropertyObjectSectionItem> articleItems = createArticleItems(aPackage, overlayThemeFile);
            if (articleItems != null) {
                sectionItems.addAll(articleItems);
            }
        }

        Set<String> newUUIDs = objectParser.getAllIds(packages, config.getPropertyMapReference());
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

        publishRelay.accept(new DataSourceResponse(sectionItems, null, JSONUtil.getLastUpdated(response)));
    }

    private ArticleLastViewDao articleLastViewDao = DatabaseSingleton.getDatabaseInstance().userLastViewDao();

    private List<PropertyObjectSectionItem> createArticleItems(PropertyObject aPackage, String overlayThemeFile) {
        ArrayList<PropertyObjectSectionItem> items = new ArrayList<>();
        List<PropertyObject> lists = aPackage.optPropertyObjects("list");
        if (lists != null) {
            for (PropertyObject list : lists) {
                List<PropertyObject> articles = list.optPropertyObjects("articles");
                if (articles == null) {
                    continue;
                }

                List<String> ids = Observable.fromIterable(articles).map(PropertyObject::getId).toList().toMaybe().blockingGet();
                List<String> readArticleIds = new ArrayList<>();
                if (!ids.isEmpty()) {
                    readArticleIds = Observable.fromIterable(articleLastViewDao.getArticles(ids))
                            .filter(article -> article.getUuid() != null)
                            .map(Article::getUuid).toList()
                            .toMaybe().blockingGet(readArticleIds);
                }

                for (PropertyObject article : articles) {
                    article.putString("article_placement_number", Integer.toString(articles.indexOf(article) + 1));
                    String template = layoutResolver.getValidTemplate(article, config.getTemplatePrefix());
                    String templateReference = layoutResolver.getValidTemplateReference(article, config.getTemplatePrefix());

                    DividerDecorationConfig dividerDecorationConfig = SectionItem.NO_DIVIDER_CONFIG;

                    if (config.getDividerConfig() != null) {
                        boolean before = false;
                        boolean after = true;

                        //First one
                        if (articles.get(0) == article && config.getDividerConfig().getPlacement() == DividerConfig.Placement.AROUND) {
                            before = true;
                        }

                        //Last one
                        if (articles.get(articles.size() - 1) == article && config.getDividerConfig().getPlacement() != DividerConfig.Placement.AROUND) {
                            after = false;
                        }

                        dividerDecorationConfig = new DividerDecorationConfig(config.getDividerConfig().getDrawable(), null, "package", before, after);
                    }
                    List<String> overlays = new ArrayList<>();
                    if (!TextUtils.isEmpty(overlayThemeFile)) {
                        overlays.add(overlayThemeFile);
                    }

                    String readTheme = config.readTheme();
                    if (readArticleIds.contains(article.getId()) && !TextUtils.isEmpty(readTheme)) {
                        overlays.add(readTheme);
                    }
                    items.add(new ArticleSectionItem(article, sectionIdentifier, getGroupKey(), template, templateReference, overlays, dividerDecorationConfig, context));
                }
            }
        }
        return items;
    }

    private SectionItem createCoverItem(PropertyObject aPackage, String overlayThemeFile) {
        PropertyObject coverArticle = aPackage.optPropertyObject("coverArticle");
        String groupKey = TextUtils.isEmpty(config.getCoverDetailTemplate()) ? "packageCover" : getGroupKey();
        if (coverArticle != null) {
            String template = layoutResolver.getValidTemplate(coverArticle, config.getCoverTemplatePrefix());
            String templateReference = layoutResolver.getValidTemplateReference(coverArticle, config.getCoverTemplatePrefix());
            return new PackageCoverSectionItem(aPackage, sectionIdentifier, template, templateReference, groupKey, config.getCoverDetailTemplate(), overlayThemeFile, context);
        }
        // TODO Templates?
        return new EmptyPackageCoverSectionItem(aPackage, sectionIdentifier, groupKey, overlayThemeFile);
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
    public void pause() {
        if (disposables.size() > 0 && currentObjects.size() > 0) {
            changeManager.unsubscribe(currentObjects);
        }
        disposables.clear();
    }

    public Observable<DataSourceResponse> observeResponse() {
        return publishRelay;
    }

    @Override
    public Set<String> groupKeys() {
        HashSet<String> keys = new HashSet<>();
        keys.add(getGroupKey());
        keys.add("cover");
        return keys;
    }

    private String getGroupKey() {
        return config.getGroup() != null ? config.getGroup() : DEFAULT_GROUP_KEY;
    }
}
