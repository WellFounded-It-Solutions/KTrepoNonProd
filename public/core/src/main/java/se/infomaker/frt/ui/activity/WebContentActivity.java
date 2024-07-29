package se.infomaker.frt.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import dagger.hilt.android.AndroidEntryPoint;
import se.infomaker.frt.moduleinterface.ModuleInterface;
import com.navigaglobal.mobile.R;

@AndroidEntryPoint
public class WebContentActivity extends AppCompatActivity {

    public static final String WEB_FRAGMENT = "webFragment";

    private WebContentFragment mCurrentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity);

        mCurrentFragment = (WebContentFragment) getSupportFragmentManager().findFragmentByTag(WEB_FRAGMENT);
        if (mCurrentFragment == null) {
            Bundle bundle = new Bundle();

            bundle.putString("moduleId", getModuleId());
            bundle.putString("url", getUrl());
            bundle.putBoolean("autoplay", getAutoPlay());

            mCurrentFragment = new WebContentFragment();
            mCurrentFragment.setArguments(bundle);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, mCurrentFragment, WEB_FRAGMENT).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCurrentFragment != null)
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment instanceof ModuleInterface) {
            if (!mCurrentFragment.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    public String getModuleId() {
        return getIntent().getStringExtra("moduleId");
    }

    public String getUrl() {
        return getIntent().getStringExtra("url");
    }

    public boolean getAutoPlay() {
        return getIntent().getBooleanExtra("autoplay", false);
    }
}
