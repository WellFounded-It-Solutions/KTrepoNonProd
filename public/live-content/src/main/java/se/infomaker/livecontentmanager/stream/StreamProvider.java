package se.infomaker.livecontentmanager.stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import se.infomaker.livecontentmanager.config.LiveContentConfig;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.livecontentmanager.query.QueryManager;
import se.infomaker.livecontentmanager.query.runnable.RunnableHandlerFactory;

@Singleton
public class StreamProvider {
    private final Map<String, HitsListStream> streams = new HashMap<>();

    private final QueryManager queryManager;
    private final RunnableHandlerFactory runnableHandlerFactory;

    @Inject
    public StreamProvider(QueryManager queryManager, RunnableHandlerFactory runnableHandlerFactory) {
        this.queryManager = queryManager;
        this.runnableHandlerFactory = runnableHandlerFactory;
    }

    public synchronized HitsListStream provide(PropertyObjectParser parser, LiveContentConfig config, String properties, String type, List<QueryFilter> filters) {
        String key = keyForCombination(config, properties, filters);
        if (streams.containsKey(key)) {
            return streams.get(key);
        } else {
            HitsListStream stream = new HitsListStream(queryManager, runnableHandlerFactory.create(), parser, config, properties, filters, type);
            streams.put(key, stream);
            return stream;
        }
    }

    private String keyForCombination(LiveContentConfig config, String properties, List<QueryFilter> filters) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("url:");
        keyBuilder.append(config.getLiveContentUrl());
        keyBuilder.append("basequery:");
        keyBuilder.append(config.getSearch().getBaseQuery());
        keyBuilder.append("properties:");
        keyBuilder.append(properties);
        keyBuilder.append("filters:");
        if (filters != null) {
            Collections.sort(filters, (queryFilter, t1) -> queryFilter.identifier().compareTo(t1.identifier()));
            for (QueryFilter filter : filters) {
                keyBuilder.append(filter.identifier());
            }
        }
        return keyBuilder.toString();
    }
}
