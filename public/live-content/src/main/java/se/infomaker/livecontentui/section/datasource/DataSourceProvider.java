package se.infomaker.livecontentui.section.datasource;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import se.infomaker.livecontentmanager.query.QueryManager;
import se.infomaker.livecontentmanager.query.lcc.BroadcastObjectChangeManager;
import se.infomaker.livecontentui.section.BehaviorResolver;
import se.infomaker.livecontentui.section.LayoutResolver;
import se.infomaker.livecontentui.section.configuration.Orientation;
import se.infomaker.livecontentui.section.configuration.SectionConfigWrapper;
import se.infomaker.livecontentui.section.configuration.SectionedLiveContentUIConfig;
import se.infomaker.livecontentui.section.datasource.list.ListDataProvider;
import se.infomaker.livecontentui.section.datasource.list.ListSectionConfig;
import se.infomaker.livecontentui.section.datasource.newspackage.PackageDataProvider;
import se.infomaker.livecontentui.section.datasource.newspackage.PackageSectionConfig;
import se.infomaker.livecontentui.section.datasource.newspackagecover.PackageCoverSectionConfig;
import se.infomaker.livecontentui.section.datasource.search.SearchDataProvider;
import se.infomaker.livecontentui.section.datasource.search.SearchSectionConfig;

public class DataSourceProvider {

    private final QueryManager queryManager;
    private final SectionedLiveContentUIConfig liveContentUIConfig;
    private final LayoutResolver layoutResolver;
    private final BehaviorResolver behaviorResolver;
    private final Map<Object, DataSource> providers = new HashMap<>();
    private final BroadcastObjectChangeManager broadcastObjectChangeManager;

    @AssistedInject
    public DataSourceProvider(BroadcastObjectChangeManager broadcastObjectChangeManager, QueryManager queryManager, @Assisted SectionedLiveContentUIConfig liveContentUIConfig) {
        this.broadcastObjectChangeManager = broadcastObjectChangeManager;
        this.queryManager = queryManager;
        this.liveContentUIConfig = liveContentUIConfig;
        layoutResolver = new LayoutResolver(liveContentUIConfig.getTemplates());
        behaviorResolver = new BehaviorResolver(liveContentUIConfig.getContentTypeTemplates());
    }

    public synchronized DataSource getSource(SectionConfigWrapper config, Orientation forceOrientation) {
        Orientation orientation = forceOrientation != null ? forceOrientation : config.getLayout();
        Object key = SectionConfigWrapper.createKey(config, orientation);
        if (!providers.containsKey(key)) {
            providers.put(key, create(config, orientation));
        }
        return providers.get(key);
    }

    @Nullable
    private DataSource create(SectionConfigWrapper config, Orientation orientation) {
        if (config == null || config.getType() == null) {
            return null;
        }
        switch (config.getType()) {
            case "search": {
                return new SearchDataProvider(layoutResolver, liveContentUIConfig.getLiveContent(), config.getSectionIdentifier(), config.getConfiguration(SearchSectionConfig.class), queryManager, broadcastObjectChangeManager, config.getContext());
            }
            case "package": {
                return new PackageDataProvider(layoutResolver, liveContentUIConfig.getLiveContent(), config.getSectionIdentifier(), config.getConfiguration(PackageSectionConfig.class), queryManager, broadcastObjectChangeManager, config.getContext());
            }
            case "packageCover": {
                return new PackageDataProvider(layoutResolver, liveContentUIConfig.getLiveContent(), config.getSectionIdentifier(), config.getConfiguration(PackageCoverSectionConfig.class), queryManager, broadcastObjectChangeManager, config.getContext());
            }
            case "list": {
                return new ListDataProvider(layoutResolver, behaviorResolver, liveContentUIConfig.getLiveContent(), config.getSectionIdentifier(), config.getConfiguration(ListSectionConfig.class), queryManager, broadcastObjectChangeManager, config.getContext(), orientation, config.getExtra());
            }
        }
        return null;
    }


}
