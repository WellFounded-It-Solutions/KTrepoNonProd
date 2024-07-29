package se.infomaker.frt.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.navigaglobal.mobile.R;

import java.util.ArrayList;

import se.infomaker.frt.moduleinterface.ModuleInterface;
import se.infomaker.frt.ui.activity.FragmentHelper;
import se.infomaker.frt.ui.activity.MainActivity;
import se.infomaker.frt.ui.adapter.PagerAdapter;
import se.infomaker.frtutilities.MainMenuConfig;
import se.infomaker.frtutilities.MainMenuItem;
import timber.log.Timber;

public class ViewPagerFragment extends Fragment implements ModuleInterface {

    private static final String ARG_ID = "id";
    private static final String TAG = "ViewPagerFragment";


    TabLayout mTabLayout;

    ViewPager mViewPager;

    int mId = -1;
    PagerAdapter mPagerAdapter;

    String mPrimaryColor;
    String mSecondaryColor;
    String mTextColor;

    public static ViewPagerFragment newInstance() {
        ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
        return viewPagerFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Handle any bundle properties here.
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(ARG_ID)) {
                mId = arguments.getInt(ARG_ID);
            }

            mPrimaryColor = arguments.getString("primaryColor");
            mSecondaryColor = arguments.getString("secondaryColor");
            mTextColor = arguments.getString("textColor");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.view_pager_fragment, container, false);

        mTabLayout = fragmentView.findViewById(R.id.tab_layout);
        mViewPager = fragmentView.findViewById(R.id.view_pager);

        setupTabLayout();
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupTabLayout() {
        MainMenuConfig modulesConfig = getMainMenuConfig();
        if (modulesConfig != null && mId >= 0) {
            mPagerAdapter = new PagerAdapter(getChildFragmentManager());

            ArrayList<MainMenuItem> subMenuItems = modulesConfig.getMainMenuItems().get(mId).getSubMenu();
            if (subMenuItems != null) {
                for (MainMenuItem subMenuItem : subMenuItems) {
                    if (getActivity() instanceof MainActivity) {
                        Fragment fragment = FragmentHelper.createModuleFragment(getActivity(), subMenuItem);
                        mPagerAdapter.addFragment(subMenuItem.getTitle(), fragment);
                    }
                }
            }

            mViewPager.setOffscreenPageLimit(2);
            mViewPager.setAdapter(mPagerAdapter);
            mTabLayout.setupWithViewPager(mViewPager);
            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

            mTabLayout.setBackgroundColor(Color.parseColor("#" + mPrimaryColor));
            mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#" + mSecondaryColor));

            float[] hsv = new float[3];
            Color.colorToHSV(Color.parseColor("#" + mSecondaryColor), hsv);
            hsv[2] *= 0.8f;
            int secondaryColorDarker = Color.HSVToColor(hsv);
            mTabLayout.setTabTextColors(secondaryColorDarker, Color.parseColor("#" + mSecondaryColor));

            if (mPagerAdapter.getCount() > 1) {
                mTabLayout.setVisibility(View.VISIBLE);
            } else {
                mTabLayout.setVisibility(View.GONE);
            }
        }
    }

    private MainMenuConfig getMainMenuConfig() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getMainMenuConfig();
        } else {
            return null;
        }
    }

    private Fragment getCurrentFragment() {
        return mPagerAdapter.getItem(mViewPager.getCurrentItem());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Needed to call onActivityResult on child fragments
        getCurrentFragment().onActivityResult(requestCode, resultCode, data);
//                .onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean shouldDisplayToolbar() {
        return true;
    }

    @Override
    public boolean onBackPressed() {
        Timber.d("onBackPressed");
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof ModuleInterface) {
            return ((ModuleInterface) currentFragment).onBackPressed();
        }
        return false;
    }

    @Override
    public void onAppBarPressed() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof ModuleInterface)
            ((ModuleInterface) currentFragment).onAppBarPressed();
    }
}
