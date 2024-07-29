package se.infomaker.iap.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.FileUtil;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.frtutilities.runtimeconfiguration.OnConfigChangeListener;
import se.infomaker.iap.theme.debug.ThemeDebugManager;
import se.infomaker.iap.theme.font.AssetFontLoader;
import timber.log.Timber;

/**
 * Them theme manager provides theme for app, module and extra layers on top of it
 */
public class ThemeManager implements OnConfigChangeListener {
    private static final String THEME_ASSET = "theme.json";

    // Constructor ensures that we only hold application context
    @SuppressLint("StaticFieldLeak")
    private static ThemeManager instance;
    private static final Object LOCK = new Object();
    private final Context context;
    private final AssetFontLoader fontLoader;

    private Theme appTheme;
    private Theme defaultTheme;
    private final Map<String, Theme> moduleThemes = new HashMap<>();
    private final HashSet<OnThemeUpdateListener>listeners = new HashSet<>();
    private final ThemeDebugManager debugManager;
    private boolean showDebug = false;
    private final CompositeDisposable garbage = new CompositeDisposable();

    private ThemeManager(Context context) {
        this.context = context.getApplicationContext();
        fontLoader = new AssetFontLoader(this.context, "shared/fonts");
        loadTheme();
        debugManager = new ThemeDebugManager(this.context);
        debugManager.setListener(isDebug -> {
            showDebug = isDebug;
            notifyThemeUpdated();
        });
        ConfigManager.getInstance(this.context).addOnConfigChangeListener(this);
    }

    public boolean showDebug() {
        return showDebug;
    }

    private void loadTheme() {
        try {
            defaultTheme = loadDefaultTheme(this.context);
        }
        catch (ThemeException e) {
            Timber.e(e, "Failed to parse default theme");
            defaultTheme = null;
        }

        // loads injected themes on top of default themes
        for (String definition : ThemeInjector.getInstance().getDefinitions()) {
            try {
                defaultTheme = loadThemeFromDefinition(new ResourceManager(this.context, ""), defaultTheme, definition);
            } catch (ThemeException e) {
                Timber.e(e, "Failed to parse app theme from definition: %s", definition);
            }
        }

        try {
            appTheme = loadTheme(new ResourceManager(this.context, ""), defaultTheme, "shared/configuration/" + THEME_ASSET);
        } catch (ThemeException e) {
            Timber.e(e, "Failed to parse app theme");
            appTheme = null;
        }
    }

    /**
     * Returns a shared instance of the theme manager
     * @param context used to access app context
     * @return a shared theme manager instance
     */
    public static ThemeManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ThemeManager(context);
                }
            }
        }
        return instance;
    }

    private Theme loadDefaultTheme(Context context) throws ThemeException {
        ResourceManager resourceManager = new ResourceManager(context, "");
        return loadTheme(resourceManager, null, "default_theme.json");
    }

    private Theme loadTheme(ResourceManager resourceManager, Theme parent, String asset) throws ThemeException {
        return loadTheme(resourceManager, parent, asset, null);
    }

    private Theme loadTheme(ResourceManager resourceManager, Theme parent, String asset, FileUtil.OnErrorListener onErrorListener) throws ThemeException {
        try {
            LayeredThemeBuilder builder = new LayeredThemeBuilder();
            if (parent instanceof LayeredTheme) {
                builder.setParent((LayeredTheme) parent);
            }
            String json = ConfigManager.getInstance(context).loadJSON(asset, onErrorListener);
            if (json != null && !json.isEmpty()) {
                builder.setDefinition(new JSONObject(json));
            }
            return builder.build(resourceManager, fontLoader);
        } catch (JSONException e) {
            throw new ThemeException("Failed to parse theme", e);
        } catch (Exception e) {
            throw new ThemeException("Failed to load theme", e);
        }
    }

    private Theme loadThemeFromDefinition(ResourceManager resourceManager, Theme parent, String definition) throws ThemeException {
        try {
            LayeredThemeBuilder builder = new LayeredThemeBuilder();
            if (parent instanceof LayeredTheme) {
                builder.setParent((LayeredTheme) parent);
            }
            builder.setDefinition(new JSONObject(definition)).build(resourceManager, fontLoader);
            return builder.build(resourceManager, fontLoader);
        } catch (JSONException e) {
            throw new ThemeException("Failed to parse theme", e);
        }
    }

    /**
     * The app global theme
     * @return app global theme
     */
    @SuppressWarnings("WeakerAccess")
    public Theme getAppTheme() {
        return appTheme != null ? appTheme : getDefaultTheme();
    }

    /**
     * Add an extra layer of theme on top of the extended theme
     * @param theme to extend
     * @param resourceManager to use when loading resources
     * @param definition to load in next layer
     * @return Theme instance whit an extra layer on top of provided theme
     */
    public Theme extendedFrom(Theme theme, ResourceManager resourceManager, JSONObject definition) {
        if (definition != null && theme instanceof LayeredTheme) {
            return new LayeredThemeBuilder().setParent((LayeredTheme) theme).setDefinition(definition).build(resourceManager, fontLoader);
        }
        return theme;
    }

    private Theme getDefaultTheme() {
        return defaultTheme != null ? defaultTheme : EmptyTheme.INSTANCE;
    }

    /**
     * Loads and caches the theme for a given module identifier
     *
     * @param moduleIdentifier to load theme for
     * @return the module theme if successfully loaded, or the app theme if failing
     */
    public Theme getModuleTheme(String moduleIdentifier) {
        if (TextUtils.isEmpty(moduleIdentifier)) {
            return getAppTheme();
        }
        if (moduleThemes.containsKey(moduleIdentifier)) {
            return moduleThemes.get(moduleIdentifier);
        }
        Theme theme;
        try {
            theme = loadTheme(new ResourceManager(context, moduleIdentifier), getAppTheme(), moduleIdentifier + "/configuration/" + THEME_ASSET, e -> {
                if (e instanceof FileNotFoundException) {
                    Timber.d("No module specific theme exists for %s, using app theme.", moduleIdentifier);
                }
                else {
                    Timber.e(e);
                }
            });
        } catch (ThemeException e) {
            theme = getAppTheme();
        }
        moduleThemes.put(moduleIdentifier, theme);
        return theme;
    }

    /**
     * Make sure we reload the theme on next access
     */
    static void reset() {
        instance = null;
    }

    @Override
    public Set<String> onChange(List<String> updated, List<String> removed) {
        HashSet<String> handled = new HashSet<>();
        for (String resource : updated) {
            if (resource.endsWith("/" + THEME_ASSET)) {
                handled.add(resource);
            }
        }
        for (String resource : removed) {
            if (resource.endsWith("/" + THEME_ASSET)) {
                handled.add(resource);
            }
        }
        if (handled.size() > 0) {
            moduleThemes.clear();
            loadTheme();
            notifyThemeUpdated();
        }
        return handled;
    }

    private void notifyThemeUpdated() {
        garbage.add(Observable.fromIterable(listeners)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(OnThemeUpdateListener::onThemeUpdated,
                        throwable -> Timber.e(throwable, "Failed to notify theme update")));
    }

    public void addOnUpdateListener(OnThemeUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeOnUpdateListener(OnThemeUpdateListener listener) {
        listeners.remove(listener);
    }
}
