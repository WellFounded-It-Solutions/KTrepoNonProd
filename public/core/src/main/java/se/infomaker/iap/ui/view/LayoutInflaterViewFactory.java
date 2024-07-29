package se.infomaker.iap.ui.view;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.ui.view.ViewFactory;
import timber.log.Timber;

public class LayoutInflaterViewFactory implements ViewFactory {
    private final LayoutInflater layoutInflater;
    private final ResourceManager resourceManager;

    public LayoutInflaterViewFactory(LayoutInflater layoutInflater, ResourceManager resourceManager) {
        this.layoutInflater = layoutInflater;
        this.resourceManager = resourceManager;
    }

    @Override
    public View create(String name, ViewGroup parent, boolean addToParent) {
        try {
            return layoutInflater.inflate(resourceManager.getLayoutIdentifier(name), parent, addToParent);
        }
        catch (Resources.NotFoundException e) {
            Timber.e(e, "Failed to create view: %s", name);
            View view = new View(parent.getContext());
            if (addToParent) {
                parent.addView(view);
            }
            return view;
        }
    }
}
