package se.infomaker.iap.ui.view;

import android.view.View;
import android.view.ViewGroup;

/**
 * Creates views based on names
 */
public interface ViewFactory {
    /**
     * Creates a view
     * @param name of the view to create
     * @param parent the view should be created as child of
     * @param addToParent if the view should be added to the parent
     * @return view created for name
     */
    @SuppressWarnings("SameParameterValue")
    View create(String name, ViewGroup parent, boolean addToParent);
}
