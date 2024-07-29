package se.infomaker.frt.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.navigaglobal.mobile.R;

import org.jetbrains.annotations.NotNull;

public class BaseActivity extends AppCompatActivity {
    public static final String CURRENT_FRAGMENT = "currentMainFragment";
    protected Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            FragmentManager fm = getSupportFragmentManager();
            mCurrentFragment = fm.findFragmentByTag(CURRENT_FRAGMENT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Replace the current fragment with a new one.
     *
     * @param newFragment The fragment you want to switch to.
     */
    public void switchFragment(@NotNull Fragment newFragment) {
        if (!isFinishing() && mCurrentFragment != newFragment) {
            mCurrentFragment = newFragment;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.content_frame, newFragment, CURRENT_FRAGMENT);
            ft.commit();
        }
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }
}
