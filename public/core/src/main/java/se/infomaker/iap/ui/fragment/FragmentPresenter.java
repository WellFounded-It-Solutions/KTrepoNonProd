package se.infomaker.iap.ui.fragment;

import androidx.fragment.app.Fragment;

@SuppressWarnings("UnusedParameters")
public interface FragmentPresenter {
    /**
     * Called when a hosted fragment is dismissed
     * @param fragment being dismissed
     */
    void onFragmentDismissed(Fragment fragment);

    /**
     * Present fragment covering the current UI
     * @param fragment to present
     */
    void presentFullScreen(Fragment fragment);
}