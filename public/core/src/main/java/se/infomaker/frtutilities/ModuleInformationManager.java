package se.infomaker.frtutilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by magnusekstrom on 01/06/16.
 */
public class ModuleInformationManager {
    private static ModuleInformationManager mInstance = null;

    private final Map<String, ModuleInformation> mModuleInformations = new HashMap<>();

    private final Map<String, NewModuleInformation> NewmModuleInformations = new HashMap<>();

    public ModuleInformationManager() {

    }

    @NonNull
    public static ModuleInformationManager getInstance() {
        if (mInstance == null) {
            mInstance = new ModuleInformationManager();
        }
        return mInstance;
    }

    public void clear() {
        mModuleInformations.clear();
        NewmModuleInformations.clear();
    }

    public void addModuleInformation(String identifier, String title, String name, String promotion) {
        mModuleInformations.put(identifier, new ModuleInformation(identifier, title, name, promotion));
    }

    public void addNewModuleInformation(String identifier, String title, String parent, String name, String promotion) {
        NewmModuleInformations.put(identifier, new NewModuleInformation(identifier, title, name, parent, promotion));
    }

    @Nullable
    public ModuleInformation getModuleInformation(String identifier) {
        return mModuleInformations.get(identifier);
    }

    @Nullable
    public String getModuleName(String identifier) {
        if (mModuleInformations.containsKey(identifier)) {
            return mModuleInformations.get(identifier).getName();
        }
        return null;
    }

    @Nullable
    public String getModuleTitle(String identifier) {
        if (mModuleInformations.containsKey(identifier)) {
            return mModuleInformations.get(identifier).getTitle();
        }
        return null;
    }

    @Nullable
    public String getModuleParent(String identifier) {
        if (NewmModuleInformations.containsKey(identifier)) {
            return NewmModuleInformations.get(identifier).getParent();
        }
        return null;
    }
}
