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
import se.infomaker.livecontentui.livecontentrecyclerview.fragment.LiveContentRecyclerViewFragment;
import timber.log.Timber;

@AndroidEntryPoint
public class ContentListFragment extends BaseModule {

    private LiveContentRecyclerViewFragment fragment;

    public ContentListFragment() {
        // Required empty public constructor
    }

    public static ContentListFragment newInstance() {
        return new ContentListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = (LiveContentRecyclerViewFragment) getChildFragmentManager().findFragmentByTag("currentFragment");
        if (fragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString("moduleId", getModuleIdentifier());
            bundle.putString("moduleName", "ContentList");

            fragment = new LiveContentRecyclerViewFragment();
            fragment.setArguments(bundle);

            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.list_frame, fragment, "currentFragment");
            ft.commit();

            Timber.d("ContentListFragment ModuleId: %s", getModuleIdentifier());
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
