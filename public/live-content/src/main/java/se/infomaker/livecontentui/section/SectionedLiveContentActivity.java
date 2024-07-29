package se.infomaker.livecontentui.section;

import androidx.fragment.app.Fragment;

import se.infomaker.livecontentui.livecontentrecyclerview.activity.LiveContentRecyclerviewActivity;

public class SectionedLiveContentActivity extends LiveContentRecyclerviewActivity {
    @Override
    protected Fragment createFragment() {
        SectionedLiveContentFragment fragment = new SectionedLiveContentFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
