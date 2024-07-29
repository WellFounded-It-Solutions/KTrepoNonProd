package se.infomaker.frtutilities;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.infomaker.frtutilities.mainmenutoolbarsettings.ToolbarConfig;
import se.infomaker.frtutilities.meta.ValueProvider;
import se.infomaker.frtutilities.runtimeconfiguration.OnConfigChangeListener;
import se.infomaker.frtutilities.runtimeconfiguration.OnModuleConfigChangeListener;
import se.infomaker.frtutilities.runtimeconfiguration.RuntimeConfigManager;
import timber.log.Timber;

/**
 * Created by magnusekstrom on 19/05/16.
 */
public class ConfigManager implements OnConfigChangeListener {
    private static ConfigManager mInstance = null;

    private Context mContext;
    private final Handler handler = new Handler();

    private Map<String, ConfigPropertyFinder> mConfigPropertyFinders = new HashMap<>();
    private Map<String, String> mConfigs = new HashMap<>();

    private MainMenuConfig mMainMenuConfig;
    private final Object lock = new Object();
    private Map<String, MainMenuItem> mainMenuModules;
    private String pathPrefix;
    private RuntimeConfigManager runtimeConfig;
    private Set<OnModuleConfigChangeListener> listeners = new HashSet<>();
    private Set<OnConfigChangeListener> changeListeners = new HashSet<>();

    public ConfigManager() {
    }

    public ConfigManager(String pathPrefix) {
        this.pathPrefix = pathPrefix + "/";
    }

    public static ConfigManager getInstance(Context context) {
        ConfigManager instance = getInstance();
        if (instance.mContext == null) {
            instance.init(context);
        }
        return instance;
    }

    public void registerRuntimeConfigManager(RuntimeConfigManager runtimeConfigManager) {
        if (this.runtimeConfig != null) {
            this.runtimeConfig.removeOnChangeListener(this);
        }
        this.runtimeConfig = runtimeConfigManager;
        runtimeConfigManager.registerOnChangeListener(this);
        clearConfiguration();
        loadConfiguration();
    }

    public static ConfigManager getInstance() {
        if (mInstance == null) {
            mInstance = new ConfigManager();
        }
        return mInstance;
    }

    public void addOnConfigChangeListener(OnConfigChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Use correct casing version of this method.
     * @param listener
     */
    @Deprecated
    public void removeOnConfigCHangeListener(OnConfigChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void removeOnConfigChangeListener(OnConfigChangeListener listener) {
        changeListeners.remove(listener);
    }

    @Override
    public Set<String> onChange(List<String> updated, List<String> removed) {
        Timber.d("Configuration changed updated: " + updated + " removed: " + removed);
        clearConfiguration();
        loadConfiguration();
        HashSet<String> handled = new HashSet<>();
        for (OnConfigChangeListener listener : changeListeners) {
            handled.addAll(listener.onChange(updated, removed));
        }
        updated.removeAll(handled);
        removed.removeAll(handled);

        handler.post(() -> {
            HashSet<String> modules = new HashSet<>();
            for (String file : updated) {
                modules.add(file.split("/")[0]);
            }
            for (String file : removed) {
                modules.add(file.split("/")[0]);
            }

            if (modules.contains("shared") && mMainMenuConfig != null) {
                for (MainMenuItem item : mMainMenuConfig.getMainMenuItems()){
                    modules.add(item.getId());
                }
            }

            if (modules.size() > 0) {
                for (OnModuleConfigChangeListener listener : listeners) {
                    listener.onModuleConfigUpdated(modules);
                }
            }
        });
        return Collections.EMPTY_SET;
    }

    public void registerOnModuleConfigChangeListener(OnModuleConfigChangeListener listener) {
        listeners.add(listener);
    }

    public void removeOnModuleConfigChangeListener(OnModuleConfigChangeListener listener) {
        listeners.remove(listener);
    }

    public void init(Context context) {
        if (mContext != null) {
            // Already loaded
            Timber.d("ConfigLoaded");
            return;
        }
        mContext = context.getApplicationContext();
        loadConfiguration();
    }

    private void clearConfiguration() {
        mConfigPropertyFinders.clear();
        mConfigs.clear();
        mMainMenuConfig = null;
        mainMenuModules = null;
        ModuleInformationManager.getInstance().clear();
    }

    private void loadConfiguration() {
        Gson gson = new Gson();

        mMainMenuConfig = gson.fromJson(loadJSON("shared/configuration/modules_config.json"), MainMenuConfig.class);

        registerConfig("global", loadJSON("shared/configuration/global_config.json"));

        registerConfig("core", loadJSON("shared/configuration/global_config.json"));
        mergeConfig("core", loadJSON("shared/configuration/core_config.json"));

        if (mMainMenuConfig != null && mMainMenuConfig.getMainMenuItems() != null) {
            for (MainMenuItem mainMenuItem : mMainMenuConfig.getMainMenuItems()) {

                String moduleId = mainMenuItem.getId();
                String moduleName = mainMenuItem.getModuleName();

                Timber.d("CManagerLog ModuleName: %s, ModuleId: %s", moduleName, moduleId);

                String configIdentifier = moduleName + moduleId;
                loadModuleConfiguration(configIdentifier, moduleId, moduleName);

                String title = mainMenuItem.getTitle();
                String promotion = mainMenuItem.getPromotion();
                ModuleInformationManager.getInstance().addModuleInformation(moduleId, title, moduleName, promotion);
            }
        }
    }

    private void loadModuleConfiguration(@NonNull String identifier, @NonNull String moduleIdentifier, @Nullable String moduleName) {

        Timber.d("loadModuleConfig identifier: %s, moduleIdentifier: %s, moduleName: %s", identifier, moduleIdentifier, moduleName);
        
        List<String> configFiles = new ArrayList<>();
        configFiles.add("shared/configuration/global_config.json");
        if (!TextUtils.isEmpty(moduleName)) {
            configFiles.add("shared/configuration/" + moduleName + "_config.json");
        }
        configFiles.add(moduleIdentifier + "/configuration/config.json");

        String first = configFiles.get(0);
        registerConfig(identifier, loadJSON(first));

        List<String> missingConfigFiles = new ArrayList<>();
        for (int i = 1; i < configFiles.size(); i++) {
            String configFile = configFiles.get(i);
            mergeConfig(identifier, loadJSON(configFile, e -> {
                if (e instanceof FileNotFoundException) {
                    missingConfigFiles.add(configFile);
                }
                else {
                    Timber.e(e);
                }
            }));
        }

        configFiles.removeAll(missingConfigFiles);
        StringBuilder debugStringBuilder = new StringBuilder();
        for (int i = 0; i < configFiles.size(); i++) {
            debugStringBuilder.append(configFiles.get(i));
            if (i < configFiles.size() - 1) {
                debugStringBuilder.append(", ");
            }
        }
        Timber.d("%s config used files: %s", identifier, debugStringBuilder.toString());
    }

    @Nullable
    public String loadJSON(String key) {
        return loadJSON(key, null);
    }

    @Nullable
    public String loadJSON(String key, FileUtil.OnErrorListener listener) {
        String path = getPath(key);
        if (runtimeConfig != null) {
            switch (runtimeConfig.status(path)) {
                case CHANGED: return runtimeConfig.get(path);
                case DELETED: return null;
                case UNCHANGED:  break;
            }
        }
        return FileUtil.loadJSONFromAssets(mContext, path, listener);
    }

    @NonNull
    private String getPath(String path) {
        return pathPrefix != null ? pathPrefix + path : path;
    }

    public MainMenuConfig getMainMenuConfig() {
        return mMainMenuConfig;
    }

    public MainMenuItem getMenuItem(String moduleId) {
        loadMainMenuModules();
        return mainMenuModules.get(moduleId);
    }

    private void loadMainMenuModules() {
        if (mainMenuModules == null) {
            synchronized (lock) {
                mainMenuModules = new HashMap<>();
                if (mMainMenuConfig != null) {
                    for (MainMenuItem item : mMainMenuConfig.getMainMenuItems()) {
                        mainMenuModules.put(item.getId(), item);
                    }
                }
            }
        }
    }

    @Nullable
    public String getLinkedModuleName(String moduleId) {
        loadMainMenuModules();
        for (MainMenuItem mainMenuModule: mainMenuModules.values()) {
            List<ToolbarConfig.ButtonConfig> buttons = mainMenuModule.getToolbarConfig().getButtons();
            if (buttons != null) {
                for (ToolbarConfig.ButtonConfig button : buttons) {
                    if (button.getClick() != null && button.getClick().has("parameters")) {
                        Object parameters = button.getClick().get("parameters");
                        if (parameters instanceof JsonObject) {
                            JsonPrimitive clickModuleId = ((JsonObject) parameters).getAsJsonPrimitive("moduleId");
                            if (clickModuleId != null) {
                                String clickModuleIdAsString = clickModuleId.getAsString();
                                if (clickModuleIdAsString.equals(moduleId)) {
                                    JsonPrimitive clickModuleName = ((JsonObject) parameters).getAsJsonPrimitive("moduleName");
                                    return clickModuleName.getAsString();
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public GlobalConfig getGlobalConfig() {
        return getGlobalConfig(new GsonBuilder().create());
    }

    public GlobalConfig getGlobalConfig(Gson gson) {
        if (mConfigs.containsKey("global")) {
            return gson.fromJson(mConfigs.get("global"), GlobalConfig.class);
        }
        return getConfig("global", GlobalConfig.class, gson);
    }

    public GlobalConfig getGlobalConfig(String identifier) {
        return getGlobalConfig(identifier, new GsonBuilder().create());
    }

    public GlobalConfig getGlobalConfig(String identifier, Gson gson) {
        if (mConfigs.containsKey(identifier)) {
            return gson.fromJson(mConfigs.get(identifier), GlobalConfig.class);
        }
        return getConfig(identifier, GlobalConfig.class, gson);
    }

    public <T> T getConfig(String identifier, Class<T> classOfT) {
        return getConfig(identifier, classOfT, new GsonBuilder().create());
    }

    public <T> T getConfig(String identifier, Class<T> classOfT, @Nullable String overlay) {
        Timber.e("GetConfig-a1");
        return getConfig(identifier, classOfT, new GsonBuilder().create(), overlay);
    }

    public <T> T getConfig(String identifier, Class<T> classOfT, Gson gson) {
        Timber.e("GetConfig-a2");
        return getConfig(identifier, classOfT, gson, null);
    }

    public <T> T getConfig(String identifier, Class<T> classOfT, Gson gson, @Nullable String overlay) {
        Timber.e("GetConfig1");

        if (mConfigs.containsKey(identifier + (overlay != null ? overlay : ""))) {
            return gson.fromJson(mConfigs.get(identifier), classOfT);
        } else {
            MainMenuItem menuItem = getMenuItem(identifier);
            String moduleName;
            if (menuItem != null) {
                moduleName = menuItem.getModuleName();
            }
            else {
                moduleName = getLinkedModuleName(identifier);
            }
            return getConfig(moduleName != null ? moduleName : "", identifier, classOfT, overlay, gson);
        }
    }

    public <T> T getConfig(String moduleName, String moduleIdentifier, Class<T> classOfT) {
        return getConfig(moduleName, moduleIdentifier, classOfT, new GsonBuilder().create());
    }

    public <T> T getConfig(String moduleName, String moduleIdentifier, Class<T> classOfT, @Nullable String overlay) {
        Timber.e("GetConfig2");
        return getConfig(moduleName, moduleIdentifier, classOfT, overlay, new Gson());
    }

    public <T> T getConfig(String moduleName, String moduleIdentifier, Class<T> classOfT, Gson gson) {
        Timber.e("GetConfig3");
        return getConfig(moduleName, moduleIdentifier, classOfT, null, gson);
    }

    public <T> T getConfig(String moduleName, String moduleIdentifier, Class<T> classOfT, @Nullable String overlay, Gson gson) {
        Timber.d("GetConfigLast");
        String identifier = (moduleName != null ? moduleName : "") + moduleIdentifier + (overlay != null ? overlay : "");
        if (!mConfigs.containsKey(identifier)) {
            loadModuleConfiguration(identifier, moduleIdentifier, moduleName);
            if (overlay != null) {
                mergeConfig(identifier, overlay);
            }
        }

        Timber.d("Identifier: %s", identifier);
        Timber.d("MConfig: %s", mConfigs);
        Timber.d("mConfigsIdentifier: %s", mConfigs.get(identifier));

        return gson.fromJson(mConfigs.get(identifier), classOfT);
    }

    public Map<String, Object> getConfig(String identifier) {
        if (mConfigPropertyFinders.containsKey(identifier)) {
            if (mConfigPropertyFinders.get(identifier).getConfig() != null) {
                return mConfigPropertyFinders.get(identifier).getConfig();
            }
        }
        return null;
    }

    public ConfigPropertyFinder getPropertyFinder(String identifier) {
        if (mConfigs.containsKey(identifier)) {
            return mConfigPropertyFinders.get(identifier);
        }
        return null;
    }

    public <T> T getProperty(String identifier, Class<T> type, String key) {
        if (mConfigs.containsKey(identifier)) {
            return mConfigPropertyFinders.get(identifier).getProperty(type, key);
        }
        return null;
    }

    public <T> T getProperty(String identifier, T defaultValue, Class<T> type, String key) {
        if (mConfigPropertyFinders.containsKey(identifier)) {
            return mConfigPropertyFinders.get(identifier).getProperty(defaultValue, type, key);
        }
        return defaultValue;
    }

    public <T> T getProperty(String identifier, Class<T> type, String... key) {
        if (mConfigPropertyFinders.containsKey(identifier)) {
            return mConfigPropertyFinders.get(identifier).getProperty(type, key);
        }
        return null;
    }

    public <T> T getProperty(String identifier, T defaultValue, Class<T> type, String... key) {
        if (mConfigPropertyFinders.containsKey(identifier)) {
            return mConfigPropertyFinders.get(identifier).getProperty(defaultValue, type, key);
        }
        return defaultValue;
    }

    public int getColor(String identifier, String key) {
        if (mConfigPropertyFinders.containsKey(identifier)) {
            return mConfigPropertyFinders.get(identifier).getColor(key);
        }
        return Color.BLACK;
    }

    public int getColor(String identifier, int defaultValue, String key) {
        if (mConfigPropertyFinders.containsKey(identifier)) {
            return mConfigPropertyFinders.get(identifier).getColor(key);
        }
        return defaultValue;
    }

    private void mergeConfig(String identifier, String config) {
        if (TextUtils.isEmpty(identifier) || TextUtils.isEmpty(config)) {
            return;
        }

        Timber.d("Merging " + identifier);
        Gson gson = new GsonBuilder().serializeNulls().create();

        if (mConfigs.containsKey(identifier)) {
            Map configToMergeMap = gson.fromJson(config, HashMap.class);
            Map configToMergeWithMap = gson.fromJson(mConfigs.get(identifier), HashMap.class);
            Map mergeResultMap = new HashMap(configToMergeWithMap);
            MapUtilKt.putRecursive(mergeResultMap, configToMergeMap);
            mConfigs.put(identifier, gson.toJson(mergeResultMap));

            Timber.d("New mergedMap for identifier %s: %s", identifier, mergeResultMap);

            // Log tabs with parent name
            // List<Map<String, String>> tabs = (List<Map<String, String>>) mergeResultMap.get("tabs");
            // if (tabs != null) {
            //     for (Map<String, String> tab : tabs) {
            //         if (tab.containsKey("parent")) {
            //             Timber.d("Tab: %s", tab);
            //         }
            //     }
            // }

        } else {
            mConfigs.put(identifier, config);
        }

        if (mConfigPropertyFinders.containsKey(identifier)) {
            ConfigPropertyFinder configPropertyFinder = mConfigPropertyFinders.get(identifier);
            configPropertyFinder.addConfig(gson.fromJson(config, HashMap.class));
        } else {
            mConfigPropertyFinders.put(identifier, new ConfigPropertyFinder(gson.fromJson(config, HashMap.class)));
        }
    }

    public void registerConfig(String identifier, String config) {
        if (config == null) {
            config = "{}";
        }
        mConfigs.put(identifier, config);
        Gson gson = new GsonBuilder().serializeNulls().create();
        mConfigPropertyFinders.put(identifier, new ConfigPropertyFinder(gson.fromJson(config, Map.class)));
    }

    public ValueProvider getValueProvider() {
        return new ConfigurationValueProvider();
    }

    String getConfigJson(String moduleId) {
        return mConfigs.get(moduleId);
    }
}
