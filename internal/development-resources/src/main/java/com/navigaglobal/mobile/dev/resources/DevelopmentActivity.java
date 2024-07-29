package com.navigaglobal.mobile.dev.resources;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import se.infomaker.frt.moduleinterface.HostInterface;
import se.infomaker.frt.moduleinterface.ModuleInterface;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.ktx.ThemeUtils;

public abstract class DevelopmentActivity extends AppCompatActivity implements HostInterface {
    public static final String MODULE_ID = "dev_module";

    protected Fragment mCurrentFragment;

    public abstract Fragment createFragment();

    /**
     * Override this to change moduleId to use
     * @return
     */
    protected String getModuleId() {
        return MODULE_ID;
    }

    /**
     * Override this to change the id of the View to add the fragment to
     * @return
     */
    protected int getViewId() {
        return R.id.dev;
    }

    /**
     * Override this to change the layout to inflate
     * @return
     */
    protected int getLayout() {
        return R.layout.activity_dev;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        mCurrentFragment = getSupportFragmentManager().findFragmentByTag("currentFragment");
        if (mCurrentFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString("moduleId", getModuleId());

            mCurrentFragment = createFragment();
            mCurrentFragment.setArguments(bundle);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(getViewId(), mCurrentFragment, "currentFragment").commit();
        }

        Theme moduleTheme = ThemeManager.getInstance(this).getModuleTheme(getModuleId());
        ThemeUtils.apply(moduleTheme, this);
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
            if (!((ModuleInterface) mCurrentFragment).onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void openNavigationDrawer() {

    }
}
