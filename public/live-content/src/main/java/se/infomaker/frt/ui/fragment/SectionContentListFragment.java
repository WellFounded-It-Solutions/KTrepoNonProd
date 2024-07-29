package se.infomaker.frt.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import dagger.hilt.android.AndroidEntryPoint;
import se.infomaker.frt.moduleinterface.BaseModule;
import com.navigaglobal.mobile.livecontent.R;
import se.infomaker.livecontentui.section.SectionedLiveContentFragment;

@AndroidEntryPoint
public class SectionContentListFragment extends BaseModule {

    private SectionedLiveContentFragment fragment;

    public static SectionContentListFragment newInstance() {
        return new SectionContentListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment = (SectionedLiveContentFragment) getChildFragmentManager().findFragmentByTag("currentFragment");
        if (fragment == null) {
            Bundle bundle = new Bundle(getArguments());
            bundle.putString("moduleId", getModuleIdentifier());
            bundle.putString("moduleName", "SectionContentList");

            fragment = new SectionedLiveContentFragment();
            fragment.setArguments(bundle);

            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.list_frame, fragment, "currentFragment");
            ft.commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_list, container, false);
    }

    @Override
    public boolean shouldDisplayToolbar() {
        return true;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onAppBarPressed() {
        fragment.scrollToTop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        fragment.onActivityResult(requestCode, resultCode, data);
    }
}
