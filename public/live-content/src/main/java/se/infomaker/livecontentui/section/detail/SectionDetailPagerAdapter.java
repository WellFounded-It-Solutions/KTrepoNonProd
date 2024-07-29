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

package se.infomaker.livecontentui.section.detail;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.Pair;
import se.infomaker.livecontentui.livecontentdetailview.pageadapters.OnPropertyObjectUpdated;
import se.infomaker.livecontentui.section.PropertyObjectSectionItem;
import se.infomaker.livecontentui.section.SectionItem;
import timber.log.Timber;

public class SectionDetailPagerAdapter extends PagerAdapter {

    private final String moduleId;
    private final FragmentManager fragmentManager;
    private final ArrayList<Fragment.SavedState> savedState = new ArrayList<>();
    private final List<Fragment> fragments = new ArrayList<>();
    private FragmentTransaction mCurTransaction = null;
    private List<SectionItem> currentSectionItems;
    private Fragment currentPrimaryItem;
    private Pair<Integer, OnFragmentCreatedListener> onFragmentCreatedListener;

    public SectionDetailPagerAdapter(FragmentManager fm, String moduleId, List<SectionItem> sectionItems) {
        this.fragmentManager = fm;
        this.moduleId = moduleId;
        currentSectionItems = sectionItems;
    }

    public Fragment getItem(int position) {
        Fragment fragment = currentSectionItems.get(position).createDetailView(moduleId);
        if (onFragmentCreatedListener != null) {
            Integer watchedPosition = onFragmentCreatedListener.getFirst();
            if (watchedPosition != null && watchedPosition == position) {
                OnFragmentCreatedListener listener = onFragmentCreatedListener.getSecond();
                if (listener != null) {
                    listener.onFragmentCreated(fragment);
                }
            }
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return currentSectionItems != null ? currentSectionItems.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (fragments.contains(object)) {
            return fragments.indexOf(object);
        }
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        if (fragments.size() > position) {
            Fragment f = fragments.get(position);
            if (f != null) {
                return f;
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = fragmentManager.beginTransaction();
        }

        Fragment fragment = getItem(position);
        if (savedState.size() > position) {
            Fragment.SavedState fss = savedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }
        while (fragments.size() <= position) {
            fragments.add(null);
        }
        fragment.setMenuVisibility(false);
        fragments.set(position, fragment);
        mCurTransaction.add(container.getId(), fragment);
        mCurTransaction.setMaxLifecycle(fragment, Lifecycle.State.STARTED);

        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Fragment fragment = (Fragment) object;

        if (mCurTransaction == null) {
            mCurTransaction = fragmentManager.beginTransaction();
        }
        while (savedState.size() <= position) {
            savedState.add(null);
        }
        savedState.set(position, fragment.isAdded()
                ? fragmentManager.saveFragmentInstanceState(fragment) : null);
        fragments.set(position, null);

        mCurTransaction.remove(fragment);

        if (fragment.equals(currentPrimaryItem)) {
            currentPrimaryItem = null;
        }
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (savedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[savedState.size()];
            savedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i = 0; i < fragments.size(); i++) {
            Fragment f = fragments.get(i);
            if (f != null && f.isAdded()) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                fragmentManager.putFragment(state, key, f);
            }
        }
        return state;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != currentPrimaryItem) {
            if (currentPrimaryItem != null) {
                currentPrimaryItem.setMenuVisibility(false);
                if (mCurTransaction == null) {
                    mCurTransaction = fragmentManager.beginTransaction();
                }
                mCurTransaction.setMaxLifecycle(currentPrimaryItem, Lifecycle.State.STARTED);
            }
            fragment.setMenuVisibility(true);
            if (mCurTransaction == null) {
                mCurTransaction = fragmentManager.beginTransaction();
            }
            mCurTransaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED);

            currentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            fragmentManager.executePendingTransactions();
        }
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            savedState.clear();
            fragments.clear();
            if (fss != null) {
                for (Parcelable parcelable : fss) {
                    savedState.add((Fragment.SavedState) parcelable);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = fragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (fragments.size() <= index) {
                            fragments.add(null);
                        }
                        f.setMenuVisibility(false);
                        fragments.set(index, f);
                    } else {
                        Timber.w("Bad fragment at key %s", key);
                    }
                }
            }
        }
    }

    public synchronized void submitList(List<SectionItem> sectionItems) {
        if (currentSectionItems != null) {
            for (SectionItem updatedObject : sectionItems) {
                for (SectionItem currentObject : currentSectionItems) {
                    if (updatedObject.getId().equals(currentObject.getId()) && !updatedObject.areContentsTheSame(currentObject)) {
                        int index = currentSectionItems.indexOf(currentObject);
                        if (fragments.size() > index) {
                            Fragment fragment = getFragment(index);
                            if (fragment instanceof OnPropertyObjectUpdated && updatedObject instanceof PropertyObjectSectionItem) {
                                ((OnPropertyObjectUpdated) fragment).onObjectUpdated(((PropertyObjectSectionItem) updatedObject).getPropertyObject());
                            }
                        }
                    }
                }
            }
        }
        currentSectionItems = sectionItems;
        notifyDataSetChanged();
    }

    public Fragment getFragment(int position) {
        if (fragments.size() > position) {
            return fragments.get(position);
        }
        return null;
    }

    public Set<Fragment> nearbyFragments(int position) {
        HashSet<Fragment> nearby = new HashSet<>();
        if (position > 0 && fragments.get(position - 1) != null) {
            nearby.add(fragments.get(position - 1));
        }
        if (fragments.size() > position + 1 && fragments.get(position + 1) != null) {
            nearby.add(fragments.get(position + 1));
        }
        return nearby;
    }

    public SectionItem getCurrentItem(int position) {
        return currentSectionItems != null && currentSectionItems.size() > position ? currentSectionItems.get(position) : null;
    }

    public void setOnFragmentCreatedListener(int position, OnFragmentCreatedListener listener) {
        onFragmentCreatedListener = new Pair<>(position, listener);
    }

    public void clearOnFragmentCreatedListener() {
        onFragmentCreatedListener = null;
    }

    public interface OnFragmentCreatedListener {
        void onFragmentCreated(@NonNull Fragment fragment);
    }
}