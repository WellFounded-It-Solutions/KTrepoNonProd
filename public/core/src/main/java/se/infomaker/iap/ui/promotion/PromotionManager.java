package se.infomaker.iap.ui.promotion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import se.infomaker.iap.ui.config.ConfigLoader;
import se.infomaker.iap.ui.fragment.FragmentPresenter;
import timber.log.Timber;

public class PromotionManager {
    private static final String PROMOTION = "promotion";

    // Constructor ensures that we only hold application context
    @SuppressLint("StaticFieldLeak")
    private static PromotionManager INSTANCE;
    private final Context context;
    private Set<String> presented;
    private final HashMap<String, PromotionConfiguration> configCache = new HashMap<>();

    public static synchronized PromotionManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PromotionManager(context);
        }
        return INSTANCE;
    }

    private PromotionManager(Context context) {
        this.context = context.getApplicationContext();
        presented = new HashSet<>(getPreferences().getAll().keySet());
    }

    private PromotionConfiguration getConfiguration(String configFile) throws IOException {
        if (configCache.containsKey(configFile)) {
            return configCache.get(configFile);
        }
        PromotionConfiguration configuration = ConfigLoader.load(context.getAssets().open(configFile), PromotionConfiguration.class);
        configCache.put(configFile, configuration);
        return configuration;
    }

    public void forcePromote(FragmentPresenter presenter, String moduleId, String configuration) {
        try {
            promote(presenter, moduleId, getConfiguration(configuration), true);
        } catch (IOException e) {
            Timber.e(e, "Could not load configuration");
        }
    }

    /**
     * Display a module promotion
     * @param presenter presenter responsible of
     * @param moduleId moduleId to display promotion for
     * @param configuration config file describing the promotion
     * @return true if the module was promoted
     */
    public boolean promote(FragmentPresenter presenter, String moduleId, String configuration) {
        try {
            return promote(presenter, moduleId, getConfiguration(configuration), false);
        } catch (IOException e) {
            Timber.e(e, "Could not load configuration");
        }
        return false;
    }

    @SuppressWarnings("unused")
    public void forcePromote(FragmentPresenter presenter, String moduleId, PromotionConfiguration configuration) {
        promote(presenter, moduleId, configuration, true);
    }

    /**
     * Display a module promotion
     * @param presenter presenter responsible of
     * @param moduleId moduleId to display promotion for
     * @param configuration config file describing the promotion
     * @return true if the module was promoted
     */
    public boolean promote(FragmentPresenter presenter, String moduleId, PromotionConfiguration configuration) {
        return promote(presenter, moduleId, configuration, false);
    }

    private boolean promote(FragmentPresenter presenter, String moduleId, PromotionConfiguration configuration, boolean force) {
        if (!force && presented.contains(configuration.getId())) {
            Timber.d("%s already presented", configuration.getId());
            return false;
        }
        PromotionFragment fragment = PromotionFragment.createInstance(moduleId, configuration);
        presenter.presentFullScreen(fragment);
        return true;
    }

    public void setPresented(String promotionId) {
        presented.add(promotionId);
        getPreferences().edit().putBoolean(promotionId, true).apply();
    }

    public void clearPromoted() {
        presented = new HashSet<>();
        getPreferences().edit().clear().apply();
    }

    private SharedPreferences getPreferences() {
        return context.getSharedPreferences(PROMOTION, Context.MODE_PRIVATE);
    }
}
