package se.infomaker.frt.ui.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by davidolsson on 10/22/15.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    LinkedHashMap<String, Fragment> mFragments = new LinkedHashMap<>();

    public PagerAdapter(FragmentManager paramFragmentManager) {
        super(paramFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    public void addFragment(String title, Fragment paramFragment) {
        this.mFragments.put(title, paramFragment);
    }

    public int getCount() {
        return this.mFragments.size();
    }

    public Fragment getItem(int paramInt) {
        return (new ArrayList<>(mFragments.values())).get(paramInt);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return (new ArrayList<String>(mFragments.keySet())).get(position);
    }
}
