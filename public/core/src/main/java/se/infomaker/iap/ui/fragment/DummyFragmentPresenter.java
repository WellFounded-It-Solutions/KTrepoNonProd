package se.infomaker.iap.ui.fragment;

import androidx.fragment.app.Fragment;

/**
 * Placeholder NOP implementation
 */
public class DummyFragmentPresenter implements FragmentPresenter {
    public static final FragmentPresenter INSTANCE = new DummyFragmentPresenter();

    @Override
    public void onFragmentDismissed(Fragment fragment) {

    }

    @Override
    public void presentFullScreen(Fragment fragment) {

    }
}
