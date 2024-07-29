package se.infomaker.livecontentui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import se.infomaker.livecontentmanager.config.LiveContentConfig;
import se.infomaker.livecontentmanager.config.PropertyConfig;
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.livecontentmanager.stream.HitsListStream;
import se.infomaker.livecontentmanager.stream.StreamProvider;
import timber.log.Timber;

@Singleton
public class LiveContentStreamProvider {

    private final StreamProvider streamProvider;

    @Inject
    public LiveContentStreamProvider(StreamProvider streamProvider) {
        this.streamProvider = streamProvider;
    }

    public HitsListStream provide(LiveContentConfig config, String properties, List<QueryFilter> filters) {
        HashMap<String, Map<String, PropertyConfig>> typePropertyMap = config.getTypePropertyMap();
        if (typePropertyMap == null) {
            typePropertyMap = new HashMap<>();
        }

        Map<String, String> typeDescriptionTemplate = config.getTypeDescriptionTemplate();
        if (typeDescriptionTemplate == null) {
            typeDescriptionTemplate = new HashMap<>();
        }
        DefaultPropertyObjectParser parser = new DefaultPropertyObjectParser(typePropertyMap, typeDescriptionTemplate, config.getTransformSettings());
        HitsListStream stream = streamProvider.provide(parser, config, properties, config.getDefaultPropertyMap(), filters);
        Timber.d("Provided " + stream + " from " + this);
        return stream;
    }
}
