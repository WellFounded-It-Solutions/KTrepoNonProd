package se.infomaker.frt.statistics;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.analytics.HitBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import se.infomaker.frt.statistics.googleanalytics.Filter;
import se.infomaker.frt.statistics.googleanalytics.GAConfig;
import se.infomaker.frt.statistics.googleanalytics.LegacyMapping;
import se.infomaker.frt.statistics.googleanalytics.Mapping;
import se.infomaker.googleanalytics.EventBuilder;
import se.infomaker.googleanalytics.Tracker;
import se.infomaker.googleanalytics.di.TrackerFactory;
import timber.log.Timber;

/**
 * Created by magnusekstrom on 19/04/16.
 */
public class GoogleAnalyticsService implements StatisticsManager.StatisticsService {

    private Tracker mTracker;
    private GAConfig config;
    private Random random = new Random();

    @Override
    public String getIdentifier() {
        return "GoogleAnalytics:" + config.getTrackingId();
    }

    @Override
    public void init(Context context, Map<String, Object> rawConfig) {
        if (rawConfig== null) {
            return;
        }
        this.config = GAConfig.fromMap(rawConfig);


        if (TextUtils.isEmpty(config.getTrackingId())) {
            Timber.w("No tracking id found in config");
            return;
        }

        GaServiceEntryPoint entryPoint = EntryPointAccessors.fromApplication(context, GaServiceEntryPoint.class);
        mTracker = entryPoint.trackerFactory().create(config.getTrackingId());
    }

    @Override
    public void logEvent(StatisticsEvent event) {
        if (config.getFilter() != null) {
            List<Filter> filters = config.getFilter().get(event.getEventName());
            if (filters != null) {
                boolean shouldReport = true;
                for (Filter filter : filters) {
                    shouldReport = shouldReport && filter.evaluate(event);
                }
                if (!shouldReport) {
                    return;
                }
            }
        }

        if (config.getMapping() != null)  {
            List<Mapping> mappings = config.getMapping().get(event.getEventName());
            if (mappings != null) {
                EventBuilder builder = new EventBuilder();
                List<Mapping> commonMapping = config.getCommonMapping();
                if (commonMapping != null) {
                    for (Mapping mapping : commonMapping) {
                        builder.set(mapping.getVariable(), mapping.resolveTemplate(event));
                    }
                }
                for (Mapping mapping : mappings) {
                    builder.set(mapping.getVariable(), mapping.resolveTemplate(event));
                }
                int cacheBuster =  random.nextInt() & Integer.MAX_VALUE;
                builder.set("&z", "" + cacheBuster );
                mTracker.send(builder.build());
            }
        }
        else if (config.getEvents() != null) {
            legacyEventMapping(event);
        }
        else {
            Timber.w("No events or mapping configured");
        }
    }

    private void legacyEventMapping(StatisticsEvent event) {
        LegacyMapping mapping;

        if (config.getEvents() != null && config.getEvents().containsKey(event.getEventName())) {
            mapping = config.getEvents().get(event.getEventName());

            if (mapping == null || mapping.getEventMapping() == null) {
                return;

            }
            Map<String, String> eventMapping = mapping.getEventMapping();
            String category;
            if (eventMapping.containsKey("category")) {
                category = eventMapping.get("category");
            } else {
                return;
            }

            String action;
            if (eventMapping.containsKey("action")) {
                action = eventMapping.get("action");
            } else {
                return;
            }

            String formattedLabel = "";
            if (eventMapping.containsKey("label")) {
                String label = eventMapping.get("label");
                for (Map.Entry entry : event.getAttributes().entrySet()) {
                    label = label.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
                }
                formattedLabel = label;
            }

            Map<String, Object> eventAttributes = event.getAttributes();
            eventAttributes.put("eventType", event.getEventName());

            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setLabel(formattedLabel);

            String userId;
            if (event.getAttributes().containsKey("userID")) {
                userId = (String) event.getAttributes().get("userID");
                mTracker.setClientId(userId);
            }

            List<String> cdMapping = mapping.getCdMapping();
            if (cdMapping != null) {
                for (int i = 0; i < cdMapping.size(); i++) {
                    if (eventAttributes.containsKey(cdMapping.get(i))) {
                        String value = "" + eventAttributes.get(cdMapping.get(i));
                        builder.setCustomDimension(i + 1, value);
                    }
                }
            }

            mTracker.send(normalizeKeys(builder.build()));
        }
    }

    private static Map<String, String> normalizeKeys(Map<String, String> map) {
        HashMap<String, String> out = new HashMap<>(map.size());
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (key.startsWith("&")) {
                out.put(key.substring(1), map.get(key));
            }
            else {
                out.put(key, map.get(key));
            }
        }
        return out;
    }

    @Override
    public void globalAttributesUpdated(@NonNull Map<String, Object> globalAttributes) {
        // NOP
    }

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface GaServiceEntryPoint {
        TrackerFactory trackerFactory();
    }
}
