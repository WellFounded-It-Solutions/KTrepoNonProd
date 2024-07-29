package se.infomaker.iap.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.ui.view.LayoutInflaterViewFactory;
import se.infomaker.iap.ui.view.ViewFactory;

public abstract class ModuleAwareFragment extends Fragment {

    private static final String MODULE_ID = "moduleId";
    private String moduleId;
    private LayoutInflaterViewFactory viewFactory;

    protected static Bundle createModuleArguments(String moduleId) {
        Bundle arguments = new Bundle();
        arguments.putString(MODULE_ID, moduleId);
        return arguments;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            moduleId = arguments.getString(MODULE_ID);
        }
    }

    @SuppressWarnings("unused")
    public String getModuleId() {
        return moduleId;
    }

    protected Theme getModuleTheme() {
        return ThemeManager.getInstance(getActivity()).getModuleTheme(moduleId);
    }

    @NonNull
    protected ResourceManager getResourceManager() {
        return new ResourceManager(getActivity(), moduleId);
    }

    protected ViewFactory getViewFactory() {
        if (viewFactory == null) {
            viewFactory = new LayoutInflaterViewFactory(LayoutInflater.from(getActivity()), getResourceManager());
        }
        return viewFactory;
    }
}
