/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.infomaker.livecontentui.livecontentdetailview.adapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.livecontentdetailview.pageadapters.OnPropertyObjectUpdated;
import timber.log.Timber;

/**
 * Implementation of {@link PagerAdapter} that
 * uses a {@link Fragment} to manage each page. This class also handles
 * saving and restoring of fragment's state.
 * <p>This version of the pager is more useful when there are a large number
 * of pages, working more like a list view.  When pages are not visible to
 * the user, their entire fragment may be destroyed, only keeping the saved
 * state of that fragment.  This allows the pager to hold on to much less
 * memory associated with each visited page as compared to
 * {@link ArticleFragmentStatePagerAdapter} at the cost of potentially more overhead when
 * switching between pages.
 * <p>When using FragmentPagerAdapter the host ViewPager must have a
 * valid ID set.</p>
 * <p>
 * <p>Subclasses only need to implement {@link #getItem(int)}
 * and {@link #getCount()} to have a working adapter.
 * <p>
 */

public abstract class ArticleFragmentStatePagerAdapter extends PagerAdapter {
    private static final boolean DEBUG = false;

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;

    private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<>();
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private Fragment mCurrentPrimaryItem = null;

    public List<PropertyObject> getmHitsList() {
        return mHitsListList;
    }

    private List<PropertyObject> mHitsListList = new ArrayList<>();
    private final Handler mHandler;

    public ArticleFragmentStatePagerAdapter(FragmentManager fm, List<PropertyObject> hitsListList) {
        mHandler = new Handler();
        mHitsListList = hitsListList;
        mFragmentManager = fm;
    }

    /**
     * Return the Fragment associated with a specified position.
     */
    public abstract Fragment getItem(int position);

    @Override
    public void startUpdate(ViewGroup container) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        if (mFragments.size() > position) {
            Fragment f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        Fragment fragment = getItem(position);
        if (DEBUG) Timber.v("Adding item #" + position + ": f=" + fragment);
        if (mSavedState.size() > position) {
            Fragment.SavedState fss = mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }
        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        fragment.setMenuVisibility(false);
        mFragments.set(position, fragment);
        mCurTransaction.add(container.getId(), fragment);
        mCurTransaction.setMaxLifecycle(fragment, Lifecycle.State.STARTED);

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG) Timber.v("Removing item #" + position + ": f=" + object
                + " v=" + ((Fragment) object).getView());
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
        }
        mSavedState.set(position, fragment.isAdded()
                ? mFragmentManager.saveFragmentInstanceState(fragment) : null);
        mFragments.set(position, null);

        mCurTransaction.remove(fragment);
        if (fragment.equals(mCurrentPrimaryItem)) {
            mCurrentPrimaryItem = null;
        }
    }

    @Nullable
    public Fragment getFragment(int position) {
        return mFragments.size() > position ? mFragments.get(position) : null;
    }

    public void addHitsListListsToStart(List<PropertyObject> hitsListsList) {
        mHitsListList.addAll(0, hitsListsList);
        shiftPositionForward(hitsListsList.size());
        notifyDataSetChanged();
    }

    public void addHitsListListsToEnd(List<PropertyObject> hitsListsList) {
        mHitsListList.addAll(hitsListsList);
        notifyDataSetChanged();
    }


    public void updateDataSet(List<PropertyObject> all, List<PropertyObject> updated) {
        if (updated != null) {
            // Make sure we have our own copy of the list as we modify it when adding
            // Make this less costly
            for (PropertyObject updatedObject : updated) {
                for (PropertyObject currentObject : mHitsListList) {
                    if (updatedObject.getId().equals(currentObject.getId())) {
                        Fragment fragment = getFragment(mHitsListList.indexOf(currentObject));
                        if (fragment instanceof OnPropertyObjectUpdated) {
                            ((OnPropertyObjectUpdated) fragment).onObjectUpdated(updatedObject);
                        }
                    }
                }
            }
        }
        mHitsListList = new ArrayList<>(all);
        notifyDataSetChanged();
    }

    private void shiftPositionForward(int steps) {
        for (int i = 0; i < steps; i++) {
            mFragments.add(0, null);
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        // Make sure we only notify on the ui thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.notifyDataSetChanged();
        }
        else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemPosition(Object object) {
        if (mFragments.contains(object)) {
            return mFragments.indexOf(object);
        }
        return POSITION_NONE;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                if (mCurTransaction == null) {
                    mCurTransaction = mFragmentManager.beginTransaction();
                }
                mCurTransaction.setMaxLifecycle(mCurrentPrimaryItem, Lifecycle.State.STARTED);
            }
            fragment.setMenuVisibility(true);
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            mCurTransaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED);

            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public int getCount() {
        return mHitsListList.size();
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (mSavedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null && f.isAdded()) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        return state;
    }

    public PropertyObject getHitslistAtPosition(int position) {
        if (mHitsListList.size() > position) {
            return mHitsListList.get(position);
        }
        return null;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            mSavedState.clear();
            mFragments.clear();
            if (fss != null) {
                for (int i = 0; i < fss.length; i++) {
                    mSavedState.add((Fragment.SavedState) fss[i]);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (mFragments.size() <= index) {
                            mFragments.add(null);
                        }
                        f.setMenuVisibility(false);
                        mFragments.set(index, f);
                    } else {
                        Timber.w("Bad fragment at key " + key);
                    }
                }
            }
        }
    }

    public List<PropertyObject> getHitsList() {
        return mHitsListList;
    }
}

