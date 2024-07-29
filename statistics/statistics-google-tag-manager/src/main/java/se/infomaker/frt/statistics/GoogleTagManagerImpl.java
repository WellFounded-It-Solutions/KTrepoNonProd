package se.infomaker.frt.statistics;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import timber.log.Timber;

public class GoogleTagManagerImpl implements StatisticsManager.StatisticsService {

    private static final String TAG = "GoogleTagManagerImpl";
    private String containerId;
    private FirebaseAnalyticsService firebaseService;

    @Override
    public String getIdentifier() { return "GoogleTagManager"; }

    @Override
    public void init(Context context, Map<String, Object> config) {
        this.firebaseService = setupFirebaseAnalytics(context, config);
    }

    private FirebaseAnalyticsService getFirebaseService() {
        for (StatisticsManager.StatisticsService service : StatisticsManager.getInstance().registeredServices()) {
            if (service.getIdentifier().equals("FirebaseAnalytics")) {
                return (FirebaseAnalyticsService) service;
            }
        }
        return null;
    }

    private FirebaseAnalyticsService setupFirebaseAnalytics(Context context, Map<String, Object> config) {
        Timber.w("Google Tag Manager is deprecated; use FirebaseAnalytics in future.");
        // Handle case when we have a registered FBA service
        FirebaseAnalyticsService service = getFirebaseService();
        if (service != null) {
            return service;
        }

        // Handle case where we have a FBA config but no instantiated FBA service
        ArrayList statisticsProviders = StatisticsManager.getInstance().getStatisticsProviders();
        Map<String, Object> rawConfig = null;
        if (statisticsProviders != null) {
            for (Object statisticsProvider : statisticsProviders) {
                Map<String, Object> provider = (Map<String, Object>) statisticsProvider;
                if (provider != null) {
                    if (provider.get("provider") == "FirebaseAnalytics") {
                        return null;
                    }
                }
            }
        }

        // Handle case where we have no configured FBA service and no FBA config
        service = new FirebaseAnalyticsService();
        service.init(context, config != null ? config : Collections.emptyMap());
        return service;
    }

    @Override
    public void logEvent(StatisticsEvent event) {
        Timber.d("ReceivedFirebaseEvent: %s", event);
        if (this.firebaseService == null) {
            this.firebaseService = getFirebaseService();
        }
        this.firebaseService.logEvent(event);
    }

    @Override
    public void globalAttributesUpdated(@NonNull Map<String, Object> globalAttributes) {
        // NOOP
    }
}
