package se.infomaker.frt.moduleinterface;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ModuleInformation;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.frtutilities.ResourceManager;
import timber.log.Timber;

/**
 * Created by magnusekstrom on 01/06/16.
 */
@AndroidEntryPoint
public abstract class BaseModule extends Fragment implements ModuleInterface {
    private static final String ARG_MODULE_IDENTIFIER = "moduleId";
    private static final String ARG_MODULE_TITLE = "title";
    private static final String ARG_MODULE_NAME = "moduleName";
    private static final String ARG_MODULE_PROMOTION = "promotion";

    private ModuleInformation mModuleInformation;
    private ResourceManager mResourceManager;

    @Inject ConfigManager configManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String moduleIdentifier = getArguments().getString(ARG_MODULE_IDENTIFIER);
            Timber.d("BaseModule MIdentifier: %s", moduleIdentifier);
            mModuleInformation = ModuleInformationManager.getInstance().getModuleInformation(moduleIdentifier);
            if (mModuleInformation == null) {
                mModuleInformation = fromArguments();
            }
            if (getContext() != null) {
                mResourceManager = new ResourceManager(getContext(), moduleIdentifier);
            }
        }
    }

    private ModuleInformation fromArguments() {
        Bundle arguments = getArguments();
        String id = arguments.getString(ARG_MODULE_IDENTIFIER);
        String title = arguments.getString(ARG_MODULE_TITLE);
        String name = arguments.getString(ARG_MODULE_NAME);
        String promotion = arguments.getString(ARG_MODULE_PROMOTION);
        return new ModuleInformation(id, title, name, promotion);
    }

    public ModuleInformation getModuleInformation() {
        return mModuleInformation;
    }

    public <T> T getModuleConfig(Class<T> classOfT) {
        return configManager.getConfig(getModuleIdentifier(), classOfT);
    }

    public <T> T getModuleConfig(Class<T> classOfT, Gson gson) {
        return configManager.getConfig(getModuleIdentifier(), classOfT, gson);
    }

    public String getPromotion() {
        return mModuleInformation.getPromotion();
    }

    public String getModuleIdentifier() {
        return mModuleInformation.getIdentifier();
    }

    public String getModuleTitle() {
        return mModuleInformation.getTitle();
    }

    public String getModuleName() {
        return mModuleInformation.getName();
    }

    public ResourceManager getResourceManager() {
        return mResourceManager;
    }
}
