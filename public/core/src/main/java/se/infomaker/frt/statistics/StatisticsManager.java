package se.infomaker.frt.statistics;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.infomaker.frt.statistics.blacklist.FeatureToggle;
import timber.log.Timber;

public class StatisticsManager {
    private static StatisticsManager mInstance = null;

    private List<StatisticsService> mServices = new ArrayList<>();
    private Map<String, Object> mGlobalAttributes = new HashMap<>();
    private Set<StatisticsEventInterceptor> interceptors = new HashSet<>();
    private FeatureToggle blackList;
    private ArrayList statisticsProviders;

    public static StatisticsManager getInstance() {
        if (mInstance == null) {
            mInstance = new StatisticsManager();
        }
        return mInstance;
    }

    public StatisticsManager() {

    }

    public void setStatisticsProviders(ArrayList providers) {
        statisticsProviders = providers;
    }

    public ArrayList getStatisticsProviders(){
        return statisticsProviders;
    }

    public void setBlackList(FeatureToggle blackList) {
        this.blackList = blackList;
    }

    public void registerInterceptor(StatisticsEventInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public void removeInterceptor(StatisticsEventInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

    public void registerService(StatisticsService service) {
        mServices.add(service);
    }

    public void addGlobalAttribute(String key, Object value) {
        mGlobalAttributes.put(key, value);
        notifyServices();
    }

    public void removeGlobalAttribute(String key) {
        mGlobalAttributes.remove(key);
        notifyServices();
    }

    public void clearServices() {
        mServices.clear();
    }

    public List<StatisticsService> registeredServices() {
        return mServices;
    }

    public void logEvent(StatisticsEvent event) {

        Timber.e("StatisticsManager Event: %s", event);

        StringBuilder sb = new StringBuilder("Log event ").append(event.getEventName());
        if (event.getAttributes().size() > 0) {
            sb.append(" with values");
            for (Map.Entry<String, Object> entry : event.getAttributes().entrySet()) {
                sb.append(", ").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        Timber.d(sb.toString());
        event.addAttributes(mGlobalAttributes);

        for (StatisticsEventInterceptor interceptor : interceptors) {
            event = interceptor.onEvent(event);
        }

        for (StatisticsService service : mServices) {
            if (blackList == null) {
                service.logEvent(event);
            }
            else if (blackList.isEnabled(service.getIdentifier())) {
                service.logEvent(event);
            }
        }
    }

    private void notifyServices() {
        for (StatisticsService service : mServices) {
            service.globalAttributesUpdated(mGlobalAttributes);
        }
    }

    public interface StatisticsService {
        void init(Context context, Map<String, Object> config);
        String getIdentifier();
        void logEvent(StatisticsEvent event);
        void globalAttributesUpdated(@NonNull Map<String, Object> globalAttributes);
    }
}
