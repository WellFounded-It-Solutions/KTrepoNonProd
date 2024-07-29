package se.infomaker.frt.module;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.infomaker.frt.moduleinterface.ModuleIntegration;
import se.infomaker.frt.moduleinterface.model.ModulesConfig;
import se.infomaker.frtutilities.FileUtil;
import se.infomaker.frtutilities.MainMenuItem;
import timber.log.Timber;

public class ModuleIntegrationProvider {
    private static final String MODULE_INTEGRATION_PACKAGE_NAME = "se.infomaker.frt.integration";
    private static final String MODULE_INTEGRATION_SUFFIX = "Integration";

    private static ModuleIntegrationProvider instance;
    private static final Object LOCK = new Object();
    private final List<ModuleIntegration> integrations = new ArrayList<>();

    private ModuleIntegrationProvider(Context context) {
        Gson gson = new Gson();
        ModulesConfig modulesConfig = gson.fromJson(FileUtil.loadJSONFromAssets(context, "shared/configuration/modules_config.json", e -> {
            Timber.w("No modules_config.json found, no module integrations will be created.");
        }), ModulesConfig.class);
        if (modulesConfig != null) {
            ArrayList<MainMenuItem> menuItems = modulesConfig.getMainMenuItems();
            if (menuItems != null) {
                for (MainMenuItem menuItem : menuItems) {
                    integrations.add(createModuleIntegration(context, menuItem.getModuleName(), menuItem.getId()));
                    Timber.e("LoadingConfig: Name: %s ID: %s", menuItem.getModuleName(), menuItem.getId());
                }
            }
        }
    }

    public static ModuleIntegrationProvider getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ModuleIntegrationProvider(context);
                }
            }
        }
        return instance;
    }

    /**
     * All module instances configured in modules_config.json are present. If the module does not
     * provide a ModuleIntegration implementation a placeholder is used
     * @return a list of module integrations
     */
    public List<ModuleIntegration> getIntegrationList() {
        return Collections.unmodifiableList(integrations);
    }

    public static ModuleIntegration createModuleIntegration(Context context, String moduleName, String moduleId) {

        String className = MODULE_INTEGRATION_PACKAGE_NAME + "." + moduleName + MODULE_INTEGRATION_SUFFIX;
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            return createPlaceholderIntegration(moduleId);
        }

        try {
            Constructor<?>constructor = clazz.getConstructor(Context.class, String.class);
            try {
                return (ModuleIntegration) constructor.newInstance(context, moduleId);
            }
            catch (Exception e) {
                Timber.e(e, "Could not create module integration");
            }
        } catch (NoSuchMethodException e) {
            // Try to use default constructor
            try {
                return (ModuleIntegration) clazz.getConstructor().newInstance();
            } catch (Exception e1) {
                Timber.e(e1, "Failed to create module integration");
            }
        }

        return createPlaceholderIntegration(moduleId);
    }

    @NonNull
    private static ModuleIntegration createPlaceholderIntegration(String moduleId) {
        return () -> moduleId;
    }
}
