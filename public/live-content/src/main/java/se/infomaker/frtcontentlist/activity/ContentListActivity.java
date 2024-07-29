package se.infomaker.frtcontentlist.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import dagger.hilt.android.AndroidEntryPoint;
import se.infomaker.frt.ui.fragment.ContentListFragment;
import com.navigaglobal.mobile.livecontent.R;

@AndroidEntryPoint
public class ContentListActivity extends AppCompatActivity {

    private Fragment mCurrentFragment;

    public static Intent getIntent(Context context) {
        return new Intent(context, ContentListActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        mCurrentFragment = getSupportFragmentManager().findFragmentByTag("currentFragment");
        if (mCurrentFragment == null) {
            mCurrentFragment = ContentListFragment.newInstance();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragmentHolder, mCurrentFragment, "currentFragment").commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCurrentFragment != null) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
